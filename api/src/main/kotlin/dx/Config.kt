package dx

import com.noumenadigital.platform.engine.client.AuthConfig
import com.noumenadigital.platform.engine.client.TokenAuthorizationProvider
import com.noumenadigital.platform.engine.client.UserConfig

const val ISSUER = "dxuser1"

const val pinvestor1 = "investor1"
const val pinvestor2 = "investor2"
const val pinvestor3 = "investor3"
const val pinvestor4 = "investor4"
const val rePlatform = "rePlatform"

val engineURL: String = System.getenv("ENGINE_URL") ?: "http://localhost:12000"
val keycloakURL: String = System.getenv("KEYCLOAK_URL") ?: "http://localhost:11000"

// CASPER
data class CasperConfig(
    val casperNodeURL: String = System.getenv("CASPER_NODE_URL") ?: "http://3.140.179.157",
    val casperNodePort: Int = (System.getenv("CASPER_NODE_PORT") ?: "7777").toInt(),
    val casperEventPort: Int = (System.getenv("CASPER_EVENT_PORT") ?: "9999").toInt(),
    val chainName: String = (System.getenv("CHAIN_NAME")) ?: "integration-test",
    val wasmPath: String = (System.getenv("WASM_PATH")) ?: "/contract.wasm",
    val privateKeyPath: String = (System.getenv("PRIVATE_KEY_PATH")) ?: "/secret_key.pem",
    val publicKeyPath: String = (System.getenv("PUBLIC_KEY_PATH")) ?: "/public_key.pem"
)

private val authConfig = AuthConfig(
    realm = "noumena",
    authUrl = keycloakURL,
    clientId = "nm-platform-service-client",
    clientSecret = "87ff12ca-cf29-4719-bda8-c92faa78e3c4"
)

fun authProvider(username: String = ISSUER) =
    TokenAuthorizationProvider(
        UserConfig(username, "welcome"),
        authConfig
    )
