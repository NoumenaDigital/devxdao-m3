package casper

import dx.objectMapper
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class ContractClientHttpTest {

    @Test
    fun mintCargoJsonStringMatchesExpected() {
        val actual = objectMapper.writeValueAsString(
            MintCargo(
                contractNamedKey = "devxdao",
                gas = "100000000",
                tokenOwner = "de94bf0ac8be8008b1261bc74557d20790cf2fe7b95e42924485b44a84a3dc67",
                tokenId = "dfb58ce4-c9fb-4ddb-978e-36c5a7bea978",
                tokenMetaData = TokenMetaData("INITIAL")
            )
        )

        val expected =
            """
                {
                  "contractNamedKey": "devxdao",
                  "gas": "100000000",
                  "tokenOwner": "de94bf0ac8be8008b1261bc74557d20790cf2fe7b95e42924485b44a84a3dc67",
                  "tokenId": "dfb58ce4-c9fb-4ddb-978e-36c5a7bea978",
                  "tokenMetaData": {
                    "type": "INITIAL"
                  }
                }
            """

        JSONAssert.assertEquals(expected, actual, true)
    }

    @Test
    fun burnCargoJsonStringMatchesExpected() {
        val actual = objectMapper.writeValueAsString(
            BurnCargo(
                contractNamedKey = "devxdao",
                gas = "100000000",
                tokenId = "dfb58ce4-c9fb-4ddb-978e-36c5a7bea978"
            )
        )

        val expected =
            """
                {
                  "contractNamedKey": "devxdao",
                  "gas": "100000000",
                  "tokenId": "dfb58ce4-c9fb-4ddb-978e-36c5a7bea978"
                }
            """

        JSONAssert.assertEquals(expected, actual, true)
    }

    @Test
    fun transferFromCargoJsonStringMatchesExpected() {
        val actual = objectMapper.writeValueAsString(
            TransferFromCargo(
                contractNamedKey = "devxdao",
                gas = "100000000",
                tokenId = "dfb58ce4-c9fb-4ddb-978e-36c5a7bea978",
                from = "source",
                to = "destination"
            )
        )

        val expected =
            """
                {
                  "contractNamedKey": "devxdao",
                  "gas": "100000000",
                  "tokenId": "dfb58ce4-c9fb-4ddb-978e-36c5a7bea978",
                  "from": "source",
                  "to": "destination"
                }
            """

        JSONAssert.assertEquals(expected, actual, true)
    }

    @Test
    fun addressWhiteListCargoJsonStringMatchesExpected() {
        val actual = objectMapper.writeValueAsString(
            AddressWhiteListCargo(
                contractNamedKey = "devxdao",
                gas = "100000000",
                address = "account_address"
            )
        )

        val expected =
            """
                {
                  "contractNamedKey": "devxdao",
                  "gas": "100000000",
                  "address": "account_address"
                }
            """

        JSONAssert.assertEquals(expected, actual, true)
    }
}
