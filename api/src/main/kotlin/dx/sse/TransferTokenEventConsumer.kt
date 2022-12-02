package dx.sse

import casper.ContractClientImpl
import com.noumenadigital.npl.api.generated.platform.notifications.TransferTokenFacade
import com.noumenadigital.platform.engine.values.ClientFluxNotification
import com.noumenadigital.platform.engine.values.ClientTextValue
import mu.KotlinLogging
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

class TransferTokenEventConsumer : Consumer<ClientFluxNotification> {
    private val contractClient = ContractClientImpl()
    override fun accept(flux: ClientFluxNotification) {
        val payload = flux.payload
        if (payload.name == TransferTokenFacade.typeName && payload.arguments.isNotEmpty()) {
            val sourceAccountHash = (payload.arguments[0] as ClientTextValue).value
            val targetAccountHash = (payload.arguments[1] as ClientTextValue).value
            val tokenId = (payload.arguments[2] as ClientTextValue).value

            try {
                contractClient.transferFrom(
                    "nft_contract",
                    "10000000000",
                    tokenId,
                    sourceAccountHash,
                    targetAccountHash
                )

                logger.info { "Token transfer, payload=$payload, targetAccountHash=$targetAccountHash, tokenId=$tokenId" }
            } catch (error: Exception) {
                logger.error { "Error during transferring a token: ${error.message}" }
            }
        }
    }
}
