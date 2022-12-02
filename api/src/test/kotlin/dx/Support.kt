package dx

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.http.Header
import org.springframework.http.HttpStatus

val objectMapper = ObjectMapper()

fun createDemo(): Payload {
    return RestAssured.given()
        .post("/dx/demo")
        .then()
        .assertThat()
        .statusCode(HttpStatus.OK.value())
        .and()
        .extract()
        .body()
        .`as`(Payload::class.java)
}

fun initialiseSseListener() {
    RestAssured.given()
        .header(Header("Accept", "text/event-stream"))
        .get("/dx/sse/start")
}
