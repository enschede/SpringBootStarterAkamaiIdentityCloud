package nl.marcenschede.starters.akamaiidentitycloud.update

import arrow.core.Either
import nl.marcenschede.starters.akamaiidentitycloud.account.BaseAccount
import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.util.MultiValueMap
import java.net.URI

class SingleResponsePostRequest(val config: AkamaiIdentityCloudConfig) {

    fun postRequest(
        input: HeaderParameterPair,
        endpoint: String,
    ): Either<PersistenceError, BaseAccount> {

        val request = HttpEntity(input.map, input.httpHeaders)
        val response = config.restTemplate.postForEntity(URI(config.url + endpoint), request, String::class.java)

        return when {
            response.statusCode.is2xxSuccessful -> {
                val forEntity = config.singleElementDecoder.invoke(config.objectMapper, response.body!!)

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

    class HeaderParameterPair(val map: MultiValueMap<String, String>, val httpHeaders: HttpHeaders)

}