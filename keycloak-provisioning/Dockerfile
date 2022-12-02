FROM hashicorp/terraform:latest

LABEL org.opencontainers.image.source="https://github.com/noumenadigital/replatform"

ENV KEYCLOAK_USER="admin" \
    KEYCLOAK_PASSWORD="DevX123!" \
    KEYCLOAK_URL=http://keycloak:11000

WORKDIR /terraform
COPY *.tf *.sh /terraform/
VOLUME /state

ENTRYPOINT ["/bin/sh", "-c"]
