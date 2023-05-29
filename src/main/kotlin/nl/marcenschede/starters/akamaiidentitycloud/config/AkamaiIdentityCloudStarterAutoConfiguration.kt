package nl.marcenschede.starters.akamaiidentitycloud.config

import com.fasterxml.jackson.databind.ObjectMapper
import nl.marcenschede.starters.akamaiidentitycloud.account.MultiAccountResponse
import nl.marcenschede.starters.akamaiidentitycloud.account.SingleAccountResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Clock
import java.time.ZoneId

@Configuration
class AkamaiIdentityCloudStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun config(
        @Value("\${akamai.identitycloud.accounts.baseurl:}") baseUrl: String,
        @Value("\${akamai.identitycloud.accounts.clientid:}") clientId: String,
        @Value("\${akamai.identitycloud.accounts.clientsecret:}") clientSecret: String,
        @Value("\${akamai.identitycloud.accounts.timezone:UTC}") timezone: String,
        @Qualifier("akamai_identity_cloud_objectmapper") objectMapper: ObjectMapper,
    ): AkamaiIdentityCloudConfig {

        val restTemplate = RestTemplate()

        return akamaiIdentityCloudConfig {
            this.url = baseUrl
            this.clock = Clock.system(ZoneId.of(timezone))
            this.clientId = clientId
            this.clientSecret = clientSecret
            this.restTemplate = restTemplate
            this.singleElementDecoder = { objectMapper: ObjectMapper, jsonString: String ->
                objectMapper.readValue(jsonString, SingleAccountResponse::class.java)
            }
            this.multiElementDecoder = {objectMapper, jsonString ->
                objectMapper.readValue(jsonString, MultiAccountResponse::class.java)
            }
        }
    }
}