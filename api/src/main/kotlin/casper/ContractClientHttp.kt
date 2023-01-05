package casper

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.auto

data class MintCargo(
    val contractNamedKey: String,
    val gas: String = "500000000",
    var tokenOwner: String,
    var tokenId: String,
    var tokenMetaData: TokenMetaData
)

data class BurnCargo(
    val contractNamedKey: String,
    val gas: String = "500000000",
    val tokenId: String
)

data class TransferFromCargo(
    val contractNamedKey: String,
    val gas: String = "1000000000",
    val tokenId: String,
    val from: String,
    val to: String
)

data class AddressWhiteListCargo(
    val contractNamedKey: String,
    val gas: String = "90000000",
    val address: String
)

/**
 * ContractClientHttp class:
 * - is an HTTP wrapper for Casper smart contract interactions
 */
class ContractClientHttp(var client: ContractClient = ContractClientImpl()) {

    private val mintCargoLens = Body.auto<MintCargo>().toLens()
    private val burnLens = Body.auto<BurnCargo>().toLens()
    private val transferFromLens = Body.auto<TransferFromCargo>().toLens()
    private val addressWhiteListLens = Body.auto<AddressWhiteListCargo>().toLens()

    fun mint(): HttpHandler {
        return { req ->
            val args = mintCargoLens(req)
            Response(Status.OK).body(client.mint(args.contractNamedKey, args.gas, args.tokenOwner, args.tokenId, args.tokenMetaData))
        }
    }

    fun burn(): HttpHandler {
        return { req ->
            val args = burnLens(req)
            Response(Status.OK).body(client.burn(args.contractNamedKey, args.gas, args.tokenId))
        }
    }

    fun transferFrom(): HttpHandler {
        return { req ->
            val args = transferFromLens(req)
            Response(Status.OK).body(
                client.transferFrom(
                    args.contractNamedKey,
                    args.gas,
                    args.tokenId,
                    args.from,
                    args.to
                )
            )
        }
    }

    fun addAddressToWhiteList(): HttpHandler {
        return { req ->
            val args = addressWhiteListLens(req)
            Response(Status.OK).body(client.addAddressToWhiteList(args.contractNamedKey, args.gas, args.address))
        }
    }

    fun removeAddressFromWhiteList(): HttpHandler {
        return { req ->
            val args = addressWhiteListLens(req)
            Response(Status.OK).body(client.removeAddressFromWhiteList(args.contractNamedKey, args.gas, args.address))
        }
    }
}
