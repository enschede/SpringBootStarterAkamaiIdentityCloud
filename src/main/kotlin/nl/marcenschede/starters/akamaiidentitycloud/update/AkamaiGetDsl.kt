package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import arrow.core.flatMap
import nl.marcenschede.starters.akamaiidentitycloud.account.BaseAccount
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.config.ENDPOINT_ENTITY_GET
import nl.marcenschede.starters.akamaiidentitycloud.update.SingleResponsePostRequest.HeaderParameterPair
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun getAccount(config: AkamaiIdentityCloudConfig, f: AkamaiGetDsl.() -> Unit): Either<PersistenceError, BaseAccount?> {
    return AkamaiGetDsl(config).apply(f).execute()
}

class AkamaiGetDsl(val config: AkamaiIdentityCloudConfig) {
    val requestor = SingleResponsePostRequest(config)
    lateinit var uuid: String

    fun execute(): Either<PersistenceError, BaseAccount?> {
        return createFindValues(uuid)
            .flatMap { createFormParams(it) }
            .flatMap { createHeaders(it) }
            .flatMap { requestor.postRequest(it, ENDPOINT_ENTITY_GET) }
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
}
