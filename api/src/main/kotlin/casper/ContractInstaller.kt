package casper

import com.casper.sdk.CasperSdk
import com.casper.sdk.service.serialization.cltypes.CLValueBuilder
import com.casper.sdk.types.DeployNamedArgBuilder
import com.casper.sdk.types.DeployParams
import com.casper.sdk.types.ModuleBytes
import com.fasterxml.jackson.databind.ObjectMapper
import dx.CasperConfig
import org.apache.commons.io.IOUtils
import java.io.InputStream

data class Property(val name: String, val description: String, val required: Boolean)
data class Properties(val properties: Map<String, Property>)

interface ContractInstaller {
    fun installContract(name: String, symbol: String, gas: String = "165000000000"): String
}

class ContractInstallerImpl(
    private val casperConfig: CasperConfig = CasperConfig(),
    private val casperSdk: CasperSdk = CasperSdk(casperConfig.casperNodeURL, casperConfig.casperNodePort),
    private val objectMapper: ObjectMapper = ObjectMapper()
) : ContractInstaller {

    override fun installContract(name: String, symbol: String, gas: String): String {
        val contractWasm: InputStream = this::class.java.getResourceAsStream(casperConfig.wasmPath)!!
        val contractWasmByteArray = IOUtils.toByteArray(contractWasm)

        val operatorKeyPair = casperSdk.loadKeyPair(
            this::class.java.getResourceAsStream(casperConfig.publicKeyPath),
            this::class.java.getResourceAsStream(casperConfig.privateKeyPath)
        )

        val deployParams = DeployParams(
            operatorKeyPair.public,
            casperConfig.chainName,
            null,
            null,
            null,
            null
        )

        val jsonSchema = objectMapper.writeValueAsString(
            Properties(
                mapOf(
                    Pair("type", Property("type", "How token is created: INITIAL, SPLIT or MERGED", true))
                )
            )
        )

        val deploy = casperSdk.makeDeploy(
            deployParams,
            ModuleBytes(
                contractWasmByteArray,
                DeployNamedArgBuilder()
                    .add("collection_name", CLValueBuilder.string(name))
                    .add("collection_symbol", CLValueBuilder.string(symbol))
                    .add("total_token_supply", CLValueBuilder.u64(1000))
                    .add("ownership_mode", CLValueBuilder.u8(2)) // Transferable
                    .add("nft_kind", CLValueBuilder.u8(2)) // Physical
                    .add("holder_mode", CLValueBuilder.u8(2)) // Mixed (Accounts and Contracts can own NFTs)
                    .add("nft_metadata_kind", CLValueBuilder.u8(3)) // CustomValidated
                    .add("json_schema", CLValueBuilder.string(jsonSchema))
                    .add("identifier_mode", CLValueBuilder.u8(1)) // Hash
                    .add("metadata_mutability", CLValueBuilder.u8(0)) // Mutable
                    .build()
            ),
            casperSdk.standardPayment(gas.toLong())
        )

        // Approve deploy.
        casperSdk.signDeploy(deploy, operatorKeyPair)

        // Dispatch deploy to a node.
        val response = casperSdk.putDeploy(deploy)

        return response.toString()
    }
}
