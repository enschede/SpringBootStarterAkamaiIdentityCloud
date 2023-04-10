package nl.marcenschede.starters.akamaiidentitycloud

import nl.marcenschede.starters.akamaiidentitycloud.config.AkamaiIdentityCloudConfig
import nl.marcenschede.starters.akamaiidentitycloud.update.createAccount
import nl.marcenschede.starters.akamaiidentitycloud.update.findAccount
import nl.marcenschede.starters.akamaiidentitycloud.update.getAccount
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RestClient(
    val config: AkamaiIdentityCloudConfig
) {

    @GetMapping("/get")
    fun get(): String {
        val findAccount = getAccount(config) {
            this.uuid = "cf5cdfe6-5000-43ef-a7bf-4f67ca4bf6e4"
        }

        return "Hello world: $findAccount"
    }

    @GetMapping("/find")
    fun find(): String {
        val findAccount = findAccount(config) {
            this.filter = "email='marc@marcenschede.nl'"
        }

        return "Hello world: $findAccount"
    }

    @GetMapping("/create")
    fun create(): String {
        val created = createAccount(config) {
            attributes["email"] = "marc@marcenschede.nl"
            attributes["password"] = "Welcome-123"
        }

        return "Hello world: $created"
    }

}