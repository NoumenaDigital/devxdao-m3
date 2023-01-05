package dx.sse

import casper.ContractClientImpl
import com.noumenadigital.npl.api.generated.platform.notifications.AddWhiteListedInvestorFacade
import com.noumenadigital.platform.engine.values.ClientFluxNotification
import com.noumenadigital.platform.engine.values.ClientTextValue
import mu.KotlinLogging
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

/**
 * AddWhiteListedInvestorEventConsumer class:
 * - is SSE event consumer
 * - listens on event emitted on adding investors to whitelist
 * - triggers the addition of the investor to the Casper smart contract
 */
class AddWhiteListedInvestorEventConsumer : Consumer<ClientFluxNotification> {
    private val contractClient = ContractClientImpl()
    override fun accept(flux: ClientFluxNotification) {
        val payload = flux.payload
        if (payload.name == AddWhiteListedInvestorFacade.typeName && payload.arguments.isNotEmpty()) {
            val contractHash = (payload.arguments[0] as ClientTextValue).value // Always a first argument as defined by NPL notification
            val accountHash = (payload.arguments[1] as ClientTextValue).value // Always second argument

            try {
                contractClient.addAddressToWhiteList("nft_contract", "3000000000", accountHash)

                logger.info { "Add whitelisted account, payload=$payload, accountHash=$accountHash, contractHash=$contractHash" }
            } catch (error: Exception) {
                logger.error { "Error during adding to whitelist: ${error.message}" }
            }
        }
    }
}
