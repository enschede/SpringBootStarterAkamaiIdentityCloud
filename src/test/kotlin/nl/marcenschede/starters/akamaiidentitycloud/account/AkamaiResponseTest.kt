package nl.marcenschede.starters.akamaiidentitycloud.account

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class AkamaiResponseTest {
    val SEPT_16_2021 = OffsetDateTime.of(LocalDateTime.of(2020, 9, 16, 0, 0), ZoneOffset.ofHoursMinutes(2, 0))

    val mapper = ObjectMapper().apply {
        registerModule(kotlinModule())
        registerModule(Jdk8Module())
        registerModule(JavaTimeModule())
        registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))

        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Test
    fun `when stat not ok then the error is decoded`() {

        val singleElementDecoder: ((String) -> SingleAccountResponse) = {
            mapper.readValue(it, SingleAccountResponse::class.java)
        }

        val response = singleElementDecoder.invoke(
            """
            {"stat":"error", "error":"my error", "error_description": "description"}
        """.trimIndent()
        )

        assertThat(response.stat).isEqualTo("error")
        assertThat(response.error).isEqualTo("my error")
        assertThat(response.errorDescription).isEqualTo("description")
    }

    @Test
    fun `when stat ok then the account is decoded`() {

        val singleElementDecoder: ((String) -> SingleAccountResponse) = {
            mapper.readValue(it, SingleAccountResponse::class.java)
        }

        val response = singleElementDecoder.invoke(
            """
            {
                "stat":"ok",
                "result": {
                    "id": "12345",
                    "uuid": "7ba35d0a-f532-40cd-b91e-5c1452b59151",
                    "created": "2020-09-16 00:00:00+02:00",
                    "lastUpdated": "2020-09-16 00:00:00+02:00"
                }
            }
        """.trimIndent()
        )

        assertThat(response.stat).isEqualTo("ok")
        assertThat(response.error).isNull()
        assertThat(response.errorDescription).isNull()

        assertThat(response.result?.uuid).isEqualTo(UUID.fromString("7ba35d0a-f532-40cd-b91e-5c1452b59151"))
        assertThat(response.result?.created).isEqualTo(SEPT_16_2021)
        assertThat(response.result?.lastUpdated).isEqualTo(SEPT_16_2021)
    }

    @Test
    fun `when stat ok in extended account then the account is decoded`() {

        val singleElementDecoder: ((String) -> SingleAccountResponse) = {
            mapper.readValue(it, SingleExtendedAccountResponse::class.java)
        }

        val response = singleElementDecoder.invoke(
            """
            {
                "stat":"ok",
                "result": {
                    "id": "12345",
                    "uuid": "7ba35d0a-f532-40cd-b91e-5c1452b59151",
                    "created": "2020-09-16 00:00:00+02:00",
                    "lastUpdated": "2020-09-16 00:00:00+02:00",
                    "email": "marc@marc.com",
                    "emailVerified": "2020-09-16 00:00:00+02:00",
                    "mobileNumber": "+3161234527",
                    "mobileNumberVerified": "2020-09-16 00:00:00+02:00"
                }
            }
        """.trimIndent()
        ) as SingleExtendedAccountResponse

        assertThat(response.stat).isEqualTo("ok")
        assertThat(response.error).isNull()
        assertThat(response.errorDescription).isNull()
        assertThat(response.result?.uuid).isEqualTo(UUID.fromString("7ba35d0a-f532-40cd-b91e-5c1452b59151"))
        assertThat(response.result?.created).isEqualTo(SEPT_16_2021)
        assertThat(response.result?.lastUpdated).isEqualTo(SEPT_16_2021)
        assertThat(response.result?.email).isEqualTo("marc@marc.com")
        assertThat(response.result?.emailVerified).isEqualTo(SEPT_16_2021)
        assertThat(response.result?.mobileNumber).isEqualTo("+3161234527")
        assertThat(response.result?.mobileNumberVerified).isEqualTo(SEPT_16_2021)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class SingleExtendedAccountResponse(
        override val result: ExtendedAccount? = null,
    ) : SingleAccountResponse()


    class ExtendedAccount(
        id: String,
        uuid: UUID,
        @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
        @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
        created: OffsetDateTime,
        @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
        @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
        lastUpdated: OffsetDateTime,
        var email: String? = null,
        @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
        @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
        var emailVerified: OffsetDateTime? = null,
        var mobileNumber: String? = null,
        @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
        @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
        var mobileNumberVerified: OffsetDateTime? = null
    ) : BaseAccount(id, uuid, created, lastUpdated)

}