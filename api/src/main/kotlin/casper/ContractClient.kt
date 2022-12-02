package casper

import com.casper.sdk.CasperSdk
import com.casper.sdk.exceptions.ValueNotFoundException
import com.casper.sdk.service.serialization.cltypes.CLValueBuilder
import com.casper.sdk.service.serialization.util.ByteUtils
import com.casper.sdk.types.ContractHash
import com.casper.sdk.types.DeployNamedArgBuilder
import com.casper.sdk.types.DeployParams
import com.casper.sdk.types.StoredContractByHash
import com.fasterxml.jackson.databind.ObjectMapper
import dx.CasperConfig
import java.math.BigInteger
import java.security.PublicKey

data class TokenMetaData(val type: String)

interface ContractClient {
    fun mint(
        contractNamedKey: String,
        gas: String = "500000000",
        tokenOwner: String,
        tokenHash: String,
        tokenMetaData: TokenMetaData
    ): String

    fun burn(contractNamedKey: String, gas: String = "500000000", tokenId: String): String
    fun transferFrom(
        contractNamedKey: String,
        gas: String = "1000000000",
        tokenHash: String,
        from: String,
        to: String
    ): String

    fun addAddressToWhiteList(contractNamedKey: String, gas: String = "90000000", address: String): String
    fun removeAddressFromWhiteList(contractNamedKey: String, gas: String = "90000000", address: String): String
}

class ContractClientImpl(
    private val casperConfig: CasperConfig = CasperConfig(),
    private val casperSdk: CasperSdk = CasperSdk(casperConfig.casperNodeURL, casperConfig.casperNodePort),
    private val objectMapper: ObjectMapper = ObjectMapper()
) : ContractClient {

    private fun getContractHash(contractNamedKey: String = "nft_contract", accountKey: PublicKey): ContractHash {
        val accountInfo = casperSdk.getAccountInfo(accountKey)

        val map = objectMapper.reader().readValue(accountInfo, Map::class.java)

        val storedValue = map["stored_value"] as Map<*, *>
        val account = storedValue["Account"] as Map<*, *>
        val namedKeys = account["named_keys"] as List<*>
        val namedKey = namedKeys.first { item -> (item as Map<*, *>)["name"] == contractNamedKey }

        if (namedKey is Map<*, *>) {
            val contractHash = namedKey["key"] as String
            if (contractHash.length > 5) {
                return ContractHash(ByteUtils.decodeHex(contractHash.substring(5)))
            }
        }
        throw ValueNotFoundException("'$contractNamedKey' not found in account info 'named_keys'")
    }

    private fun getDeployParams(publicKey: PublicKey) = DeployParams(
        publicKey,
        casperConfig.chainName,
        null,
        null,
        null,
        null
    )

    private fun getKeys() = casperSdk.loadKeyPair(
        this::class.java.getResourceAsStream(casperConfig.publicKeyPath),
        this::class.java.getResourceAsStream(casperConfig.privateKeyPath)
    )

    override fun mint(contractNamedKey: String, gas: String, tokenOwner: String, tokenHash: String, tokenMetaData: TokenMetaData): String {
        val operatorKeyPair = getKeys()
        val contractHash = getContractHash(contractNamedKey, operatorKeyPair.public)
        val deployParams = getDeployParams(operatorKeyPair.public)
        val payment = casperSdk.standardPayment(BigInteger(gas))
        val tokenMetaDataAsJson = objectMapper.writeValueAsString(tokenMetaData)

        val deploy = casperSdk.makeDeploy(
            deployParams,
            StoredContractByHash(
                contractHash,
                "mint",
                DeployNamedArgBuilder()
                    .add(
                        "token_owner",
                        CLValueBuilder.accountKey(ByteUtils.decodeHex(tokenOwner))
                    )
                    .add("token_meta_data", CLValueBuilder.string(tokenMetaDataAsJson))
                    .add("token_id_hash", CLValueBuilder.string(tokenHash))
                    .build()
            ),
            payment
        )

        casperSdk.signDeploy(deploy, operatorKeyPair)

        val response = casperSdk.putDeploy(deploy)

        return response.toString()
    }

    override fun burn(contractNamedKey: String, gas: String, tokenId: String): String {
        val operatorKeyPair = getKeys()
        val contractHash = getContractHash(contractNamedKey, operatorKeyPair.public)
        val deployParams = getDeployParams(operatorKeyPair.public)
        val payment = casperSdk.standardPayment(BigInteger(gas))

        val deploy = casperSdk.makeDeploy(
            deployParams,
            StoredContractByHash(
                contractHash,
                "burn",
                DeployNamedArgBuilder()
                    .add("token_id", CLValueBuilder.u64(tokenId))
                    .build()
            ),
            payment
        )

        casperSdk.signDeploy(deploy, operatorKeyPair)

        val response = casperSdk.putDeploy(deploy)

        return response.toString()
    }

    override fun transferFrom(
        contractNamedKey: String,
        gas: String,
        tokenHash: String,
        from: String,
        to: String
    ): String {
        val operatorKeyPair = getKeys()
        val contractHash = getContractHash(contractNamedKey, operatorKeyPair.public)
        val deployParams = getDeployParams(operatorKeyPair.public)
        val payment = casperSdk.standardPayment(BigInteger(gas))

        val deploy = casperSdk.makeDeploy(
            deployParams,
            StoredContractByHash(
                contractHash,
                "transfer",
                DeployNamedArgBuilder()
                    .add("token_hash", CLValueBuilder.string(tokenHash))
                    .add("source_key", CLValueBuilder.accountKey(ByteUtils.decodeHex(from)))
                    .add("target_key", CLValueBuilder.accountKey(ByteUtils.decodeHex(to)))
                    .build()
            ),
            payment
        )

        casperSdk.signDeploy(deploy, operatorKeyPair)

        val response = casperSdk.putDeploy(deploy)

        return response.toString()
    }

    override fun addAddressToWhiteList(contractNamedKey: String, gas: String, address: String): String {
        val operatorKeyPair = getKeys()
        val contractHash = getContractHash(contractNamedKey, operatorKeyPair.public)
        val deployParams = getDeployParams(operatorKeyPair.public)
        val payment = casperSdk.standardPayment(BigInteger(gas))

        val deploy = casperSdk.makeDeploy(
            deployParams,
            StoredContractByHash(
                contractHash,
                "add_to_account_whitelist",
                DeployNamedArgBuilder()
                    .add("account_whitelist", CLValueBuilder.accountKey(ByteUtils.decodeHex(address)))
                    .build()
            ),
            payment
        )

        casperSdk.signDeploy(deploy, operatorKeyPair)

        val response = casperSdk.putDeploy(deploy)

        return response.toString()
    }

    override fun removeAddressFromWhiteList(contractNamedKey: String, gas: String, address: String): String {
        val operatorKeyPair = getKeys()
        val contractHash = getContractHash(contractNamedKey, operatorKeyPair.public)
        val deployParams = getDeployParams(operatorKeyPair.public)
        val payment = casperSdk.standardPayment(BigInteger(gas))

        val deploy = casperSdk.makeDeploy(
            deployParams,
            StoredContractByHash(
                contractHash,
                "remove_from_account_whitelist",
                DeployNamedArgBuilder()
                    .add("account_whitelist", CLValueBuilder.accountKey(ByteUtils.decodeHex(address)))
                    .build()
            ),
            payment
        )

        casperSdk.signDeploy(deploy, operatorKeyPair)

        val response = casperSdk.putDeploy(deploy)

        return response.toString()
    }
}
