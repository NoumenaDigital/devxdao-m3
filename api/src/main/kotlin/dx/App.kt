package dx

import casper.ContractClientHttp
import casper.ContractEventConsumer
import casper.ContractInstallerHttp
import com.noumenadigital.platform.engine.client.EngineClientApi
import com.noumenadigital.platform.engine.client.EngineClientWriter
import com.noumenadigital.platform.read.streams.client.SseReaderClient
import dx.model.Demo
import dx.sse.AddWhiteListedInvestorEventConsumer
import dx.sse.ProjectCreateEventConsumer
import dx.sse.RemoveWhiteListedInvestorEventConsumer
import dx.sse.TokenCreateEventConsumer
import dx.sse.TransferTokenEventConsumer
import mu.KotlinLogging
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.sse
import org.http4k.server.PolyHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.sse.Sse
import org.http4k.sse.SseHandler

val engineClient: EngineClientWriter = EngineClientApi(engineURL)
val sseReaderClient = SseReaderClient(engineURL)

val demo = Demo(engineClient)
val contractEventConsumer = ContractEventConsumer(engineClient)
val contractInstaller = ContractInstallerHttp()
val contractClient = ContractClientHttp()

private val logger = KotlinLogging.logger {}

val app: HttpHandler = routes(
    // Endpoints used for demo scenario
    "/dx" bind routes(
        "/demo" bind Method.POST to demo.create(),
        "/demo/{protocolId}/setupInvestors" bind Method.POST to demo.setupInvestors(),
        "/demo/{protocolId}/setupToken" bind Method.POST to demo.setupToken(),
        "/demo/{protocolId}/setupProject" bind Method.POST to demo.setupProject(),
        "/demo/{protocolId}/transferToken" bind Method.POST to demo.transferToken()
    ),
    // Endpoints used for other interactions and testing
    "/casper" bind routes(
        "/install_contract" bind Method.POST to contractInstaller.installContract(),
        "/mint" bind Method.POST to contractClient.mint(),
        "/burn" bind Method.POST to contractClient.burn(),
        "/transferFrom" bind Method.POST to contractClient.transferFrom(),
        "/addAddressToWhiteList" bind Method.POST to contractClient.addAddressToWhiteList(),
        "/removeAddressFromWhiteList" bind Method.POST to contractClient.removeAddressFromWhiteList()
    )
)

/**
 * Handler of the SSE (Server Sent Events) emitted by the Platform.
 * Is responsible for:
 * - listening on SSE events
 * - triggering necessary actions on the Casper blockchain
 */
val sse: SseHandler = sse(
    "/dx/sse/start" bind { _: Sse ->
        sseReaderClient.notifications(-1, authProvider(), false)
            .subscribe {
                TokenCreateEventConsumer().accept(it)
                TransferTokenEventConsumer().accept(it)
                ProjectCreateEventConsumer().accept(it)
                AddWhiteListedInvestorEventConsumer().accept(it)
                RemoveWhiteListedInvestorEventConsumer().accept(it)
            }
    }
)

fun main() {
    val printingApp: HttpHandler = DebuggingFilters.PrintRequest().then(app)

    val server = PolyHandler(
        http = printingApp,
        sse = sse
    ).asServer(Undertow(9000)).start()

    // The Casper blockchain events consumers
    contractEventConsumer.consumeDeployAccepted()
    contractEventConsumer.consumeDeployProcessed()

    logger.info { "Server started on " + server.port() }
}
