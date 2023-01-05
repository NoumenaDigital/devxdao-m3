package casper

import com.casper.sdk.types.Deploy
import com.casper.sdk.types.StoredContractByHash
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.noumenadigital.platform.engine.client.EngineClientWriter
import dx.CasperConfig
import dx.model.data.Token
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * JSON object for DeployAccepted Casper event
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeployAccepted(
    var hash: String? = null,
    var account: String? = null
) {
    @JsonProperty("DeployAccepted")
    private fun unpackNested(deployAccepted: Map<String, Any>) {
        hash = deployAccepted["hash"] as String?
        val accountTemp = deployAccepted["header"] as Map<*, *>?
        account = accountTemp!!["account"] as String
    }
}

/**
 * JSON object for DeployProcessed Casper event
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeployProcessed(
    var deploy_hash: String? = null,
    var account: String? = null,
    var executionResult: Boolean = false
) {
    @JsonProperty("DeployProcessed", required = true)
    private fun unpackNested(deployProcessed: Map<String, Any>) {
        deploy_hash = deployProcessed["deploy_hash"] as String?
        account = deployProcessed["account"] as String?

        @Suppress("UNCHECKED_CAST")
        val executionResultTemp = deployProcessed["execution_result"] as Map<String, String>

        @Suppress("UNCHECKED_CAST")
        val otherTemp = executionResultTemp["Success"] as Map<String, String>?
        if (otherTemp != null) {
            executionResult = true
        }
    }
}

data class DeployTransfer(
    val tokenId: String,
    val fromAccount: String,
    val toAccount: String
)

data class TokenData(
    val protocolId: String,
    val tokenId: String
)

// Helper collections
val deploysTransfers: ConcurrentHashMap<String, DeployTransfer> = ConcurrentHashMap()
val deploysTokens: ConcurrentHashMap<String, TokenData> = ConcurrentHashMap()
val mintedTokens: ConcurrentHashMap<String, String> = ConcurrentHashMap()

/**
 * ContractEventConsumer class:
 * - is a Casper events consumer
 */
class ContractEventConsumer(
    client: EngineClientWriter,
    private val jsonDecoderMaxBufferSize: Int = 8 * 1024 * 1024, // 8MB
    private val casperConfig: CasperConfig = CasperConfig()
) {
    private val webClient = WebClient.builder()
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs { c ->
                    c.defaultCodecs().maxInMemorySize(jsonDecoderMaxBufferSize)
                }
                .build()
        )
        .baseUrl("${casperConfig.casperNodeURL}:${casperConfig.casperEventPort}/events")
        .build()

    private val eventsDeploy = webClient.get()
        .uri("/deploys")
        .accept(MediaType.TEXT_EVENT_STREAM)
        .retrieve()
        .bodyToFlux(String::class.java)

    private val eventsMain = webClient.get()
        .uri("/main")
        .accept(MediaType.TEXT_EVENT_STREAM)
        .retrieve()
        .bodyToFlux(String::class.java)

    private val token = Token(client)

    // Helper method for extracting the Casper account address from the Casper network events
    private fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    /**
     * consumeDeployAccepted:
     * - listens on DeployAccepted events
     * - registers the accepted token transfers and account addresses to be used for DeployProcessed events
     */
    fun consumeDeployAccepted() {
        eventsDeploy.subscribe(
            { content ->
                val objectMapper = ObjectMapper()

                if (content.contains("DeployAccepted")) { // Process only DeployAccepted events
                    val deploy = objectMapper.reader()
                        .readValue(content, Deploy::class.java)
                    logger.info { "Deploy accepted: $deploy" }

                    // Search for transfers
                    try {
                        val session = deploy.getSession() as StoredContractByHash
                        if (session.entryPoint != "transfer") {
                            logger.info { "Deploy accepted: Not a transfer entrypoint" }
                            throw RuntimeException()
                        }
                        val args = session.args
                        val deployHash = deploy.hash
                        val deployTransfer = DeployTransfer(
                            (args[0].value.parsed as BigInteger).toString(), // Token id
                            args[1].value.bytes.toHex().drop(2), // Source account
                            args[2].value.bytes.toHex().drop(2) // Destination account
                        )

                        // Store the transfer for this deploy
                        deploysTransfers[deployHash.toString()] = deployTransfer
                    } catch (error: Exception) {
                        logger.info { "Not a StoredContractByHash or wrong endpoint called" }
                    }
                }
            },
            { error: Throwable? -> logger.error { "Error receiving SSE: $error" } },
            { logger.info { "Completed!!!" } }
        )
    }

    /**
     * consumeDeployProcessed:
     * - listens on DeployProcessed events
     * - dispatches DeployProcessed events into token creation and token transfer
     * - notifies the NPL Platform on token creation and token transfer
     */
    fun consumeDeployProcessed() {
        eventsMain.subscribe(
            { content ->
                val jacksonMapper = jacksonObjectMapper()

                if (content.contains("DeployProcessed")) { // Process only DeployProcessed events
                    val deploy = jacksonMapper.readValue(content, DeployProcessed::class.java)
                    val deployHash = deploy.deploy_hash

                    logger.info { "Deploy processed, deploy hash: $deployHash" }

                    // on transfer processed
                    val deployData = deploysTransfers[deployHash]
                    if (deployData != null) {
                        logger.info { "Processed deploy transfer: $deployHash" }
                        token.onChainTransfer(deployData.tokenId, deployData.toAccount)
                        deploysTransfers.remove(deployHash)
                    }

                    // on token create
                    val deployHashTokenEvent = deploysTokens[deployHash]
                    if (deployHashTokenEvent != null) {
                        logger.info { "Processed deployTokensEvent $deployHash" }
                        mintedTokens[deployHashTokenEvent.tokenId] = deployHashTokenEvent.protocolId
                        token.setTokenId(deployHashTokenEvent.protocolId, deployHashTokenEvent.tokenId)
                    }
                }
            },
            { error: Throwable? -> logger.error { "Error receiving SSE: $error" } },
            { logger.info { "Completed!!!" } }
        )
    }
}
