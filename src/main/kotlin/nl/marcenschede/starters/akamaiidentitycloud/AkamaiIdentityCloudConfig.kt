package nl.marcenschede.starters.akamaiidentitycloud

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL
import java.time.Clock

data class AkamaiIdentityCloudConfig(
    val url: URL,
    val objectMapper: ObjectMapper,
    val clock: Clock,
    val clientId: String,
    val clientSecret: String,
)