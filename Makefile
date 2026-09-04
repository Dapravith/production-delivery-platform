.PHONY: test package compose-up compose-down helm-lint verify smoke
test:
	mvn verify
package:
	mvn -DskipTests package
compose-up:
	docker compose up -d
compose-down:
	docker compose down
helm-lint:
	helm lint deploy/helm/platform
verify:
	./scripts/verify.sh
smoke:
	./scripts/smoke-test.sh
