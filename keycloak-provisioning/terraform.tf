# reference: https://registry.terraform.io/providers/mrparkers/keycloak/latest/docs/resources/openid_client

locals {
  parties = {
    dxuser1 = "dxuser1"
    dxuser2 = "dxuser2"

    bank = "bank"
    pinvestor1 = "investor1"
    pinvestor2 = "investor2"
    pinvestor3 = "investor3"
    pinvestor4 = "investor4"
    pinvestor5 = "investor5"
    pinvestor6 = "investor6"
    pinvestor7 = "investor7"
    pinvestor8 = "investor8"
    rePlatform = "rePlatform"
    pBuyer = "buyer"
  }
}

resource "keycloak_realm" "realm" {
  realm = "noumena"
}

resource "keycloak_role" "nm_user" {
  realm_id    = keycloak_realm.realm.id
  name        = "NM_USER"
  description = "Required role for accessing the platform"
}

resource "keycloak_default_roles" "default_roles" {
  realm_id      = keycloak_realm.realm.id
  default_roles = ["offline_access", "uma_authorization", keycloak_role.nm_user.name]
}

resource "keycloak_openid_client" "client" {
  realm_id                     = keycloak_realm.realm.id
  client_id                    = "nm-platform-service-client"
  client_secret                = "87ff12ca-cf29-4719-bda8-c92faa78e3c4"
  access_type                  = "CONFIDENTIAL"
  web_origins                  = ["*"]
  valid_redirect_uris          = ["*"]
  standard_flow_enabled        = true
  direct_access_grants_enabled = true
  service_accounts_enabled     = true
  authorization {
    policy_enforcement_mode          = "ENFORCING"
    decision_strategy                = "UNANIMOUS"
    allow_remote_resource_management = true
  }
}

resource "keycloak_openid_user_attribute_protocol_mapper" "party_mapper" {
  realm_id  = keycloak_realm.realm.id
  client_id = keycloak_openid_client.client.id
  name      = "party-mapper"

  user_attribute   = "party"
  claim_name       = "party"
  claim_value_type = "JSON"
}

resource "keycloak_user" "user1" {
  realm_id = keycloak_realm.realm.id
  username = "dxuser1"
  attributes = {
    "party" = jsonencode([local.parties.dxuser1])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "user2" {
  realm_id = keycloak_realm.realm.id
  username = "dxuser2"
  attributes = {
    "party" = jsonencode([local.parties.dxuser2])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "bank" {
  realm_id = keycloak_realm.realm.id
  username = "bank"
  attributes = {
    "party" = jsonencode([local.parties.bank])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor1" {
  realm_id = keycloak_realm.realm.id
  username = "investor1"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor1])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor2" {
  realm_id = keycloak_realm.realm.id
  username = "investor2"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor2])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor3" {
  realm_id = keycloak_realm.realm.id
  username = "investor3"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor3])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor4" {
  realm_id = keycloak_realm.realm.id
  username = "investor4"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor4])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor5" {
  realm_id = keycloak_realm.realm.id
  username = "investor5"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor5])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor6" {
  realm_id = keycloak_realm.realm.id
  username = "investor6"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor6])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor7" {
  realm_id = keycloak_realm.realm.id
  username = "investor7"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor7])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "pinvestor8" {
  realm_id = keycloak_realm.realm.id
  username = "investor8"
  attributes = {
    "party" = jsonencode([local.parties.pinvestor8])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "rePlatform" {
  realm_id = keycloak_realm.realm.id
  username = "replatform"
  attributes = {
    "party" = jsonencode([local.parties.rePlatform])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}

resource "keycloak_user" "buyer" {
  realm_id = keycloak_realm.realm.id
  username = "buyer"
  attributes = {
    "party" = jsonencode([local.parties.pBuyer])
  }
  initial_password {
    value     = "welcome"
    temporary = false
  }
}
