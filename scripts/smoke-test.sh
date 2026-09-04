#!/usr/bin/env bash
set -euo pipefail

for tool in docker curl python3; do
  command -v "$tool" >/dev/null || { echo "Missing required tool: $tool" >&2; exit 1; }
done

compose_project="${COMPOSE_PROJECT_NAME:-pdp-smoke}"
compose=(docker compose --project-name "$compose_project")
invalid_body="$(mktemp)"

cleanup() {
  local status=$?
  rm -f "$invalid_body"
  if (( status != 0 )); then
    echo "Smoke test failed; capturing container state and logs." >&2
    "${compose[@]}" ps --all >&2 || true
    "${compose[@]}" logs --no-color --tail=200 gateway-service order-service keycloak postgres redis kafka >&2 || true
  fi
  if [[ "${SMOKE_KEEP_RUNNING:-false}" != "true" ]]; then
    "${compose[@]}" down --volumes --remove-orphans
  fi
  return "$status"
}
trap cleanup EXIT

"${compose[@]}" up --build --detach --wait --wait-timeout 300

token_response="$(curl --fail --silent --show-error \
  --data-urlencode client_id=platform-cli \
  --data-urlencode username=demo-user \
  --data-urlencode password=demo-password \
  --data-urlencode grant_type=password \
  http://localhost:8180/realms/platform/protocol/openid-connect/token)"
access_token="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])' <<<"$token_response")"

other_token_response="$(curl --fail --silent --show-error \
  --data-urlencode client_id=platform-cli \
  --data-urlencode username=other-user \
  --data-urlencode password=other-password \
  --data-urlencode grant_type=password \
  http://localhost:8180/realms/platform/protocol/openid-connect/token)"
other_access_token="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])' <<<"$other_token_response")"

wrong_audience_response="$(curl --fail --silent --show-error \
  --data-urlencode client_id=wrong-audience-cli \
  --data-urlencode username=demo-user \
  --data-urlencode password=demo-password \
  --data-urlencode grant_type=password \
  http://localhost:8180/realms/platform/protocol/openid-connect/token)"
wrong_audience_token="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])' <<<"$wrong_audience_response")"

unauthorized_status="$(curl --silent --output /dev/null --write-out '%{http_code}' http://localhost:8080/api/orders)"
[[ "$unauthorized_status" == "401" ]] || { echo "Expected unauthenticated request to return 401, got $unauthorized_status" >&2; exit 1; }

wrong_audience_status="$(curl --silent --output /dev/null --write-out '%{http_code}' \
  -H "Authorization: Bearer $wrong_audience_token" \
  http://localhost:8080/api/orders)"
[[ "$wrong_audience_status" == "401" ]] || { echo "Expected wrong audience to return 401, got $wrong_audience_status" >&2; exit 1; }

invalid_status="$(curl --silent --output /dev/null --write-out '%{http_code}' \
  -H "Authorization: Bearer $access_token" \
  -H 'Content-Type: application/json' \
  -d '{"amount":0,"currency":"usd"}' \
  http://localhost:8080/api/orders)"
[[ "$invalid_status" == "400" ]] || { echo "Expected invalid order to return 400, got $invalid_status" >&2; exit 1; }

problem_status="$(curl --silent --show-error --output "$invalid_body" --write-out '%{http_code}' \
  -H "Authorization: Bearer $access_token" \
  -H 'Content-Type: application/json' \
  -d '{"amount":1000000.01,"currency":"EUR"}' \
  http://localhost:8080/api/orders)"
[[ "$problem_status" == "400" ]] || { echo "Expected validation problem to return 400, got $problem_status" >&2; exit 1; }
python3 -c 'import json,sys; data=json.load(open(sys.argv[1])); assert data["type"] == "urn:problem-type:validation"; assert data["code"] == "validation_failed"; assert len(data["violations"]) >= 2' "$invalid_body"

created="$(curl --fail --silent --show-error \
  -H "Authorization: Bearer $access_token" \
  -H 'Content-Type: application/json' \
  -d '{"amount":10.50,"currency":"USD"}' \
  http://localhost:8080/api/orders)"
python3 -c 'import json,sys; data=json.load(sys.stdin); assert data["amount"] == 10.50; assert data["currency"] == "USD"; assert data["status"] == "CREATED"' <<<"$created"

orders="$(curl --fail --silent --show-error -H "Authorization: Bearer $access_token" http://localhost:8080/api/orders)"
python3 -c 'import json,sys; data=json.load(sys.stdin); assert len(data) >= 1; assert all(item["amount"] > 0 for item in data)' <<<"$orders"

other_orders="$(curl --fail --silent --show-error -H "Authorization: Bearer $other_access_token" http://localhost:8080/api/orders)"
python3 -c 'import json,sys; data=json.load(sys.stdin); assert data == []' <<<"$other_orders"

"${compose[@]}" exec -T -e PGPASSWORD=app-local-password postgres \
  psql --host=127.0.0.1 --username=platform_app --dbname=platform \
  --set=ON_ERROR_STOP=1 --command='SELECT COUNT(*) FROM orders;' >/dev/null

if "${compose[@]}" exec -T -e PGPASSWORD=app-local-password postgres \
    psql --host=127.0.0.1 --username=platform_app --dbname=platform \
    --set=ON_ERROR_STOP=1 --command='CREATE TABLE forbidden_runtime_ddl (id INTEGER);' >/dev/null 2>&1; then
  echo "Runtime database role unexpectedly has DDL permission" >&2
  exit 1
fi

echo "Authentication, audience, authorization, validation, database permissions, customer isolation, creation, and retrieval smoke tests passed."
