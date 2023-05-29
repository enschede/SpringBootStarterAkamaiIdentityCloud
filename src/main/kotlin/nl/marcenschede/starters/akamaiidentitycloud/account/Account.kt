package nl.marcenschede.starters.akamaiidentitycloud.account

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.OffsetDateTime
import java.util.*

class ExtendedAccount(
    id: String,
    uuid: UUID,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    created: OffsetDateTime,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    lastUpdated: OffsetDateTime,
    var email: String? = null,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    var emailVerified: OffsetDateTime? = null,
    var mobileNumber: String? = null,
    @JsonDeserialize(using = CustomAkamaiDateTimeDeserializer::class)
    @JsonSerialize(using = CustomAkamaiDateTimeSerializer::class)
    var mobileNumberVerified: OffsetDateTime? = null
) : BaseAccount(id, uuid, created, lastUpdated)

@JsonIgnoreProperties(ignoreUnknown = true)
class SingleExtendedAccountResponse(
    override val result: ExtendedAccount? = null,
) : SingleAccountResponse()

@JsonIgnoreProperties(ignoreUnknown = true)
open class MultiExtendedAccountResponse(
    override val result: List<ExtendedAccount>? = null,
) : MultiAccountResponse()
