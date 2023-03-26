package nl.marcenschede.starters.akamaiidentitycloud.config

import com.fasterxml.jackson.databind.ObjectMapper
import nl.marcenschede.starters.akamaiidentitycloud.update.SingleAccountResponse
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
            url = expectedUrl
            clock = Clock.systemUTC()
            clientId = expectedClientId
            clientSecret = expectedClientSecret
            restTemplate = expectedRestTemplate
            objectMapper = expectedObjectMapper
            singleElementDecoder = {
                SingleAccountResponse(null)
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
                url = "https://nu.nl/"
                clientId = "expectedClientId"
                clientSecret = "expectedClientSecret"
                restTemplate = RestTemplate()
                objectMapper = ObjectMapper()
                singleElementDecoder = {
                    SingleAccountResponse(null)
                }
            }
        }
    }

    @Test
    fun `when clientId not set then exception`() {
        assertThrows<IllegalArgumentException> {
            akamaiIdentityCloudConfig {
                "https://nu.nl/".also { this.url = it }
                clock = Clock.systemUTC()
                clientId = ""
                clientSecret = "expectedClientSecret"
                restTemplate = RestTemplate()
                objectMapper = ObjectMapper()
                singleElementDecoder = {
                    SingleAccountResponse(null)
                }
            }
        }
    }

    @Test
    fun `when clientSecret not set then exception`() {
        assertThrows<IllegalArgumentException> {
            akamaiIdentityCloudConfig {
                url = "https://nu.nl/"
                clock = Clock.systemUTC()
                clientId = "expectedClientId"
                clientSecret = ""
                restTemplate = RestTemplate()
                objectMapper = ObjectMapper()
                singleElementDecoder = {
                    SingleAccountResponse(null)
                }
            }
        }
    }

    @Test
    fun `when singleElementDecoder not set then exception`() {
        assertThrows<IllegalArgumentException> {
            akamaiIdentityCloudConfig {
                url = "https://nu.nl/"
                clock = Clock.systemUTC()
                clientId = "expectedClientId"
                clientSecret = "expectedClientSecret"
                restTemplate = RestTemplate()
                objectMapper = ObjectMapper()
            }
        }
    }

    @Test
    fun `when restTemplate not set then default`() {
        val config =    akamaiIdentityCloudConfig {
                url = "https://nu.nl/"
                clock = Clock.systemUTC()
                clientId = "expectedClientId"
                clientSecret = "expectedClientSecret"
                objectMapper = ObjectMapper()
                singleElementDecoder = {
                    SingleAccountResponse(null)
                }
            }

        assertThat(config.restTemplate).isNotNull
    }


    @Test
    fun `when objectMapper not set then default`() {
        val config =    akamaiIdentityCloudConfig {
                url = "https://nu.nl/"
                clock = Clock.systemUTC()
                clientId = "expectedClientId"
                clientSecret = "expectedClientSecret"
                restTemplate = RestTemplate()
                singleElementDecoder = {
                    SingleAccountResponse(null)
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
                    clock = Clock.systemUTC()
                    clientId = "expectedClientId"
                    clientSecret = "expectedClientSecret"
                    restTemplate = RestTemplate()
                    objectMapper = ObjectMapper()
                    singleElementDecoder = {
                        SingleAccountResponse(null)
                    }
                }
            }
        }

        @Test
        fun `when url is blank then exception`() {

            assertThrows<IllegalArgumentException> {
                akamaiIdentityCloudConfig {
                    url = ""
                    clock = Clock.systemUTC()
                    clientId = "expectedClientId"
                    clientSecret = "expectedClientSecret"
                    restTemplate = RestTemplate()
                    objectMapper = ObjectMapper()
                    singleElementDecoder = {
                        SingleAccountResponse(null)
                    }
                }
            }
        }

        @Test
        fun `when url is invalid then exception`() {

            assertThrows<IllegalArgumentException> {
                akamaiIdentityCloudConfig {
                    url = "aapjeskijken"
                    clock = Clock.systemUTC()
                    clientId = "expectedClientId"
                    clientSecret = "expectedClientSecret"
                    restTemplate = RestTemplate()
                    objectMapper = ObjectMapper()
                    singleElementDecoder = {
                        SingleAccountResponse(null)
                    }
                }
            }
        }
    }


}