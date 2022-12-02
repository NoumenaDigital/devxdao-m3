package dx.model

import arrow.core.getOrHandle
import com.noumenadigital.codegen.Party
import com.noumenadigital.npl.api.generated.demo.DemoProxy
import com.noumenadigital.platform.engine.client.EngineClientWriter
import dx.Payload
import dx.authProvider
import dx.lens
import dx.pinvestor1
import dx.pinvestor2
import dx.pinvestor3
import dx.pinvestor4
import dx.protocolId
import dx.rePlatform
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto

class Demo(client: EngineClientWriter) {

    private val proxy = DemoProxy(client)
    private val setupProjectLens = Body.auto<SetupProjectCargo>().toLens()
    private val transferTokenLens = Body.auto<TransferTokenCargo>().toLens()

    fun create(): HttpHandler {
        return {
            val result = proxy.create(
                pPlatform = Party(rePlatform),
                pBank = Party(rePlatform), // rePlatform is bank here
                pInvestor1 = Party(pinvestor1),
                pInvestor2 = Party(pinvestor2),
                pInvestor3 = Party(pinvestor3),
                pInvestor4 = Party(pinvestor4),
                authorizationProvider = authProvider()
            )

            val id = result.getOrHandle { throw it }.result

            val response = Payload(id.id, "[${id.typeName}]")
            Response(Status.OK).with(lens of response)
        }
    }

    fun setupProject(): HttpHandler {
        return { req ->

            val args = setupProjectLens(req)

            proxy.setupProject(
                protocolId = protocolId(req),
                args.account1,
                args.account2,
                args.account3,
                args.account4,
                authorizationProvider = authProvider(rePlatform)
            ).getOrHandle { throw it }.result

            Response(Status.OK)
        }
    }

    fun setupInvestors(): HttpHandler {
        return { req ->

            proxy.setupInvestors(
                protocolId = protocolId(req),
                authorizationProvider = authProvider(rePlatform)
            ).getOrHandle { throw it }.result

            Response(Status.OK)
        }
    }

    fun setupToken(): HttpHandler {
        return { req ->

            proxy.setupToken(
                protocolId = protocolId(req),
                authorizationProvider = authProvider(rePlatform)
            ).getOrHandle { throw it }.result

            Response(Status.OK)
        }
    }

    fun transferToken(): HttpHandler {
        return { req ->

            val args = transferTokenLens(req)

            proxy.transferToken(
                protocolId = protocolId(req),
                args.accountBuyer,
                authorizationProvider = authProvider(rePlatform)
            ).getOrHandle { throw it }.result

            Response(Status.OK)
        }
    }
}

data class SetupProjectCargo(
    var account1: String,
    var account2: String,
    var account3: String,
    var account4: String
)

data class TransferTokenCargo(
    var accountBuyer: String
)
