package nl.marcenschede.starters.akamaiidentitycloud.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nl.marcenschede.starters.akamaiidentitycloud.account.Account
import nl.marcenschede.starters.akamaiidentitycloud.account.CustomAkamaiDateTimeDeserializer
import nl.marcenschede.starters.akamaiidentitycloud.account.CustomAkamaiDateTimeSerializer
import nl.marcenschede.starters.akamaiidentitycloud.account.SingleAccountResponse
import nl.marcenschede.starters.akamaiidentitycloud.config.JacksonConfiguration
import nl.marcenschede.starters.akamaiidentitycloud.config.akamaiIdentityCloudConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.SimpleRequestExpectationManager
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

class AkamaiCreateDslTest {

    @Test
    fun a() {
        val objectMapper = JacksonConfiguration().objectMapper()
        val restTemplate = RestTemplate()
        val mockServer = MockRestServiceServer.bindTo(restTemplate).build(SimpleRequestExpectationManager())

        val config = akamaiIdentityCloudConfig {
            this.url = "http://localhost"
            this.clock = Clock.systemUTC()
            this.clientId = "id"
            this.clientSecret = "secret"
            this.restTemplate = restTemplate
            this.singleElementDecoder = {
                objectMapper.readValue(it, SingleExtendedAccountResponse::class.java)
            }
        }

        mockServer
            .expect(MockRestRequestMatchers.requestTo("http://localhost/entity.create"))
            .andExpect(
                MockRestRequestMatchers.content().string(
                    """
                type_name=user&attributes=%7B%22email%22%3A%22marc%40marc.com%22%7D&include_record=true
            """.trimIndent()
                )
            )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withSuccess().body(
                    """
                {
                  "stat": "ok",
                  "result": {
                    "uuid": "b5eff84c-115d-4000-aee4-e3815e5c84c1",
                    "id": "1805281",
                    "created": "2023-03-18 22:23:24+01:00",
                    "lastUpdated": "2023-03-18 22:23:24+01:00",
                    "email": "marc@marc.com"
                  }
                }
            """.trimIndent()
                ).contentType(MediaType.APPLICATION_JSON)
            )

        val account = createAccount(config) {
            this.attributes["email"] = "marc@marc.com"
        }

        assertThat(account.isRight()).isTrue

        val expectedAccount = account.getOrNull() as ExtendedAccount?
        assertThat(expectedAccount?.uuid).isEqualTo(UUID.fromString("b5eff84c-115d-4000-aee4-e3815e5c84c1"))
        assertThat(expectedAccount?.id).isEqualTo("1805281")
        assertThat(expectedAccount?.created).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))
        assertThat(expectedAccount?.lastUpdated).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))
        assertThat(expectedAccount?.email).isEqualTo("marc@marc.com")

        mockServer.verify()
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
    ) : Account(id, uuid, created, lastUpdated)

}