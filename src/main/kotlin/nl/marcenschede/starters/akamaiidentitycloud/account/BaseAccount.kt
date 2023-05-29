package nl.marcenschede.starters.akamaiidentitycloud.account

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.OffsetDateTime
import java.util.*


@JsonIgnoreProperties(ignoreUnknown = true)
open class BaseAccount(
    var id: String? = null,
    var uuid: UUID? = null,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    val created: OffsetDateTime? = null,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    var lastUpdated: OffsetDateTime? = null,
)
