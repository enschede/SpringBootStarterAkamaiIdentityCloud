package nl.marcenschede.starters.akamaiidentitycloud.update

import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun calculateAkamaiSignature(
    clientId: String,
    clientSecret: String,
    dateTime: String,
    endpoint: String, f: AkamaiSignatureCalculatorDsl.() -> Unit
): String {
    return AkamaiSignatureCalculatorDsl(clientId, clientSecret, dateTime, endpoint).apply(f).build()
}

class AkamaiSignatureCalculatorDsl(
    val clientId: String,
    val clientSecret: String,
    dateTime: String,
    endpoint: String
) {

    private var accumulatedString: String

    init {
        accumulatedString = buildString {
            appendLine(endpoint)
            appendLine(dateTime)
        }
    }

    fun build(): String {
        val HMAC_SHA1 = "HmacSHA1"

        return try {
            val sha1Hmac = Mac.getInstance(HMAC_SHA1)
            val keySpec = SecretKeySpec(clientSecret.toByteArray(StandardCharsets.UTF_8), HMAC_SHA1)
            sha1Hmac.init(keySpec)
            val hashValue = sha1Hmac.doFinal(accumulatedString.toByteArray(StandardCharsets.UTF_8))
            val base64Hash = String(Base64.getEncoder().encode(hashValue), StandardCharsets.UTF_8)
            "Signature $clientId:$base64Hash"
        } catch (e: NoSuchAlgorithmException) {
            ""
        } catch (e: InvalidKeyException) {
            ""
        }
    }

    fun header(name: String, value: String) {
        accumulatedString = buildString {
            append(accumulatedString)

            append(name)
            append("=")
            appendLine(value)
        }
    }

}

internal fun createTimestap(config: AkamaiIdentityCloudConfig): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
        .withZone(ZoneId.of("UTC"))
    return formatter.format(config.clock.instant())
}
