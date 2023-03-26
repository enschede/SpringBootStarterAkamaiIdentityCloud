package nl.marcenschede.starters.akamaiidentitycloud.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
open class Account(
    var id: String? = null,
    var uuid: UUID? = null,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    var created: OffsetDateTime? = null,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    var lastUpdated: OffsetDateTime? = null,
)

class CustomAkamaiDateTimeDeserializer : StdScalarDeserializer<OffsetDateTime>(OffsetDateTime::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): OffsetDateTime? {

        val dateTimeString =
            p?.text?.trim()
                ?.replaceFirst(" ", "T")
                ?.replaceFirst(" ", "")
                ?.replace("([+-]\\d\\d)(\\d\\d)".toRegex(), "$1:$2")

        return dateTimeString?.let {
            OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
    }

}

class CustomAkamaiDateTimeSerializer : StdScalarSerializer<OffsetDateTime>(OffsetDateTime::class.java) {
    override fun serialize(value: OffsetDateTime?, gen: JsonGenerator?, provider: SerializerProvider?) {
        TODO("Not yet implemented")
    }

}
