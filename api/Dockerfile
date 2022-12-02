FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine-slim

ARG BUILD_DATE
ARG GIT_REV
ARG VERSION

WORKDIR /api

USER nobody

LABEL org.label-schema.name="api" \
      org.label-schema.vendor="=DevXDao" \
      org.label-schema.build-date="${BUILD_DATE}" \
      org.label-schema.vcs-ref="${GIT_REV}" \
      org.label-schema.version="${VERSION}" \
      org.opencontainers.image.source="https://github.com/noumenadigital/replatform"

COPY target/dependency /api/lib
COPY target/api.jar /api/api.jar

#CMD ["java", "-cp", "/api/api.jar:/api/lib/*", "dx.AppKt", "-Djava.util.logging.config.file=\"logging.properties\""]
CMD ["java", "-cp", "/api/api.jar:/api/lib/*", "dx.AppKt"]
