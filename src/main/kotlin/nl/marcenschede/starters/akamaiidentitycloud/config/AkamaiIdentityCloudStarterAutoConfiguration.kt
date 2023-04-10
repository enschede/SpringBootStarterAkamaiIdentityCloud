package nl.marcenschede.starters.akamaiidentitycloud.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
class AkamaiIdentityCloudStarterAutoConfiguration {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

//    @Bean
//    @ConditionalOnMissingBean
//    fun factory(
//        @Value("\${akamai.identitycloud.accounts.baseurl:}") baseUrl: String,
//        @Value("\${akamai.identitycloud.accounts.clientid:}") clientId: String,
//        @Value("\${akamai.identitycloud.accounts.clientsecret:}") clientSecret: String,
//        @Value("\${akamai.identitycloud.accounts.timezone:}") timezone: String,
//    ): AkamaiIdentityCloudConfigFactory {
//        log.debug("factory::baseUrl = $baseUrl")
//        log.debug("factory::clientId = $clientId")
//
//        return AkamaiIdentityCloudConfigFactory(baseUrl, clientId, clientSecret, timezone)
//    }

//    @Bean
//    @ConditionalOnMissingBean
//    fun config(factory: AkamaiIdentityCloudConfigFactory): AkamaiIdentityCloudConfig {
//        return factory.build
//    }
}