package dx.sse

import casper.ContractInstallerImpl
import com.noumenadigital.npl.api.generated.platform.notifications.CreateProjectFacade
import com.noumenadigital.platform.engine.values.ClientFluxNotification
import com.noumenadigital.platform.engine.values.ClientProtocolReferenceValue
import mu.KotlinLogging
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

/**
 * ProjectCreateEventConsumer class:
 * - is SSE event consumer
 * - listens on project's creation
 * - triggers the contract deployment to the Casper blockchain
 */
class ProjectCreateEventConsumer : Consumer<ClientFluxNotification> {
    private val contractInstaller = ContractInstallerImpl()
    override fun accept(flux: ClientFluxNotification) {
        val payload = flux.payload
        if (payload.name == CreateProjectFacade.typeName && payload.arguments.isNotEmpty()) {
            val contractId = (payload.arguments[0] as ClientProtocolReferenceValue).value // Always a first argument as defined by NPL notification

            try {
                // User needs to check if contract has been deployed
                contractInstaller.installContract(name = "devxdao", symbol = "DXD", gas = "200000000000")

                logger.info { "Project create, payload=$payload, contractId=$contractId" }
            } catch (error: Exception) {
                logger.error { "Error during contract installing: ${error.message}" }
            }
        }
    }
}
