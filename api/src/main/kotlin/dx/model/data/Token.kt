package dx.model.data

import arrow.core.getOrHandle
import casper.mintedTokens
import com.noumenadigital.npl.api.generated.platform.financialInstruments.TokenProxy
import com.noumenadigital.platform.engine.client.EngineClientWriter
import dx.authProvider
import dx.protocolId
import dx.rePlatform

/**
 * Token class:
 * - is a convenience class providing interface for NPL implementation of the Token
 */
class Token(client: EngineClientWriter) {

    private val proxy = TokenProxy(client)

    /**
     * setTokenId needs to be invoked in order to set the Token id in the Platform
     */
    fun setTokenId(id: String, hash: String) {
        proxy.setBcId(
            protocolId = protocolId(id),
            hash,
            authorizationProvider = authProvider(rePlatform)
        ).getOrHandle { throw it }.result
    }

    /**
     * onChainTransfer is used to register in the Platform the token transfer done the Casper blockchain
     */
    fun onChainTransfer(tokenId: String, to: String) {
        val tokenProtocolId = mintedTokens[tokenId]!!
        proxy.onChainTransfer(
            protocolId = protocolId(tokenProtocolId),
            to,
            authorizationProvider = authProvider(rePlatform)
        ).getOrHandle { throw it }.result
    }
}
