CI_COMMIT_SHA=HEAD

LEVANT_VERSION=0.3.0
VERSION?=1.0-SNAPSHOT
MAVEN_CLI_OPTS?=--settings ./.m2/settings.xml

define deploy
	docker run --rm -v $(CURDIR)/nomad:/jobs:ro --network=host \
		hashicorp/levant:$(LEVANT_VERSION) levant deploy \
			-address $(NOMAD_ADDR) \
			-ignore-no-changes \
			-force-count \
			-var 'version=$(VERSION)' \
			-var-file /jobs/env-$(ENVIRONMENT).yml \
			/jobs/$1.nomad
endef

# $(call tag_and_push,IMAGE_NAME) tags IMAGE_NAME with VERSION and pushes it to the repository
define tag_and_push
	docker tag $1:latest $1:$(VERSION)
	docker push $1:$(VERSION)
endef

.PHONY:	clean
clean:
	docker-compose down --remove-orphans --volumes
	mvn $(MAVEN_CLI_OPTS) clean

.PHONY: test
test:
	mvn $(MAVEN_CLI_OPTS) clean verify

.PHONY: install
install:
	mvn $(MAVEN_CLI_OPTS) install
	docker-compose build --build-arg VERSION="$(VERSION)" --build-arg GIT_REV="$(CI_COMMIT_SHA)" --build-arg BUILD_DATE="$(shell date)"

.PHONY:	format
format:
	mvn $(MAVEN_CLI_OPTS) ktlint:format

.PHONY:	run
run: format install
	docker-compose up -d

.PHONY: release-docker-image
release-docker-image: install
	$(call tag_and_push,ghcr.io/noumenadigital/replatform/engine)
	$(call tag_and_push,ghcr.io/noumenadigital/replatform/api)
	$(call tag_and_push,ghcr.io/noumenadigital/replatform/keycloak-provisioning)

.PHONY: deploy-dev
deploy-dev: export NOMAD_ADDR=https://nomad.devxdao-dev.noumenadigital.com
deploy-dev: export ENVIRONMENT=demo
deploy-dev:
	@if [[ "$(VERSION)" = "latest" ]] || [[ "$(VERSION)" = "" ]]; then echo "Explicit VERSION not set"; exit 1; fi
	$(call deploy,keycloak)
	$(call deploy,keycloak-provisioning)
	$(call deploy,platform)
	$(call deploy,api)

.PHONY: clean-dev
clean-dev: export NOMAD_ADDR=https://nomad.devxdao-dev.noumenadigital.com
clean-dev: export ENVIRONMENT=demo
clean-dev:
	-nomad stop -yes -purge cleanup
	-nomad stop -yes -purge keycloak
	-nomad stop -yes -purge keycloak-provisioning
	-nomad stop -yes -purge platform
	-nomad stop -yes -purge api
	$(call deploy,cleanup)
