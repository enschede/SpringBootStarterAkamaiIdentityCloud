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

fun calculateAkamaiSignature(f: AkamaiSignatureCalculatorDsl.() -> Unit): String {
    return AkamaiSignatureCalculatorDsl().apply(f).build()
}

class AkamaiSignatureCalculatorDsl {
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var dateTime: String
    lateinit var endpoint: String
    lateinit var params: Map<String, String>

    fun build(): String {
        val HMAC_SHA1 = "HmacSHA1"
        val toHash = buildString {
            appendLine(endpoint)
            appendLine(dateTime)

            for ((key, value) in params) {
                append(key)
                append("=")
                appendLine(value)
            }
        }

        return try {
            val sha1Hmac = Mac.getInstance(HMAC_SHA1)
            val keySpec = SecretKeySpec(clientSecret.toByteArray(StandardCharsets.UTF_8), HMAC_SHA1)
            sha1Hmac.init(keySpec)
            val hashValue = sha1Hmac.doFinal(toHash.toByteArray(StandardCharsets.UTF_8))
            val base64Hash = String(Base64.getEncoder().encode(hashValue), StandardCharsets.UTF_8)
            "Signature $clientId:$base64Hash"
        } catch (e: NoSuchAlgorithmException) {
            ""
        } catch (e: InvalidKeyException) {
            ""
        }

    }

}

internal fun createTimestap(config: AkamaiIdentityCloudConfig): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
        .withZone(ZoneId.of("UTC"))
    return formatter.format(config.clock.instant())
}
