job "keycloak-provisioning" {
  datacenters = [
    "[[ .datacenter ]]",
  ]

  constraint {
    attribute = "${node.class}"
    value     = "worker"
  }

  type = "batch"

  group "keycloak-provisioning" {
    task "keycloak-provisioning" {
      leader = true
      driver = "docker"
      config {
        image        = "ghcr.io/noumenadigital/replatform/keycloak-provisioning:[[ .version ]]"
        command = "/terraform/cloud.sh"
        network_mode = "host"
      }

      env {
        KEYCLOAK_URL = "[[ .KEYCLOAK_URL ]]"
      }

      template {
        destination = "${NOMAD_SECRETS_DIR}/psql"
        env         = true
        data        = <<EOT
{{ with secret "secret/devxdao-dev/keycloak-admin" }}
KEYCLOAK_USER = {{ .Data.username }}
KEYCLOAK_PASSWORD = {{ .Data.password }}
{{ end }}
EOT
      }

      resources {
        memory = 512
      }
    }

    task "filebeat" {
      driver = "docker"
      config {
        image        = "ghcr.io/noumenadigital/filebeat:1.0.3"
        network_mode = "host"
        args         = [
          "keycloak-provisioning",
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
