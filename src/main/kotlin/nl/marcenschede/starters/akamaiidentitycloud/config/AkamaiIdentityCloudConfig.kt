package nl.marcenschede.starters.akamaiidentitycloud.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.time.Clock

internal const val ENDPOINT_ENTITY_GET = "/entity"
internal const val ENDPOINT_ENTITY_FIND = "/entity.find"
internal const val ENDPOINT_ENTITY_UPDATE = "/entity.update"
internal const val ENDPOINT_ENTITY_CREATE = "/entity.create"

data class AkamaiIdentityCloudConfig(
    val url: String,
    val objectMapper: ObjectMapper,
    val clock: Clock,
    val clientId: String,
    val clientSecret: String,
    val restTemplate: RestTemplate,
) {
    val getUri by lazy {
        URL(url + ENDPOINT_ENTITY_GET).toURI()
    }
    val updateUri by lazy {
        URL(url + ENDPOINT_ENTITY_UPDATE).toURI()
    }
    val findUri by lazy {
        URL(url + ENDPOINT_ENTITY_FIND).toURI()
    }
    val createUri by lazy {
        URL(url + ENDPOINT_ENTITY_CREATE).toURI()
    }
}