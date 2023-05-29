package nl.marcenschede.starters.akamaiidentitycloud.account

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

val isoOffsetDateTime: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .parseLenient()
    .appendOffset("+HH:MM", "Z")
    .toFormatter()

class CustomAkamaiDateTimeDeserializer : StdScalarDeserializer<OffsetDateTime>(OffsetDateTime::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): OffsetDateTime? {

        return p?.text?.trim()?.let {
            OffsetDateTime.parse(it, isoOffsetDateTime)
        }
    }
}

class CustomAkamaiDateTimeSerializer : StdScalarSerializer<OffsetDateTime>(OffsetDateTime::class.java) {
    override fun serialize(value: OffsetDateTime?, gen: JsonGenerator?, provider: SerializerProvider?) {

        gen?.writeString(isoOffsetDateTime.format(value))
    }

}
