package nl.marcenschede.starters.akamaiidentitycloud.update

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AkamaiSignatureCalculatorDslTest {

    @Test
    fun `when signature is requested then it is created`() {

        val signature = calculateAkamaiSignature("id", "secret", "2023-04-10T13:28:00+02.00", "/blah") {
            header("headerA", "valueA")
            header("headerB", "valueB")
            header("headerC", "valueC")
        }

        assertThat(signature).isEqualTo("Signature id:sEucb8ZwQUZUoPDdExSAM/QHWJQ=")
    }

}