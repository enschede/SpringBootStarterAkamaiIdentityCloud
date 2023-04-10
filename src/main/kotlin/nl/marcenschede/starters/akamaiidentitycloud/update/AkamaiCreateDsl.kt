package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.core.JsonProcessingException
import nl.marcenschede.starters.akamaiidentitycloud.account.Account
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_CREATE
import nl.marcenschede.starters.akamaiidentitycloud.update.PersistenceError.TechnicalError
import nl.marcenschede.starters.akamaiidentitycloud.update.SingleResponsePostRequest.HeaderParameterPair
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun createAccount(
    config: AkamaiIdentityCloudConfig,
    f: AkamaiCreateDsl.() -> Unit
): Either<PersistenceError, Account> {
    return AkamaiCreateDsl(config).apply(f).execute()
}

class AkamaiCreateDsl(val config: AkamaiIdentityCloudConfig) {
    val requestor = SingleResponsePostRequest(config)

    val attributes = mutableMapOf<String, Any?>()

    fun execute(): Either<PersistenceError, Account> {
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
        httpHeaders["Authorization"] =
            calculateAkamaiSignature(config.clientId, config.clientSecret, timestamp, ENDPOINT_ENTITY_CREATE) {
                for ((key, value) in treeMap) {
                    header(key, value)
                }
            }

        return Either.Right(HeaderParameterPair(map, httpHeaders))
    }

}
