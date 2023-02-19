package nl.marcenschede.starters.akamaiidentitycloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AkamaiIdentityCloudStarterAutoConfiguration {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    init {
        println("Stage A")
    }

    @Bean
    @ConditionalOnMissingBean
    fun factory(
        @Value("\${akamai.identitycloud.accounts.baseurl:}") baseUrl: String,
        @Value("\${akamai.identitycloud.accounts.clientid:}") clientId: String,
        @Value("\${akamai.identitycloud.accounts.clientsecret:}") clientSecret: String,
    ): AkamaiIdentityCloudConfigFactory {
        log.debug("factory::baseUrl = $baseUrl")
        log.debug("factory::clientId = $clientId")

        return AkamaiIdentityCloudConfigFactory(baseUrl, clientId, clientSecret)
    }

    @Bean
    @ConditionalOnMissingBean
    fun config(factory: AkamaiIdentityCloudConfigFactory): AkamaiIdentityCloudConfig {
        return factory.build
    }
}