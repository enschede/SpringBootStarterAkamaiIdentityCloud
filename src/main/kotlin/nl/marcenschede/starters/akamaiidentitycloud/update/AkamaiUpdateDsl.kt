package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.core.JsonProcessingException
import nl.marcenschede.starters.akamaiidentitycloud.account.Account
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_UPDATE
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun accountUpdate(config: AkamaiIdentityCloudConfig, f: AkamaiUpdateDsl.() -> Unit): Either<PersistenceError, Account> {
    return AkamaiUpdateDsl(config).apply(f).execute()
}

class AkamaiUpdateDsl(private val config: AkamaiIdentityCloudConfig) {
    lateinit var uuid: UUID

    val attributes = mutableMapOf<String, Any?>()

    fun execute(): Either<PersistenceError, Account> {

        return createUpdateValues(attributes)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { postRequest(it) }
    }

    private fun createUpdateValues(attributes: MutableMap<String, Any?>): Either<PersistenceError, String> {
        return try {
            Either.Right(config.objectMapper.writeValueAsString(attributes))
        } catch (e: JsonProcessingException) {
            Either.Left(PersistenceError.TechnicalError("Json parsing", e))
        }
    }

    private fun createFormParams(it: String): Either.Right<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters["type_name"] = "user"
        parameters["uuid"] = uuid.toString()
        parameters["attributes"] = it

        parameters["include_record"] = "true"

        return Either.Right(parameters)
    }

    private fun createHeaders(map: MultiValueMap<String, String>): Either.Right<HeaderParameterPair> {
        val treeMap = TreeMap<String, String>()
        for (key in TreeSet(map.keys)) {
            val a = map[key]!![0]
            treeMap[key] = a
        }

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val timestamp = createTimestap(config)
        httpHeaders["Date"] = timestamp
        httpHeaders["Authorization"] =
            calculateAkamaiSignature(config.clientId, config.clientSecret, timestamp, ENDPOINT_ENTITY_UPDATE) {
                for ((key, value) in treeMap) {
                    header(key, value)
                }
            }

        return Either.Right(HeaderParameterPair(map, httpHeaders))
    }

    class HeaderParameterPair(val map: MultiValueMap<String, String>, val httpHeaders: HttpHeaders)

    private fun postRequest(
        input: HeaderParameterPair,
    ): Either<PersistenceError, Account> {

        val headers = input.httpHeaders

        val request = HttpEntity(input.map, headers)
        val response = config.restTemplate.postForEntity(config.updateUri, request, String::class.java)

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

}
