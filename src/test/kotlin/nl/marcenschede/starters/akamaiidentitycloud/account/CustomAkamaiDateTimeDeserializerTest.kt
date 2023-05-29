package nl.marcenschede.starters.akamaiidentitycloud.account

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class CustomAkamaiDateTimeDeserializerTest {

    @Test
    fun `OffsetDateTime deserializer test`() {
        val objectMapper = prepareObjectMapper()

        val readValue = objectMapper.readValue<BaseAccount>(
            """
            {
                "id": "12345",
                "uuid": "7ba35d0a-f532-40cd-b91e-5c1452b59151",
                "created": "2020-09-16 13:46:59+02:00",
                "lastUpdated": "2020-09-16 13:46:59+02:00"
            }
        """.trimIndent()
        )

        assertThat(readValue.id).isEqualTo("12345")
        assertThat(readValue.uuid).isEqualTo(UUID.fromString("7ba35d0a-f532-40cd-b91e-5c1452b59151"))
        assertThat(readValue.created).isEqualTo(
            OffsetDateTime.of(
                LocalDateTime.parse("2020-09-16T13:46:59"),
                ZoneOffset.ofHours(2)
            )
        )
        assertThat(readValue.lastUpdated).isEqualTo(
            OffsetDateTime.of(
                LocalDateTime.parse("2020-09-16T13:46:59"),
                ZoneOffset.ofHours(2)
            )
        )

    }

    @Test
    fun `OffsetDateTime serializer test`() {
        val objectMapper = prepareObjectMapper()

        val account = BaseAccount(
            id = "12345",
            uuid = UUID.fromString("7ba35d0a-f532-40cd-b91e-5c1452b59151"),
            created = OffsetDateTime.of(
                LocalDateTime.parse("2020-09-16T13:48:41"),
                ZoneOffset.ofHours(2),
            ),
            lastUpdated = OffsetDateTime.of(
                LocalDateTime.parse("2020-09-16T13:48:41"),
                ZoneOffset.ofHours(2),
            )
        )

        val asString = objectMapper.writeValueAsString(account)

        JSONAssert.assertEquals(
            """
            {
              "id":"12345",
              "uuid":"7ba35d0a-f532-40cd-b91e-5c1452b59151",
              "created":"2020-09-16 13:48:41+02:00",
              "lastUpdated":"2020-09-16 13:48:41+02:00"
            }
            """.trimIndent(),
            asString,
            JSONCompareMode.LENIENT
        )
    }

    private fun prepareObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper().apply {
            registerModule(kotlinModule())
            registerModule(Jdk8Module())
            registerModule(JavaTimeModule())
            registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))

            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        return objectMapper
    }

}