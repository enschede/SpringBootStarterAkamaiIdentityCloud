package nl.marcenschede.starters.akamaiidentitycloud.update

import nl.marcenschede.starters.akamaiidentitycloud.account.*
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

class AkamaiGetDslTest {

    @Test
    fun `when base item is fetched then message is found in identity cloud`() {
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
                objectMapper.readValue(jsonString, MultiAccountResponse::class.java)
            }
        }

        mockServer
            .expect(MockRestRequestMatchers.requestTo("http://localhost/entity"))
            .andExpect(
                MockRestRequestMatchers.content().string(
                    """
                type_name=user&uuid=b5eff84c-115d-4000-aee4-e3815e5c84c1
            """.trimIndent()
                )
            )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.header("Authorization", "Signature id:Mo1Lhs8HRBTRDkcMTXdyjH8qEwA="))
            .andRespond(
                MockRestResponseCreators.withSuccess().body(
                    """
                {
                  "stat": "ok",
                  "result": {
                    "uuid": "b5eff84c-115d-4000-aee4-e3815e5c84c1",
                    "id": "1805281",
                    "created": "2023-03-18 22:23:24+01:00",
                    "lastUpdated": "2023-03-18 22:23:24+01:00"
                  }
                }
            """.trimIndent()
                ).contentType(MediaType.APPLICATION_JSON)
            )

        val account = getAccount(config) {
            this.uuid = "b5eff84c-115d-4000-aee4-e3815e5c84c1"
        }

        assertThat(account.isRight()).isTrue

        val expectedAccount = account.getOrNull()
        assertThat(expectedAccount?.uuid).isEqualTo(UUID.fromString("b5eff84c-115d-4000-aee4-e3815e5c84c1"))
        assertThat(expectedAccount?.id).isEqualTo("1805281")
        assertThat(expectedAccount?.created).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))
        assertThat(expectedAccount?.lastUpdated).isEqualTo(OffsetDateTime.parse("2023-03-18T22:23:24+01:00"))

        mockServer.verify()
    }

    @Test
    fun `when extended item is fetched then message is found in identity cloud`() {
        val restTemplate = RestTemplate()
        val mockServer = MockRestServiceServer.bindTo(restTemplate).build(SimpleRequestExpectationManager())

        val config = akamaiIdentityCloudConfig {
            this.url = "http://localhost"
            this.clock = fixedClockMay29
            this.clientId = "id"
            this.clientSecret = "secret"
            this.restTemplate = restTemplate
            this.singleElementDecoder = { objectMapper, jsonString ->
                objectMapper.readValue(jsonString, SingleExtendedAccountResponse::class.java)
            }
            this.multiElementDecoder = { objectMapper, jsonString ->
                objectMapper.readValue(jsonString, MultiExtendedAccountResponse::class.java)
            }
        }

        mockServer
            .expect(MockRestRequestMatchers.requestTo("http://localhost/entity"))
            .andExpect(
                MockRestRequestMatchers.content().string(
                    """
                type_name=user&uuid=b5eff84c-115d-4000-aee4-e3815e5c84c1
            """.trimIndent()
                )
            )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.header("Authorization", "Signature id:Mo1Lhs8HRBTRDkcMTXdyjH8qEwA="))
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

        val account = getAccount(config) {
            this.uuid = "b5eff84c-115d-4000-aee4-e3815e5c84c1"
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

}