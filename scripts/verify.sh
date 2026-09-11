#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

for tool in java mvn docker helm python3; do
  command -v "$tool" >/dev/null || { echo "Missing required tool: $tool" >&2; exit 1; }
done

java_major="$(java -version 2>&1 | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' | head -n1)"
[[ "$java_major" == "21" ]] || { echo "Java 21 is required; found Java ${java_major:-unknown}" >&2; exit 1; }

helm_version="$(helm version --short)"
[[ "$helm_version" == v3.* ]] || { echo "Helm 3 is required; found $helm_version" >&2; exit 1; }

python3 scripts/test_delivery.py
mvn -B clean verify
docker compose config --quiet
docker compose --file docker-compose.yml --env-file /dev/null config --format json | python3 -c '
import json, sys
services = json.load(sys.stdin)["services"]
for name, service in services.items():
    for port in service.get("ports", []):
        assert port.get("host_ip") == "127.0.0.1", f"Demo service {name} must bind to loopback"
print("All published demo ports are loopback-only.")
'

rendered="$(mktemp)"
trap 'rm -f "$rendered"' EXIT
for profile in default production; do
  values=()
  if [[ "$profile" == production ]]; then
    values=(-f deploy/helm/platform/values-prod.yaml)
  fi
  helm lint deploy/helm/platform "${values[@]}"
  helm template platform deploy/helm/platform "${values[@]}" >"$rendered"
  for kind in Deployment Service HorizontalPodAutoscaler PodDisruptionBudget ServiceMonitor; do
    expected=2
    [[ "$kind" != ServiceMonitor ]] || expected=1
    actual="$(awk -v kind="$kind" '$0 == "kind: " kind { count++ } END { print count+0 }' "$rendered")"
    printf 'profile=%s kind=%s expected=%s actual=%s\n' "$profile" "$kind" "$expected" "$actual"
    [[ "$actual" -eq "$expected" ]] || { echo "Unexpected $kind count in $profile render" >&2; exit 1; }
  done
done

echo "All unit, configuration, and Helm rendering checks passed."
