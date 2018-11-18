package com.github.pnowy.starter.auth

import com.github.pnowy.starter.auth.jwt.TokenProvider
import com.github.pnowy.starter.account.User
import com.github.pnowy.starter.account.UserService
import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class AuthService(val tokenProvider: TokenProvider,
                  val authenticationManager: AuthenticationManager,
                  val userService: UserService,
                  val userDetailsService: UserDetailsService) {

    fun usernameAuthentication(userPassRequest: UserPassRequest): JWTToken {
        log.debug { "Username ${userPassRequest.email} / password '***' authentication" }
        val authenticationToken = UsernamePasswordAuthenticationToken(userPassRequest.email, userPassRequest.password)
        val authentication = this.authenticationManager.authenticate(authenticationToken)
        SecurityContextHolder.getContext().authentication = authentication
        val rememberMe = userPassRequest.rememberMe ?: false
        val jwt = tokenProvider.createToken(authentication, rememberMe)
        return JWTToken(jwt)
    }

    fun socialAuthentication(socialLoginCommand: SocialProviderCommand): JWTToken {
        // get access token and user social profile in order to create internal profile
        val userSocialProfile = socialLoginCommand.getUserInfo()
        log.debug { "Social ${userSocialProfile.email} authentication" }
        val optionalUser = userService.findOneByEmail(userSocialProfile.email)
        optionalUser
                .map { user -> userService.updateSocialConnection(user, userSocialProfile) }
                .orElseGet { userService.registerUser(userSocialProfile) }
        val user = userDetailsService.loadSocialUser(userSocialProfile.email)
        val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
        SecurityContextHolder.getContext().authentication = authentication
        val jwt = tokenProvider.createToken(authentication, false)
        return JWTToken(jwt)
    }

    fun refreshToken(user: User): JWTToken {
        val userDetails = userDetailsService.loadUserByUsername(user.email)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        SecurityContextHolder.getContext().authentication = authentication
        val jwt = tokenProvider.createToken(authentication, false)
        return JWTToken(jwt)
    }

}


