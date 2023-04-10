package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import nl.marcenschede.starters.akamaiidentitycloud.account.Account
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_FIND
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun findAccount(config: AkamaiIdentityCloudConfig, f: AkamaiFindDsl.() -> Unit): Either<AkamaiUpdateDsl.UpdateError, List<Account>?> {
    return AkamaiFindDsl(config).apply(f).execute()
}

class AkamaiFindDsl(val config: AkamaiIdentityCloudConfig) {
    lateinit var filter: String


    fun execute(): Either<AkamaiUpdateDsl.UpdateError, List<Account>?> {
        return createFindValues(filter)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { postRequest(it.first, it.second) }
    }

    private fun createFindValues(filter: String): Either<AkamaiUpdateDsl.UpdateError, String> {
        return Either.Right(filter)
    }

    private fun createFormParams(it: String): Either.Right<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters["type_name"] = "user"
        parameters["filter"] = it

        return Either.Right(parameters)
    }

    private fun createHeaders(map: MultiValueMap<String, String>): Either<AkamaiUpdateDsl.UpdateError, Pair<MultiValueMap<String, String>, HttpHeaders>> {
        val treeMap = TreeMap<String, String>()
        for (key in TreeSet(map.keys)) {
            treeMap[key] = map[key]!![0]
        }

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val timestamp = createTimestap(config)
        httpHeaders["Date"] = timestamp
        httpHeaders["Authorization"] = calculateAkamaiSignature {
            this.clientId = config.clientId
            this.clientSecret = config.clientSecret
            this.dateTime = timestamp
            this.endpoint = ENDPOINT_ENTITY_FIND
            this.params = treeMap
        }

        return Either.Right(Pair(map, httpHeaders))
    }

    private fun postRequest(
        attributes: MultiValueMap<String, String>,
        headers: HttpHeaders
    ): Either<AkamaiUpdateDsl.UpdateError, List<Account>?> {

        val request = HttpEntity(attributes, headers)
        val forEntity = config.restTemplate.postForEntity(config.findUri, request, FindAccountsResponse::class.java)

        return when (forEntity.statusCodeValue) {
            200 -> {
                when (forEntity.body?.stat) {
                    "ok" -> Either.Right(forEntity.body?.results)
                    else -> Either.Left(AkamaiUpdateDsl.UpdateError.TechnicalError(""))
                }
            }

            else -> Either.Left(AkamaiUpdateDsl.UpdateError.HttpError(forEntity.statusCode))
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    open class AkamaiResponse {
        var stat: String? = null
        var error: String? = null

        @JsonProperty("error_description")
        var errorDescription: String? = null
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FindAccountsResponse(
        val results: List<Account>? = null,
        val result_count: Int? = null,
    ) : AkamaiResponse()

}
