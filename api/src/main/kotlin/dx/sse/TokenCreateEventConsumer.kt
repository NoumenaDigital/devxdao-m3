package dx.sse

import casper.ContractClientImpl
import casper.TokenData
import casper.TokenMetaData
import casper.deploysTokens
import com.noumenadigital.npl.api.generated.platform.notifications.CreateTokenFacade
import com.noumenadigital.platform.engine.values.ClientFluxNotification
import com.noumenadigital.platform.engine.values.ClientProtocolReferenceValue
import com.noumenadigital.platform.engine.values.ClientTextValue
import mu.KotlinLogging
import java.util.UUID
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

/**
 * TokenCreateEventConsumer class:
 * - is SSE event consumer
 * - listens on event emitted on token creation in the NPL Engine
 * - triggers the token minting on the Casper blockchain
 */
class TokenCreateEventConsumer : Consumer<ClientFluxNotification> {
    private val contractClient = ContractClientImpl()
    override fun accept(flux: ClientFluxNotification) {
        val payload = flux.payload
        if (payload.name == CreateTokenFacade.typeName && payload.arguments.isNotEmpty()) {
            val accountHash = (payload.arguments[1] as ClientTextValue).value // Always a second argument as defined by NPL notification
            val tokenProtocolId = (payload.arguments[2] as ClientProtocolReferenceValue).value // Always third argument

            val tokenId = UUID.randomUUID().toString() // Generate token UUID used as token identifier for the Casper blockchain

            try {
                val deployHash = contractClient.mint(
                    "nft_contract",
                    "10000000000",
                    accountHash,
                    tokenId,
                    TokenMetaData("{\"type\":\"INITIAL\"}")
                )

                // In order to identify accepted and processed deploys, store the deploy hash
                deploysTokens[deployHash] = TokenData(
                    tokenId = tokenId,
                    protocolId = tokenProtocolId.toString()
                )

                logger.info { "Token create, payload=$payload, accountHash=$accountHash, tokenId=$tokenProtocolId, deployHash=$deployHash" }
            } catch (error: Exception) {
                logger.error { "Error during token creation: ${error.message}" }
            }
        }
    }
}
