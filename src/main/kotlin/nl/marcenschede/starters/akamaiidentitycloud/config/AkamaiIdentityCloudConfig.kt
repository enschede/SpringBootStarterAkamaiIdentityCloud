package nl.marcenschede.starters.akamaiidentitycloud.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import nl.marcenschede.starters.akamaiidentitycloud.account.MultiAccountResponse
import nl.marcenschede.starters.akamaiidentitycloud.account.SingleAccountResponse
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.util.StringUtils.hasText
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.time.Clock

internal const val ENDPOINT_ENTITY_GET = "/entity"
internal const val ENDPOINT_ENTITY_FIND = "/entity.find"
internal const val ENDPOINT_ENTITY_UPDATE = "/entity.update"
internal const val ENDPOINT_ENTITY_CREATE = "/entity.create"

fun akamaiIdentityCloudConfig(f: AkamaiIdentityCloudConfigDsl.() -> Unit): AkamaiIdentityCloudConfig {
    return AkamaiIdentityCloudConfigDsl().apply(f).build()
}

class AkamaiIdentityCloudConfigDsl {
    var url: String? = null
    var clock: Clock? = null
    var clientId: String? = null
    var clientSecret: String? = null
    var restTemplate: RestTemplate? = null
    var objectMapper: ObjectMapper? = null
    var singleElementDecoder: ((String) -> SingleAccountResponse)? = null
    var multiElementDecoder: ((String) -> MultiAccountResponse)? = null

    private fun exceptOnInvalidUrl() {
        if (url == null || url!!.length == 0)
            throw IllegalArgumentException("URL should not be empty")

        try {
            URL(url)
        } catch (e: Exception) {
            throw IllegalArgumentException("URL is invalid")
        }
    }

    fun build(): AkamaiIdentityCloudConfig {
        exceptOnInvalidUrl()

        return AkamaiIdentityCloudConfig(
            url = url!!,
            objectMapper = this.objectMapper ?: ObjectMapper().apply {
                registerModule(kotlinModule())
                registerModule(Jdk8Module())
                registerModule(JavaTimeModule())
                registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))

                setSerializationInclusion(JsonInclude.Include.NON_NULL)
                configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            },
            clock = clock ?: throw IllegalArgumentException("clock is mandatory"),
            clientId = if (hasText(clientId)) clientId!! else throw IllegalArgumentException("clientid is mandatory"),
            clientSecret = if (hasText(clientSecret)) clientSecret!! else  throw IllegalArgumentException("clientSecret is mandatory"),
            restTemplate = restTemplate ?: RestTemplateBuilder().build(),
            singleElementDecoder = singleElementDecoder
                ?: throw IllegalArgumentException("singleElementDecoder is mandatory"),
            multiElementDecoder = multiElementDecoder
                ?: throw IllegalArgumentException("multiElementDecoder is mandatory"),
            )
    }
}


data class AkamaiIdentityCloudConfig(
    val url: String,
    val objectMapper: ObjectMapper,
    val clock: Clock,
    val clientId: String,
    val clientSecret: String,
    val restTemplate: RestTemplate,
    val singleElementDecoder: (String) -> SingleAccountResponse,
    val multiElementDecoder: (String) -> MultiAccountResponse,
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