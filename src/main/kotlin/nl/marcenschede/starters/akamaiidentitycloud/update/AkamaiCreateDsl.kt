package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.*
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_CREATE
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun createAccount(
    config: AkamaiIdentityCloudConfig,
    f: AkamaiCreateDsl.() -> Unit
): Either<AkamaiUpdateDsl.UpdateError, AkamaiCreateDsl.CreateAccountResponse> {
    return AkamaiCreateDsl(config).apply(f).execute()
}

class AkamaiCreateDsl(val config: AkamaiIdentityCloudConfig) {
    val attributes = mutableMapOf<String, Any?>()

    fun execute(): Either<AkamaiUpdateDsl.UpdateError, CreateAccountResponse> {
        return createValues(attributes)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { postRequest(it) }
    }

    private fun createValues(attributes: MutableMap<String, Any?>): Either<AkamaiUpdateDsl.UpdateError, String> {
        return try {
            Either.Right(config.objectMapper.writeValueAsString(attributes))
        } catch (e: JsonProcessingException) {
            Either.Left(AkamaiUpdateDsl.UpdateError.TechnicalError("Parsing json", e))
        }
    }

    private fun createFormParams(attributes: String): Either.Right<MultiValueMap<String, String>> {
        // marc Deze map levert lijsten op, dat is niet de bedoeling
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters["type_name"] = "user"
        parameters["attributes"] = attributes
        parameters["include_record"] = true.toString()

        return Either.Right(parameters)
    }

    private fun createHeaders(map: MultiValueMap<String, String>): Either.Right<HeaderParameterPair> {
        val treeMap = TreeMap<String, String>()
        for (key in TreeSet(map.keys)) {
            val a = map[key]!![0]
            treeMap[key] = a
        }
        println(treeMap)

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val timestamp = createTimestap(config)
        httpHeaders["Date"] = timestamp
        httpHeaders["Authorization"] = calculateAkamaiSignature {
            clientId = config.clientId
            clientSecret = config.clientSecret
            dateTime = timestamp
            endpoint = ENDPOINT_ENTITY_CREATE
            params = treeMap
        }

        return Either.Right(HeaderParameterPair(map,httpHeaders))
    }

    class HeaderParameterPair(val map: MultiValueMap<String, String>, val httpHeaders: HttpHeaders)

    private fun postRequest(
      input:  HeaderParameterPair
    ): Either<AkamaiUpdateDsl.UpdateError, CreateAccountResponse> {

        val headers = input.httpHeaders

        val request = HttpEntity(input.map, headers)
        println("postRequest::request = $request")
        val forEntity = config.restTemplate.postForEntity(config.createUri, request, CreateAccountResponse::class.java)

        return when (forEntity.statusCodeValue) {
            200, 201, 202, 203, 204, 205, 206, 207, 208, 226 -> {
                when (forEntity.body?.stat) {
                    "ok" -> Either.Right(forEntity.body!!)
                    "error" -> {
                        Either.Left(
                            AkamaiUpdateDsl.UpdateError.AkamaiError(
                                forEntity.body?.error,
                                forEntity.body?.errorDescription
                            )
                        )
                    }

                    "fail" -> {
                        println("postRequest::fail::forEntity.body = ${forEntity.body}")
                        Either.Left(AkamaiUpdateDsl.UpdateError.TechnicalError(""))
                    }

                    else -> {
                        println("postRequest::else::forEntity.body = ${forEntity.body}")
                        Either.Left(
                            AkamaiUpdateDsl.UpdateError.AkamaiError(
                                forEntity.body?.error,
                                forEntity.body?.errorDescription
                            )
                        )
                    }
                }
            }

            else -> {
                println("postRequest::else::forEntity.body = ${forEntity.body}")
                Either.Left(AkamaiUpdateDsl.UpdateError.HttpError(forEntity.statusCode))
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
    data class CreateAccountResponse(
        val result: Account? = null,
    ) : AkamaiGetDsl.AkamaiResponse()
}
