package dx

import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DemoIT {

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = System.getProperty("rest-assured.baseURI") ?: "http://localhost:9000"
    }

    @Test
    fun `shares scenario`() {
        createDemo()

        initialiseSseListener()
    }
}
