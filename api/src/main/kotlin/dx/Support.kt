package dx

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.lens.uuid
import java.util.UUID

@JsonInclude(Include.NON_NULL)
data class Payload(
    val protocolId: UUID?,
    val value: String? = null
)

val lens = Body.auto<Payload>().toLens()
val protocolId = Path.uuid().of("protocolId")
