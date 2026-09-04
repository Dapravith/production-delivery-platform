#!/usr/bin/env bash
set -euo pipefail

for tool in java mvn docker helm; do
  command -v "$tool" >/dev/null || { echo "Missing required tool: $tool" >&2; exit 1; }
done

java_major="$(java -version 2>&1 | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' | head -n1)"
[[ "$java_major" == "21" ]] || { echo "Java 21 is required; found Java ${java_major:-unknown}" >&2; exit 1; }

mvn -B clean verify
docker compose config --quiet
helm lint deploy/helm/platform -f deploy/helm/platform/values-prod.yaml

rendered="$(mktemp)"
trap 'rm -f "$rendered"' EXIT
helm template platform deploy/helm/platform -f deploy/helm/platform/values-prod.yaml >"$rendered"

[[ "$(grep -c '^kind: Deployment$' "$rendered")" -eq 2 ]]
[[ "$(grep -c '^kind: Service$' "$rendered")" -eq 2 ]]
[[ "$(grep -c '^kind: HorizontalPodAutoscaler$' "$rendered")" -eq 2 ]]
[[ "$(grep -c '^kind: PodDisruptionBudget$' "$rendered")" -eq 2 ]]
[[ "$(grep -c '^kind: ServiceMonitor$' "$rendered")" -eq 1 ]]

echo "All unit, configuration, and Helm rendering checks passed."
