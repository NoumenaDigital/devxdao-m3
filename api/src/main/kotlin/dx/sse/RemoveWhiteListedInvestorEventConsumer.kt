package dx.sse

import casper.ContractClientImpl
import com.noumenadigital.npl.api.generated.platform.notifications.RemoveWhiteListedInvestorFacade
import com.noumenadigital.platform.engine.values.ClientFluxNotification
import com.noumenadigital.platform.engine.values.ClientTextValue
import mu.KotlinLogging
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

class RemoveWhiteListedInvestorEventConsumer : Consumer<ClientFluxNotification> {
    private val contractClient = ContractClientImpl()
    override fun accept(flux: ClientFluxNotification) {
        val payload = flux.payload
        if (payload.name == RemoveWhiteListedInvestorFacade.typeName && payload.arguments.isNotEmpty()) {
            val contractHash = (payload.arguments[0] as ClientTextValue).value
            val accountHash = (payload.arguments[1] as ClientTextValue).value

            try {
                contractClient.removeAddressFromWhiteList("nft_contract", "5000000000", accountHash)

                logger.info { "Remove whitelisted account, payload=$payload, accountHash=$accountHash, contractHash=$contractHash" }
            } catch (error: Exception) {
                logger.error { "Error during removing from whitelist: ${error.message}" }
            }
        }
    }
}
