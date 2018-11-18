package com.github.pnowy.starter.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.scribejava.apis.FacebookApi
import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.api.BaseApi
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Verb
import com.github.scribejava.core.oauth.OAuth20Service
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Supported social providers
 */
@Suppress("EnumEntryName")
enum class SocialProvider {
    google {
        override fun getServiceInstance(): BaseApi<OAuth20Service> = GoogleApi20.instance()

        override fun getUserInfo(accessToken: OAuth2AccessToken, command: SocialProviderCommand): SocialProfileData {
            val service = command.oAuth2Service
            val oAuthRequest = OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v3/userinfo")
            service.signRequest(accessToken, oAuthRequest)
            val response = service.execute(oAuthRequest)
            val jsonMapper = ObjectMapper()
            val details: Map<String, String> = jsonMapper.readValue(response.body)

            val sub = details["sub"]!!
            val email = details["email"]!!
            log.info { "Get social data email: $email for $sub" }
            return SocialProfileData(provider = this,
                    sub = sub,
                    accessToken = accessToken.accessToken,
                    email = email,
                    displayName = details["name"],
                    firstName = details["given_name"],
                    lastName = details["family_name"],
                    picture = details["picture"],
                    locale = details["locale"]
            )
        }
    },
    facebook {
        override fun getUserInfo(accessToken: OAuth2AccessToken, command: SocialProviderCommand): SocialProfileData {
            val service = command.oAuth2Service
            val oAuthRequest = OAuthRequest(Verb.GET, "https://graph.facebook.com/v3.2/me?fields=id,name,email,first_name,last_name,locale")
            service.signRequest(accessToken, oAuthRequest)
            val response = service.execute(oAuthRequest)
            val jsonMapper = ObjectMapper()
            val details: Map<String, String> = jsonMapper.readValue(response.body)

            val sub = details["id"]!!
            val email = details["email"]!!
            val pictureInfoRequest = OAuthRequest(Verb.GET, "https://graph.facebook.com/$sub/picture?redirect=false")
            service.signRequest(accessToken, pictureInfoRequest)
            val pictureResponse = service.execute(pictureInfoRequest)
            val pictureDetails = jsonMapper.readTree(pictureResponse.body)
            val pictureUrl = pictureDetails.get("data")["url"]
            return SocialProfileData(
                    provider = this,
                    sub = sub,
                    accessToken = accessToken.accessToken,
                    email = email,
                    displayName = details["name"],
                    firstName = details["first_name"],
                    lastName = details["last_name"],
                    picture = pictureUrl?.asText(),
                    locale = details["locale"]
            )
        }

        override fun getServiceInstance(): BaseApi<OAuth20Service> = FacebookApi.instance()
    };

    abstract fun getServiceInstance(): BaseApi<OAuth20Service>

    abstract fun getUserInfo(accessToken: OAuth2AccessToken, command: SocialProviderCommand): SocialProfileData
}
