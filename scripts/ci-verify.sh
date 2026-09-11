#!/usr/bin/env bash
# Share the same evidence contract between Jenkins and GitHub Actions.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

phase="${1:-}"
case "$phase" in
  verify) command=(bash scripts/verify.sh) ;;
  smoke) command=(bash scripts/smoke-test.sh) ;;
  *) echo "Usage: bash scripts/ci-verify.sh {verify|smoke}" >&2; exit 2 ;;
esac

evidence_dir="${EVIDENCE_DIR:-build/evidence}"
mkdir -p "$evidence_dir"
trap 'status=$?; printf "%s\n" "$status" >"$evidence_dir/$phase-exit-code.txt"' EXIT
{
  printf 'commit=%s\n' "$(git rev-parse HEAD)"
  printf 'phase=%s\n' "$phase"
  printf 'started_utc=%s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  printf 'tracked_changes=%s\n' "$(git status --porcelain --untracked-files=no | wc -l | tr -d ' ')"
} >"$evidence_dir/$phase-metadata.txt"

{
  if [[ "$phase" == verify ]]; then
    java -version
    mvn --version
    docker --version
    docker compose version
    helm version --short
  fi
  "${command[@]}"
} 2>&1 | tee "$evidence_dir/$phase.log"
