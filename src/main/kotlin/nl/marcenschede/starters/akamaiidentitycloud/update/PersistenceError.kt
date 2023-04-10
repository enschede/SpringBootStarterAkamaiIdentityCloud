package nl.marcenschede.starters.akamaiidentitycloud.update

import org.springframework.http.HttpStatus

sealed class PersistenceError {
    data class TechnicalError(val message: String, val e: Throwable? = null) : PersistenceError()
    data class AkamaiError(val error: String?, val errorDescription: String?) : PersistenceError()
    data class HttpError(val status: HttpStatus) : PersistenceError()

}