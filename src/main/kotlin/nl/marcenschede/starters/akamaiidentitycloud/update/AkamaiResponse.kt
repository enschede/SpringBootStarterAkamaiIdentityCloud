package nl.marcenschede.starters.akamaiidentitycloud.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
open class AkamaiResponse {
    var stat: String? = null
    var error: String? = null

    @JsonProperty("error_description")
    var errorDescription: String? = null

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleAccountResponse(
    val result: Account? = null,
) : AkamaiResponse()
