package nl.marcenschede.starters.akamaiidentitycloud.config

import com.fasterxml.jackson.databind.ObjectMapper
import nl.marcenschede.starters.akamaiidentitycloud.update.AkamaiCreateDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.RestTemplate
import java.time.Clock

class AkamaiIdentityCloudConfigDslTest {

    @Test
    fun `happy flow`() {
        val expectedUrl = "https://nu.nl/"
        val expectedRestTemplate = RestTemplate()
        val expectedObjectMapper = ObjectMapper()

        val expectedClientId = "a"
        val expectedClientSecret = "b"
        val config = akamaiIdentityCloudConfig {
            this.url = expectedUrl
            this.clock = Clock.systemUTC()
            this.clientId = expectedClientId
            this.clientSecret = expectedClientSecret
            this.restTemplate = expectedRestTemplate
            this.objectMapper = expectedObjectMapper
            this.singleElementDecoder = {
                AkamaiCreateDsl.SingleAccountResponse(null)
            }
        }

        assertThat(config.url).isEqualTo(expectedUrl)
        assertThat(config.clientId).isEqualTo(expectedClientId)
        assertThat(config.clientSecret).isEqualTo(expectedClientSecret)
        assertThat(config.clock).isNotNull
        assertThat(config.objectMapper).isSameAs(expectedObjectMapper)
        assertThat(config.restTemplate).isSameAs(expectedRestTemplate)
        assertThat(config.singleElementDecoder).isNotNull
    }

    @Test
    fun `when clock not set then exception`() {
        assertThrows<IllegalArgumentException> {
            akamaiIdentityCloudConfig {
                this.url = "https://nu.nl/"
                this.clientId = "expectedClientId"
                this.clientSecret = "expectedClientSecret"
                this.restTemplate = RestTemplate()
                this.objectMapper = ObjectMapper()
                this.singleElementDecoder = {
                    AkamaiCreateDsl.SingleAccountResponse(null)
                }
            }
        }
    }

    @Test
    fun `when clientId not set then exception`() {
        assertThrows<IllegalArgumentException> {
            akamaiIdentityCloudConfig {
                this.url = "https://nu.nl/"
                this.clock = Clock.systemUTC()
                this.clientSecret = "expectedClientSecret"
                this.restTemplate = RestTemplate()
                this.objectMapper = ObjectMapper()
                this.singleElementDecoder = {
                    AkamaiCreateDsl.SingleAccountResponse(null)
                }
            }
        }
    }

    @Test
    fun `when clientSecret not set then exception`() {
        assertThrows<IllegalArgumentException> {
            akamaiIdentityCloudConfig {
                this.url = "https://nu.nl/"
                this.clock = Clock.systemUTC()
                this.clientId = "expectedClientId"
                this.restTemplate = RestTemplate()
                this.objectMapper = ObjectMapper()
                this.singleElementDecoder = {
                    AkamaiCreateDsl.SingleAccountResponse(null)
                }
            }
        }
    }

    @Test
    fun `when singleElementDecoder not set then exception`() {
        assertThrows<IllegalArgumentException> {
            akamaiIdentityCloudConfig {
                this.url = "https://nu.nl/"
                this.clock = Clock.systemUTC()
                this.clientId = "expectedClientId"
                this.clientSecret = "expectedClientSecret"
                this.restTemplate = RestTemplate()
                this.objectMapper = ObjectMapper()
            }
        }
    }

    @Test
    fun `when restTemplate not set then default`() {
        val config =    akamaiIdentityCloudConfig {
                this.url = "https://nu.nl/"
                this.clock = Clock.systemUTC()
                this.clientId = "expectedClientId"
                this.clientSecret = "expectedClientSecret"
                this.objectMapper = ObjectMapper()
                this.singleElementDecoder = {
                    AkamaiCreateDsl.SingleAccountResponse(null)
                }
            }

        assertThat(config.restTemplate).isNotNull
    }


    @Test
    fun `when objectMapper not set then default`() {
        val config =    akamaiIdentityCloudConfig {
                this.url = "https://nu.nl/"
                this.clock = Clock.systemUTC()
                this.clientId = "expectedClientId"
                this.clientSecret = "expectedClientSecret"
                this.restTemplate = RestTemplate()
                this.singleElementDecoder = {
                    AkamaiCreateDsl.SingleAccountResponse(null)
                }
            }

        assertThat(config.objectMapper).isNotNull
    }

    @Nested
    inner class UrlValidation {

        @Test
        fun `when url is null then exception`() {

            assertThrows<IllegalArgumentException> {
                akamaiIdentityCloudConfig {
                    this.clock = Clock.systemUTC()
                    this.clientId = "expectedClientId"
                    this.clientSecret = "expectedClientSecret"
                    this.restTemplate = RestTemplate()
                    this.objectMapper = ObjectMapper()
                    this.singleElementDecoder = {
                        AkamaiCreateDsl.SingleAccountResponse(null)
                    }
                }
            }
        }

        @Test
        fun `when url is blank then exception`() {

            assertThrows<IllegalArgumentException> {
                akamaiIdentityCloudConfig {
                    this.url = ""
                    this.clock = Clock.systemUTC()
                    this.clientId = "expectedClientId"
                    this.clientSecret = "expectedClientSecret"
                    this.restTemplate = RestTemplate()
                    this.objectMapper = ObjectMapper()
                    this.singleElementDecoder = {
                        AkamaiCreateDsl.SingleAccountResponse(null)
                    }
                }
            }
        }

        @Test
        fun `when url is invalid then exception`() {

            assertThrows<IllegalArgumentException> {
                akamaiIdentityCloudConfig {
                    this.url = "aapjeskijken"
                    this.clock = Clock.systemUTC()
                    this.clientId = "expectedClientId"
                    this.clientSecret = "expectedClientSecret"
                    this.restTemplate = RestTemplate()
                    this.objectMapper = ObjectMapper()
                    this.singleElementDecoder = {
                        AkamaiCreateDsl.SingleAccountResponse(null)
                    }
                }
            }
        }
    }


}