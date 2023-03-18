package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.core.JsonProcessingException
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_UPDATE
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun accountUpdate(config: AkamaiIdentityCloudConfig, f: AkamaiUpdateDsl.() -> Unit): Any {
    return AkamaiUpdateDsl(config).apply(f).execute()
}

class AkamaiUpdateDsl(private val config: AkamaiIdentityCloudConfig) {
    lateinit var uuid: UUID

    val attributes = mutableMapOf<String, Any?>()

    fun execute(): Either<UpdateError, Any> {

        return createUpdateValues(attributes)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { postRequest(it) }
    }

    private fun createUpdateValues(attributes: MutableMap<String, Any?>): Either<UpdateError, String> {
        return try {
            Either.Right(config.objectMapper.writeValueAsString(attributes))
        } catch (e: JsonProcessingException) {
            Either.Left(UpdateError.TechnicalError("Json parsing", e))
        }
    }

    private fun createFormParams(it: String): Either.Right<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters["type_name"] = "user"
        parameters["uuid"] = uuid.toString()
        parameters["attributes"] = it

        return Either.Right(parameters)
    }

    private fun createHeaders(map: MultiValueMap<String, String>): Either.Right<AkamaiCreateDsl.HeaderParameterPair> {
        val treeMap = TreeMap<String, String>()
        for (key in TreeSet(map.keys)) {
            val a = map[key]!![0]
            treeMap[key] = a
        }

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val timestamp = createTimestap(config)
        httpHeaders["Date"] = timestamp
        httpHeaders["Authorization"] = calculateAkamaiSignature {
            clientId = config.clientId
            clientSecret = config.clientSecret
            dateTime = timestamp
            endpoint = ENDPOINT_ENTITY_UPDATE
            params = treeMap
        }

        return Either.Right(AkamaiCreateDsl.HeaderParameterPair(map, httpHeaders))
    }

    private fun postRequest(
        input: AkamaiCreateDsl.HeaderParameterPair,
    ): Either<UpdateError, AkamaiCreateDsl.CreateAccountResponse> {

        val headers = input.httpHeaders

        val request = HttpEntity(input.map, headers)
        println("postRequest::request = $request")
        val forEntity = config.restTemplate.postForEntity(config.updateUri, request, AkamaiCreateDsl.CreateAccountResponse::class.java)

        return when (forEntity.statusCodeValue) {
            200, 201, 202, 203, 204, 205, 206, 207, 208, 226 -> {
                when (forEntity.body?.stat) {
                    "ok" -> Either.Right(forEntity.body!!)
                    "error" -> {
                        println("postRequest::error::forEntity = ${forEntity}")
                        println("postRequest::error::forEntity.body.stat = ${forEntity.body?.stat}")
                        println("postRequest::error::forEntity.body.error = ${forEntity.body?.error}")
                        println("postRequest::error::forEntity.body.errorDescription = ${forEntity.body?.errorDescription}")
                        Either.Left(
                            UpdateError.AkamaiError(
                                forEntity.body?.error,
                                forEntity.body?.errorDescription
                            )
                        )
                    }

                    "fail" -> {
                        println("postRequest::fail::forEntity.body = ${forEntity.body}")
                        Either.Left(UpdateError.TechnicalError(""))
                    }

                    else -> {
                        println("postRequest::else::forEntity.body = ${forEntity.body}")
                        Either.Left(
                            UpdateError.AkamaiError(
                                forEntity.body?.error,
                                forEntity.body?.errorDescription
                            )
                        )
                    }
                }
            }

            else -> {
                println("postRequest::else::forEntity.body = ${forEntity.body}")
                Either.Left(UpdateError.HttpError(forEntity.statusCode))
            }
        }
    }

    sealed class UpdateError {
        data class TechnicalError(val message: String, val e: Throwable? = null) : UpdateError()
        data class AkamaiError(val error: String?, val errorDescription: String?) : UpdateError()
        data class HttpError(val status: HttpStatus) : UpdateError()

    }
}
