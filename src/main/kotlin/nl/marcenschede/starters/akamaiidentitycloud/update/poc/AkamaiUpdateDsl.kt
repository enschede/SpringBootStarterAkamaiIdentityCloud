package nl.marcenschede.starters.akamaiidentitycloud.update.poc

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.core.JsonProcessingException
import nl.marcenschede.starters.akamaiidentitycloud.account.Account
import nl.marcenschede.starters.akamaiidentitycloud.account.AkamaiResponse
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_UPDATE
import nl.marcenschede.starters.akamaiidentitycloud.update.AkamaiCreateDsl
import nl.marcenschede.starters.akamaiidentitycloud.update.PersistenceError
import nl.marcenschede.starters.akamaiidentitycloud.update.calculateAkamaiSignature
import nl.marcenschede.starters.akamaiidentitycloud.update.createTimestap
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.OffsetDateTime
import java.util.*

fun accountUpdate(config: AkamaiIdentityCloudConfig, f: AkamaiUpdateDsl.() -> Unit): Any {
    return AkamaiUpdateDsl(config).apply(f).execute()
}

class AkamaiUpdateDsl(private val config: AkamaiIdentityCloudConfig) {
    lateinit var uuid: UUID

    val attributes = mutableMapOf<String, Any?>()

    fun execute(): Either<PersistenceError, Any> {

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
        httpHeaders["Authorization"] =
            calculateAkamaiSignature(config.clientId, config.clientSecret, timestamp, ENDPOINT_ENTITY_UPDATE) {
                for ((key, value) in treeMap) {
                    header(key, value)
                }
            }

        return Either.Right(AkamaiCreateDsl.HeaderParameterPair(map, httpHeaders))
    }

    private fun postRequest(
        input: AkamaiCreateDsl.HeaderParameterPair,
    ): Either<PersistenceError, Account> {

        val headers = input.httpHeaders

        val request = HttpEntity(input.map, headers)
        println("postRequest::request = $request")
        val forEntity = config.restTemplate.postForEntity(config.updateUri, request, AkamaiResponse::class.java)

        return when (forEntity.statusCodeValue) {
            200, 201, 202, 203, 204, 205, 206, 207, 208, 226 -> {
                when (forEntity.body?.stat) {
                    "ok" -> Either.Right(Account("1", UUID.randomUUID(), OffsetDateTime.now(), OffsetDateTime.now()))
                    "error" -> {
                        println("postRequest::error::forEntity = ${forEntity}")
                        println("postRequest::error::forEntity.body.stat = ${forEntity.body?.stat}")
                        println("postRequest::error::forEntity.body.error = ${forEntity.body?.error}")
                        println("postRequest::error::forEntity.body.errorDescription = ${forEntity.body?.errorDescription}")
                        Either.Left(
                            PersistenceError.AkamaiError(
                                forEntity.body?.error,
                                forEntity.body?.errorDescription
                            )
                        )
                    }

                    "fail" -> {
                        println("postRequest::fail::forEntity.body = ${forEntity.body}")
                        Either.Left(PersistenceError.TechnicalError(""))
                    }

                    else -> {
                        println("postRequest::else::forEntity.body = ${forEntity.body}")
                        Either.Left(
                            PersistenceError.AkamaiError(
                                forEntity.body?.error,
                                forEntity.body?.errorDescription
                            )
                        )
                    }
                }
            }

            else -> {
                println("postRequest::else::forEntity.body = ${forEntity.body}")
                Either.Left(PersistenceError.HttpError(forEntity.statusCode))
            }
        }
    }

}
