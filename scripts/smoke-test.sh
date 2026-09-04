#!/usr/bin/env bash
set -euo pipefail

for tool in docker curl python3; do
  command -v "$tool" >/dev/null || { echo "Missing required tool: $tool" >&2; exit 1; }
done

cleanup() {
  if [[ "${SMOKE_KEEP_RUNNING:-false}" != "true" ]]; then
    docker compose down
  fi
}
trap cleanup EXIT

docker compose up --build --detach --wait --wait-timeout 300

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

unauthorized_status="$(curl --silent --output /dev/null --write-out '%{http_code}' http://localhost:8080/api/orders)"
[[ "$unauthorized_status" == "401" ]] || { echo "Expected unauthenticated request to return 401, got $unauthorized_status" >&2; exit 1; }

invalid_status="$(curl --silent --output /dev/null --write-out '%{http_code}' \
  -H "Authorization: Bearer $access_token" \
  -H 'Content-Type: application/json' \
  -d '{"amount":0}' \
  http://localhost:8080/api/orders)"
[[ "$invalid_status" == "400" ]] || { echo "Expected invalid order to return 400, got $invalid_status" >&2; exit 1; }

created="$(curl --fail --silent --show-error \
  -H "Authorization: Bearer $access_token" \
  -H 'Content-Type: application/json' \
  -d '{"amount":10.50}' \
  http://localhost:8080/api/orders)"
python3 -c 'import json,sys; data=json.load(sys.stdin); assert data["amount"] == 10.50; assert data["status"] == "CREATED"' <<<"$created"

orders="$(curl --fail --silent --show-error -H "Authorization: Bearer $access_token" http://localhost:8080/api/orders)"
python3 -c 'import json,sys; data=json.load(sys.stdin); assert len(data) >= 1; assert all(item["amount"] > 0 for item in data)' <<<"$orders"

other_orders="$(curl --fail --silent --show-error -H "Authorization: Bearer $other_access_token" http://localhost:8080/api/orders)"
python3 -c 'import json,sys; data=json.load(sys.stdin); assert data == []' <<<"$other_orders"

echo "Authentication, validation, customer isolation, order creation, and retrieval smoke tests passed."
