package com.github.pnowy.starter.account

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

open class UserDto {
    var id: Long? = null

    @Size(max = 50)
    var firstName: String? = null

    @Size(max = 50)
    var lastName: String? = null

    @Email
    @NotBlank
    @Size(min = 5, max = 254)
    var email: String? = null

    @Size(max = 256)
    var imageUrl: String? = null

    var activated = false

    @Size(min = 2, max = 6)
    var locale: String? = null

    var createdBy: String? = null

    var createdDate: Instant? = null

    var lastModifiedBy: String? = null

    var lastModifiedDate: Instant? = null

    var authorities: Set<String>? = null

    var socialConnections: Set<SocialConnectionDto>? = null

    var socialOnly: Boolean = false

    @JsonProperty
    fun hasSocialConnections() = socialConnections?.isNotEmpty() ?: false

    @JsonProperty
    fun name() = "$firstName $lastName"

    constructor()

    constructor(user: User) {
        this.id = user.id
        this.firstName = user.firstName
        this.lastName = user.lastName
        this.email = user.email
        this.activated = user.activated
        this.imageUrl = user.imageUrl
        this.locale = user.locale
        this.createdBy = user.createdBy
        this.createdDate = user.createdDate
        this.lastModifiedBy = user.lastModifiedBy
        this.lastModifiedDate = user.lastModifiedDate
        this.socialOnly = user.socialOnly
        this.authorities = user.authorities.asSequence().map { it.name }.toSet()
        this.socialConnections = user.socialConnections.map { SocialConnectionDto(it.provider) }.toSet()
    }
}

data class SocialConnectionDto(val provider: String)

class RegisterUserRequest : UserDto() {

    companion object {
        const val PASSWORD_MIN_LENGTH = 4

        const val PASSWORD_MAX_LENGTH = 100
    }

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    var password: String? = null
}

class ChangePasswordRequest(@field:NotNull var currentPassword: String, @field:NotNull var newPassword: String)

class ResetPasswordInitRequest(@field:NotNull var email: String)

class ResetPasswordFinishRequest(@field:NotNull var key: String, @field:NotNull var newPassword: String)

class ModifyProfileRequest(var firstName: String?, var lastName: String?, @field:NotNull @field:Email var email: String)

class ModifyProfileResponse(var user: UserDto, @field:JsonProperty("access_token") var accessToken: String)
