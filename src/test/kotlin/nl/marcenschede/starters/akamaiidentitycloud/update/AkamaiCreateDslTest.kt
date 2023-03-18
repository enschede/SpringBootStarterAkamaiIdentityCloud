package nl.marcenschede.starters.akamaiidentitycloud.update

import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.JacksonConfiguration
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

class AkamaiCreateDslTest {

    @Test
    fun a() {
        val objectMapper = JacksonConfiguration().objectMapper()
        val restTemplate = RestTemplate()
        val mockServer = MockRestServiceServer.bindTo(restTemplate).build(SimpleRequestExpectationManager())

        val config: AkamaiIdentityCloudConfig =
            AkamaiIdentityCloudConfig("http://locslhost", objectMapper, Clock.systemUTC(), "id", "secret", restTemplate)

        val expect =
            mockServer
                .expect(MockRestRequestMatchers.requestTo("http://locslhost/entity.create"))
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
        assertThat(account.getOrNull()?.result?.uuid).isEqualTo("b5eff84c-115d-4000-aee4-e3815e5c84c1")
        assertThat(account.getOrNull()?.result?.id).isEqualTo("1805281")
        assertThat(account.getOrNull()?.result?.created).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))
        assertThat(account.getOrNull()?.result?.lastUpdated).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))

        mockServer.verify()
    }


    class ExtendedAccount(
        id: String,
        uuid: String,
        created: OffsetDateTime,
        lastUpdated: OffsetDateTime,
        var email: String? = null,
        var emailVerified: OffsetDateTime? = null,
        var mobileNumber: String? = null,
        var mobileNumberVerified: OffsetDateTime? = null
    ) : Account(id, uuid, created, lastUpdated)

}