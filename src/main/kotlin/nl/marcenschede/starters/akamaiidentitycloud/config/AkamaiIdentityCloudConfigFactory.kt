package nl.marcenschede.starters.akamaiidentitycloud.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.util.StringUtils.hasText
import org.springframework.web.client.RestTemplate
import java.time.Clock
import java.time.ZoneId

class AkamaiIdentityCloudConfigFactory(
    val baseUrl: String?,
    val clientId: String?,
    val clientSecret: String?,
    val timezone: String?,
) {

    val build: AkamaiIdentityCloudConfig
        get() {
            val objectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS)
            val zoneId: String = if (hasText(timezone)) timezone!! else "UTC"
            val clock = Clock.system(ZoneId.of(zoneId))

            return AkamaiIdentityCloudConfig(
                url = baseUrl ?: throw IllegalArgumentException("akamai.identitycloud.accounts.baseurl property missing"),
                objectMapper = objectMapper,
                clock = clock,
                clientId = if (hasText(clientId)) clientId!! else throw IllegalArgumentException("akamai.identitycloud.accounts.clientid property missing"),
                clientSecret = if (hasText(clientSecret)) clientSecret!! else throw IllegalArgumentException("akamai.identitycloud.accounts.clientsecret property missing"),
                RestTemplate()
            )
        }
}