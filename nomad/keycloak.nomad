job "keycloak" {
  datacenters = [
    "[[ .datacenter ]]",
  ]

  constraint {
    attribute = "${node.class}"
    value     = "worker"
  }

  type = "service"

  update {
    min_healthy_time = "10s"
    auto_revert      = true
  }

  group "keycloak" {
    count = 1

    network {
      port "http" {
        static = 11000
      }
    }

    service {
      port = "http"
      name = "keycloak"
      tags = [
        "version=[[ .version ]]",
        "traefik.enable=true",
        "traefik.frontend.rule=Host:keycloak.[[ .domain ]];PathPrefix:/",
        "traefik.frontend.entryPoints=internal",
      ]
      check {
        name     = "Keycloak HTTP interface"
        type     = "http"
        path     = "/health"
        interval = "10s"
        timeout  = "1s"
      }
    }

    task "keycloak" {
      leader = true
      driver = "docker"
      config {
        image        = "quay.io/keycloak/keycloak:19.0.0"
        network_mode = "host"
        args = [
          "start",
          "--auto-build",
          "--hostname-strict=false",
          "--hostname-strict-https=false",
          "--http-enabled=true",
          "--metrics-enabled=true",
          "--db=postgres",
          "--health-enabled=true",
        ]
      }

      env {
        KC_HTTP_HOST = "${NOMAD_IP_http}"
        KC_HTTP_PORT = "${NOMAD_PORT_http}"
        KC_DB_URL    = "jdbc:postgresql://postgresql.service.consul:5432/keycloak"
        KC_PROXY     = "edge"
      }

      template {
        destination = "${NOMAD_SECRETS_DIR}/psql"
        env         = true
        data        = <<EOT
{{ with secret "secret/postgres/keycloak" }}
KC_DB_USERNAME = {{ .Data.username }}
KC_DB_PASSWORD = {{ .Data.password }}
{{ end }}
{{ with secret "secret/devxdao-dev/keycloak-admin" }}
KEYCLOAK_ADMIN = {{ .Data.username }}
KEYCLOAK_ADMIN_PASSWORD = {{ .Data.password }}
{{ end }}
EOT
      }

      resources {
        memory = 1024
      }
    }

    task "filebeat" {
      driver = "docker"
      config {
        image        = "ghcr.io/noumenadigital/filebeat:1.0.3"
        network_mode = "host"
        args         = [
          "keycloak",
          "wildfly",
        ]
      }
      resources {
        memory = 50
      }
    }
  }

  vault {
    policies = [
      "reader",
    ]
  }
}
