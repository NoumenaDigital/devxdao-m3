package casper

import dx.objectMapper
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class ContractInstallerTest {

    @Test
    fun jsonSchemaStringMatchesObjectModel() {
        val actual = objectMapper.writeValueAsString(
            Properties(
                mapOf(
                    Pair("deity_name", Property("deity_name", "The name of deity from a particular pantheon.", true)),
                    Pair("mythology", Property("mythology", "The mythology the deity belongs to.", true))
                )
            )
        )

        val expected =
            """
                {
                  "properties": {
                    "deity_name": {
                      "name": "deity_name",
                      "description": "The name of deity from a particular pantheon.",
                      "required": true
                    },
                    "mythology": {
                      "name": "mythology",
                      "description": "The mythology the deity belongs to.",
                      "required": true
                    }
                  }
                }
            """

        JSONAssert.assertEquals(expected, actual, false)
    }
}
