package com.github.pnowy.starter.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.pnowy.starter.common.WebPreconditions.checkArgument
import com.github.pnowy.starter.common.enumContains
import com.github.pnowy.starter.config.AppProperties
import com.github.pnowy.starter.config.OAuth2ProviderConfig
import com.github.pnowy.starter.exceptions.CustomParametrizedException
import com.github.pnowy.starter.exceptions.ErrorDefinition
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse
import com.google.common.base.Preconditions
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


data class JWTToken(@field:JsonProperty("access_token") val accessToken: String)

data class UserPassRequest(
        @field:NotNull @field:Size(min = 1, max = 50) val email: String?,
        @field:NotNull @field:Size(min = 4, max = 100) val password: String?,
        val rememberMe: Boolean?
)

data class SocialProviderRequest(
        val code: String,
        val clientId: String,
        val redirectUri: String) {

    fun buildCommand(provider: String, appProperties: AppProperties): SocialProviderCommand {
        val providerValue = provider.toLowerCase()
        checkArgument(enumContains<SocialProvider>(providerValue), ErrorDefinition.VALIDATION_ERROR.withTitle("Unknown provider").withParam("provider", providerValue))
        val providerEnum = SocialProvider.valueOf(providerValue)

        // get provider configuration
        val socialProviderConfig = appProperties.security.authentication.social[providerEnum.name]
        Preconditions.checkArgument(socialProviderConfig!!.clientId == clientId)

        return SocialProviderCommand(providerEnum, socialProviderConfig, code, redirectUri)
    }
}

data class SocialProviderCommand(
        val provider: SocialProvider,
        val config: OAuth2ProviderConfig,
        val code: String,
        val redirectUri: String) {

    val oAuth2Service = ServiceBuilder(config.clientId)
            .apiSecret(config.clientSecret)
            .callback(redirectUri)
            .build(provider.getServiceInstance())!!

    fun getUserInfo(): SocialProfileData {
        val accessToken: OAuth2AccessToken = try {
            oAuth2Service.getAccessToken(code)
        } catch (e: OAuth2AccessTokenErrorResponse) {
            throw CustomParametrizedException(ErrorDefinition.INVALID_SOCIAL_GRANT)
        }
        return provider.getUserInfo(accessToken, this)
    }

}

data class SocialProfileData(
        val provider: SocialProvider,
        val sub: String,
        val accessToken: String,
        val email: String,
        val displayName: String?,
        val firstName: String?,
        val lastName: String?,
        val picture: String?,
        val locale: String?
)
