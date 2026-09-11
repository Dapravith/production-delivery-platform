#!/usr/bin/env python3
"""Delivery-script regression tests using fake CLIs, not runtime acceptance proof."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[1]


class DeliveryScriptTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.work = Path(self.temp.name)
        self.bin = self.work / "bin"
        self.bin.mkdir()
        self.env = dict(os.environ, PATH=f"{self.bin}:{os.environ['PATH']}",
                        EVIDENCE_DIR=str(self.work / "evidence"),
                        TRACE_FILE=str(self.work / "docker-trace"),
                        COMPOSE_PROJECT_NAME="pdp-regression")
        for variable in ("FAKE_MAVEN_EXIT", "FAKE_HELM_VERSION", "FAKE_BAD_RENDER",
                         "FAKE_EXISTING_RESOURCE"):
            self.env.pop(variable, None)
        self.stub("java", 'echo \'openjdk version "21.0.1"\' >&2')
        self.stub("mvn", '''
if [[ "$1" == --version ]]; then echo 'Apache Maven 3.9.16'; exit 0; fi
echo 'fake Maven verification'
exit "${FAKE_MAVEN_EXIT:-0}"
''')
        # Avoid recursively invoking these orchestration tests from verify.sh.
        self.stub("python3", "exit 0")
        self.stub("docker", '''
printf '%s\\n' "$*" >>"$TRACE_FILE"
if [[ "$1" == "${FAKE_EXISTING_RESOURCE:-none}" && "$2" == ls ]]; then
  echo existing-resource
fi
''')
        self.stub("helm", '''
if [[ "$1" == version ]]; then echo "${FAKE_HELM_VERSION:-v3.21.4}"; exit 0; fi
if [[ "$1" == template ]]; then
  for kind in Deployment Service HorizontalPodAutoscaler PodDisruptionBudget; do
    [[ "$kind" != "${FAKE_BAD_RENDER:-none}" ]] || continue
    printf 'kind: %s\\nkind: %s\\n' "$kind" "$kind"
  done
  echo 'kind: ServiceMonitor'
fi
''')

    def stub(self, name, body):
        path = self.bin / name
        path.write_text("#!/usr/bin/env bash\nset -euo pipefail\n" + body + "\n")
        path.chmod(0o755)

    def run_script(self, name, *args):
        return subprocess.run(["bash", str(ROOT / "scripts" / name), *args],
                              cwd=self.work, env=self.env, text=True,
                              stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                              timeout=15)

    def test_verify_records_counts_for_both_profiles(self):
        result = self.run_script("verify.sh")
        self.assertEqual(result.returncode, 0, result.stdout)
        for profile in ("default", "production"):
            self.assertIn(f"profile={profile} kind=Deployment expected=2 actual=2", result.stdout)
            self.assertIn(f"profile={profile} kind=ServiceMonitor expected=1 actual=1", result.stdout)

    def test_missing_rendered_resources_fail_the_gate(self):
        self.env["FAKE_BAD_RENDER"] = "Deployment"
        result = self.run_script("verify.sh")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("kind=Deployment expected=2 actual=0", result.stdout)

    def test_helm_four_is_not_silently_accepted(self):
        self.env["FAKE_HELM_VERSION"] = "v4.0.0"
        result = self.run_script("verify.sh")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Helm 3 is required", result.stdout)

    def test_evidence_survives_failure_without_hiding_exit_code(self):
        self.env["FAKE_MAVEN_EXIT"] = "23"
        result = self.run_script("ci-verify.sh", "verify")
        self.assertEqual(result.returncode, 23, result.stdout)
        evidence = self.work / "evidence"
        self.assertEqual((evidence / "verify-exit-code.txt").read_text().strip(), "23")
        self.assertIn("fake Maven verification", (evidence / "verify.log").read_text())
        self.assertRegex((evidence / "verify-metadata.txt").read_text(), r"commit=[0-9a-f]{40}")

    def test_success_also_records_evidence(self):
        result = self.run_script("ci-verify.sh", "verify")
        self.assertEqual(result.returncode, 0, result.stdout)
        self.assertEqual((self.work / "evidence/verify-exit-code.txt").read_text().strip(), "0")

    def test_unknown_evidence_phase_fails(self):
        result = self.run_script("ci-verify.sh", "deploy")
        self.assertEqual(result.returncode, 2)
        self.assertFalse((self.work / "evidence").exists())

    def test_existing_compose_resources_are_not_modified_or_deleted(self):
        for resource in ("container", "volume"):
            with self.subTest(resource=resource):
                self.env["FAKE_EXISTING_RESOURCE"] = resource
                result = self.run_script("smoke-test.sh")
                self.assertNotEqual(result.returncode, 0)
                self.assertIn("Refusing to reuse Compose project", result.stdout)
                trace = (self.work / "docker-trace").read_text()
                self.assertIn("container ls --all", trace)
                self.assertNotIn("compose ", trace)


if __name__ == "__main__":
    unittest.main(verbosity=2)
