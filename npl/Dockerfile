FROM ghcr.io/noumenadigital/packages/engine:2022.1.16

ARG BUILD_DATE
ARG GIT_REV
ARG VERSION
LABEL org.label-schema.name="npl" \
      org.label-schema.vendor="devxdao" \
      org.label-schema.build-date="${BUILD_DATE}" \
      org.label-schema.vcs-ref="${GIT_REV}" \
      org.label-schema.version="${VERSION}" \
      org.opencontainers.image.source="https://github.com/noumenadigital/replatform"

ENV ENGINE_NPL_MIGRATION_DIRECTORYPATH="/npl" \
    FEATURE_APPLY_NPL_MIGRATIONS_AT_STARTUP="true"

COPY src/main/yaml /npl/yaml
COPY src/main/npl  /npl/npl-1.0.0
