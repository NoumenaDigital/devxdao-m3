job "api" {
  datacenters = [
    "[[ .datacenter ]]",
  ]

  constraint {
    attribute = "${node.class}"
    value     = "worker"
  }

  type = "service"

  update {
    min_healthy_time = "20s"
    auto_revert      = true
    max_parallel     = 1
  }

  group "api" {
    count = 1

    restart {
      attempts = 3
    }

    task "api-service" {
      leader = true
      driver = "docker"
      config {
        image        = "ghcr.io/noumenadigital/replatform/api:[[ .version ]]"
        network_mode = "host"
      }
      env {
        KEYCLOAK_URL             = "[[ .KEYCLOAK_URL ]]"
        KEYCLOAK_HOST            = "[[ .KEYCLOAK_HOST ]]"
        KEYCLOAK_REALM           = "[[ .KEYCLOAK_REALM ]]"
        VAULT_ENDPOINT           = "[[ .VAULT_ENDPOINT ]]"
        ENGINE_URL               = "[[ .ENGINE_URL]]"
        LOG_LEVEL                = "[[ .LOG_LEVEL ]]"
        CHAIN_NAME               = "[[ .CHAIN_NAME ]]"
      }

      resources {
        memory = 768
        network {
          port "HTTP_PORT" {
            static = 9000
          }
        }
      }

      service {
        name = "api"
        port = "HTTP_PORT"
        tags = [
          "version=[[ .version ]]",
          "traefik.enable=true",
          "traefik.frontend.rule=Host:api.[[ .domain ]];PathPrefix:/",
          "traefik.frontend.entryPoints=[[ .entrypoint ]]",
        ]
      }
    }

    task "filebeat" {
      driver = "docker"
      config {
        image        = "ghcr.io/noumenadigital/filebeat:1.0.3"
        network_mode = "host"
        args         = [
          "api",
          "java",
        ]
      }
      resources {
        memory = 150
      }
    }
  }

  vault {
    policies = [
      "reader",
    ]
  }
}
