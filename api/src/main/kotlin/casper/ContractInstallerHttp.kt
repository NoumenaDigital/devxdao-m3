package casper

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.auto

data class InstallCargo(var name: String, var symbol: String, val gas: String = "165000000000")

/**
 * ContractInstallerHttp class:
 * - is an HTTP wrapper for Casper smart contract installer
 */
class ContractInstallerHttp(var contractInstaller: ContractInstaller = ContractInstallerImpl()) {

    private val installCargoLens = Body.auto<InstallCargo>().toLens()

    fun installContract(): HttpHandler {
        return { req ->
            val args = installCargoLens(req)
            Response(Status.OK).body(contractInstaller.installContract(args.name, args.symbol, args.gas))
        }
    }
}
