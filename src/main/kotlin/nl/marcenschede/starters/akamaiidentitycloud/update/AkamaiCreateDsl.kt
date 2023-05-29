package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.reduceOrNull
import com.fasterxml.jackson.core.JsonProcessingException
import nl.marcenschede.starters.akamaiidentitycloud.account.BaseAccount
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_CREATE
import nl.marcenschede.starters.akamaiidentitycloud.update.PersistenceError.TechnicalError
import nl.marcenschede.starters.akamaiidentitycloud.update.SingleResponsePostRequest.HeaderParameterPair
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

fun createAccount(
    config: AkamaiIdentityCloudConfig,
    f: AkamaiCreateDsl.() -> Unit
): Either<PersistenceError, BaseAccount> {
    return AkamaiCreateDsl(config).apply(f).execute()
}

class AkamaiCreateDsl(val config: AkamaiIdentityCloudConfig) {
    val requestor = SingleResponsePostRequest(config)

    val attributes = mutableMapOf<String, Any?>()

    fun execute(): Either<PersistenceError, BaseAccount> {
        return createValues(attributes)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { requestor.postRequest(it, ENDPOINT_ENTITY_CREATE) }
    }

    private fun createValues(attributes: MutableMap<String, Any?>): Either<PersistenceError, String> {
        return try {
            Either.Right(config.objectMapper.writeValueAsString(attributes))
        } catch (e: JsonProcessingException) {
            Either.Left(TechnicalError("Parsing json", e))
        }
    }

    private fun createFormParams(attributes: String): Either.Right<Map<String, String>> {
        // marc Deze map levert lijsten op, dat is niet de bedoeling
        val parameters: MutableMap<String, String> = mutableMapOf()
        parameters["type_name"] = "user"
        parameters["attributes"] = attributes
        parameters["include_record"] = true.toString()

        return Either.Right(parameters)
    }

    private fun createHeaders(map: Map<String, String>): Either.Right<HeaderParameterPair> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val timestamp = createTimestap(config)
        httpHeaders["Date"] = timestamp
        httpHeaders["Authorization"] =
            calculateAkamaiSignature(config.clientId, config.clientSecret, timestamp, ENDPOINT_ENTITY_CREATE) {
                for ((key, value) in map) {
                    header(key, value)
                }
            }

        val multiValueMap = map.entries
            .reduceOrNull(
                initial = {
                    val valueMap = LinkedMultiValueMap<String, String>()
                    valueMap.add(it.key, it.value)
                    valueMap
                },
                operation = { b, a ->
                    b.add(a.key, a.value)
                    b
                }
            ) as MultiValueMap<String, String>

        return Either.Right(HeaderParameterPair(multiValueMap, httpHeaders))
    }

}
