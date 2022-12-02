package dx.model.data

import arrow.core.getOrHandle
import casper.mintedTokens
import com.noumenadigital.npl.api.generated.platform.financialInstruments.TokenProxy
import com.noumenadigital.platform.engine.client.EngineClientWriter
import dx.authProvider
import dx.protocolId
import dx.rePlatform

class Token(client: EngineClientWriter) {

    private val proxy = TokenProxy(client)

    fun setTokenId(id: String, hash: String) {
        proxy.setBcId(
            protocolId = protocolId(id),
            hash,
            authorizationProvider = authProvider(rePlatform)
        ).getOrHandle { throw it }.result
    }

    fun onChainTransfer(tokenId: String, to: String) {
        val tokenProtocolId = mintedTokens[tokenId]!!
        proxy.onChainTransfer(
            protocolId = protocolId(tokenProtocolId),
            to,
            authorizationProvider = authProvider(rePlatform)
        ).getOrHandle { throw it }.result
    }
}
