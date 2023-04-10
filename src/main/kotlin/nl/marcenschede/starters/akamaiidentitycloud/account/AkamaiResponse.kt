package nl.marcenschede.starters.akamaiidentitycloud.account

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
open class AkamaiResponse {
    var stat: String? = null
    var error: String? = null

    @JsonProperty("error_description")
    var errorDescription: String? = null

}

@JsonIgnoreProperties(ignoreUnknown = true)
open class SingleAccountResponse(
    open val result: Account? = null,
) : AkamaiResponse()
