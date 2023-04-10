package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import nl.marcenschede.starters.akamaiidentitycloud.account.Account
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_GET
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun getAccount(config: AkamaiIdentityCloudConfig, f: AkamaiGetDsl.() -> Unit): Either<PersistenceError, Account?> {
    return AkamaiGetDsl(config).apply(f).execute()
}

class AkamaiGetDsl(val config: AkamaiIdentityCloudConfig) {
    lateinit var uuid: String

    fun execute(): Either<PersistenceError, Account?> {
        return createFindValues(uuid)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { postRequest(it) }
    }

    private fun createFindValues(uuid: String): Either<PersistenceError, String> {
        return Either.Right(uuid)
    }

    private fun createFormParams(it: String): Either.Right<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters["type_name"] = "user"
        parameters["uuid"] = it

        return Either.Right(parameters)
    }

    class HeaderParameterPair(val map: MultiValueMap<String, String>, val httpHeaders: HttpHeaders)

    private fun createHeaders(map: MultiValueMap<String, String>): Either.Right<HeaderParameterPair> {
        val treeMap = TreeMap<String, String>()
        for (key in TreeSet(map.keys)) {
            treeMap[key] = map[key]!![0]
        }

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val timestamp = createTimestap(config)
        httpHeaders["Date"] = timestamp
        httpHeaders["Authorization"] = calculateAkamaiSignature(config.clientId,config.clientSecret,timestamp,ENDPOINT_ENTITY_GET){
            for ((key, value) in treeMap) {
                header(key,value)
            }
        }

        return Either.Right(HeaderParameterPair(map, httpHeaders))
    }

    private fun postRequest(
        input: HeaderParameterPair
    ): Either<PersistenceError, Account?> {

        val request = HttpEntity(input.map, input.httpHeaders)
        val response = config.restTemplate.postForEntity(config.getUri, request, String::class.java)

        return when {
            response.statusCode.is2xxSuccessful -> {
                val forEntity = config.singleElementDecoder.invoke(response.body!!)

                when (forEntity.stat) {
                    "ok" -> Either.Right(forEntity.result!!)

                    "error" -> {
                        Either.Left(
                            PersistenceError.AkamaiError(
                                forEntity.error,
                                forEntity.errorDescription
                            )
                        )
                    }

                    "fail" -> {
                        Either.Left(PersistenceError.TechnicalError(forEntity.errorDescription ?: ""))
                    }

                    else -> {
                        Either.Left(
                            PersistenceError.AkamaiError(
                                forEntity.error,
                                forEntity.errorDescription
                            )
                        )
                    }
                }
            }

            else -> {
                Either.Left(PersistenceError.HttpError(response.statusCode))
            }
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
    data class FindAccountResponse(
        val result: Account? = null,
    ) : AkamaiResponse()
}
