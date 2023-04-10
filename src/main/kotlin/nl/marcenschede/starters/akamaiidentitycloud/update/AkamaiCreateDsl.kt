package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.core.JsonProcessingException
import nl.marcenschede.starters.akamaiidentitycloud.account.Account
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_CREATE
import nl.marcenschede.starters.akamaiidentitycloud.update.AkamaiUpdateDsl.UpdateError.HttpError
import nl.marcenschede.starters.akamaiidentitycloud.update.AkamaiUpdateDsl.UpdateError.TechnicalError
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun createAccount(
    config: AkamaiIdentityCloudConfig,
    f: AkamaiCreateDsl.() -> Unit
): Either<AkamaiUpdateDsl.UpdateError, Account> {
    return AkamaiCreateDsl(config).apply(f).execute()
}

class AkamaiCreateDsl(val config: AkamaiIdentityCloudConfig) {
    val attributes = mutableMapOf<String, Any?>()

    fun execute(): Either<AkamaiUpdateDsl.UpdateError, Account> {
        return createValues(attributes)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { postRequest(it) }
    }

    private fun createValues(attributes: MutableMap<String, Any?>): Either<AkamaiUpdateDsl.UpdateError, String> {
        return try {
            Either.Right(config.objectMapper.writeValueAsString(attributes))
        } catch (e: JsonProcessingException) {
            Either.Left(TechnicalError("Parsing json", e))
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

        return Either.Right(HeaderParameterPair(map, httpHeaders))
    }

    class HeaderParameterPair(val map: MultiValueMap<String, String>, val httpHeaders: HttpHeaders)

    private fun postRequest(
        input: HeaderParameterPair
    ): Either<AkamaiUpdateDsl.UpdateError, Account> {

        val headers = input.httpHeaders

        val request = HttpEntity(input.map, headers)
        val s = config.restTemplate.postForEntity(config.createUri, request, String::class.java)

        return when {
            s.statusCode.is2xxSuccessful -> {
                val forEntity = config.singleElementDecoder.invoke(s.body!!)

                when (forEntity.stat) {
                    "ok" -> Either.Right(forEntity.result!!)

                    "error" -> {
                        Either.Left(
                            AkamaiUpdateDsl.UpdateError.AkamaiError(
                                forEntity.error,
                                forEntity.errorDescription
                            )
                        )
                    }

                    "fail" -> {
                        Either.Left(TechnicalError(forEntity.errorDescription ?: ""))
                    }

                    else -> {
                        Either.Left(
                            AkamaiUpdateDsl.UpdateError.AkamaiError(
                                forEntity.error,
                                forEntity.errorDescription
                            )
                        )
                    }
                }
            }

            else -> {
                Either.Left(HttpError(s.statusCode))
            }
        }
    }

}
