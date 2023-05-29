package nl.marcenschede.starters.akamaiidentitycloud.update

import nl.marcenschede.starters.akamaiidentitycloud.account.MultiExtendedAccountResponse
import nl.marcenschede.starters.akamaiidentitycloud.account.SingleAccountResponse
import nl.marcenschede.starters.akamaiidentitycloud.config.akamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.fixedClockMay29
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.SimpleRequestExpectationManager
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import java.time.OffsetDateTime
import java.util.*

class AkamaiFindDslTest {

    @Test
    fun `when extended item is updated then message is send to identity cloud`() {
        val restTemplate = RestTemplate()
        val mockServer = MockRestServiceServer.bindTo(restTemplate).build(SimpleRequestExpectationManager())

        val config = akamaiIdentityCloudConfig {
            this.url = "http://localhost"
            this.clock = fixedClockMay29
            this.clientId = "id"
            this.clientSecret = "secret"
            this.restTemplate = restTemplate
            this.singleElementDecoder = { objectMapper, jsonString ->
                objectMapper.readValue(jsonString, SingleAccountResponse::class.java)
            }
            this.multiElementDecoder = { objectMapper, jsonString ->
                objectMapper.readValue(jsonString, MultiExtendedAccountResponse::class.java)
            }
        }

        mockServer
            .expect(MockRestRequestMatchers.requestTo("http://localhost/entity.find"))
            .andExpect(
                MockRestRequestMatchers.content().string(
                    """
                type_name=user&filter=status%3D%27CLOSED%27
            """.trimIndent()
                )
            )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.header("Authorization", "Signature id:DuvwBhp28ipLU9NQJL0djmLxrVc="))
            .andRespond(
                MockRestResponseCreators.withSuccess().body(
                    """
                {
                  "stat": "ok",
                  "result":[{
                    "uuid": "b5eff84c-115d-4000-aee4-e3815e5c84c1",
                    "id": "1805281",
                    "created": "2023-03-18 22:23:24+01:00",
                    "lastUpdated": "2023-03-18 22:23:24+01:00"
                  }, {
                    "uuid": "b5eff84c-115d-4000-aee4-e3815e5c84c2",
                    "id": "1805282",
                    "created": "2023-03-18 22:23:24+01:00",
                    "lastUpdated": "2023-03-18 22:23:24+01:00"
                  }]
                }
            """.trimIndent()
                ).contentType(MediaType.APPLICATION_JSON)
            )

        val account = findAccount(config) {
            this.filter = "status='CLOSED'"
        }

        assertThat(account.isRight()).isTrue
        assertThat(account.getOrNull()?.size).isEqualTo(2)

        val expectedAccount = account.getOrNull()?.get(0)
        assertThat(expectedAccount?.uuid).isEqualTo(UUID.fromString("b5eff84c-115d-4000-aee4-e3815e5c84c1"))
        assertThat(expectedAccount?.id).isEqualTo("1805281")
        assertThat(expectedAccount?.created).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))
        assertThat(expectedAccount?.lastUpdated).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))

        mockServer.verify()
    }
}
