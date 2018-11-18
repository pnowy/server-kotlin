package com.github.pnowy.starter.auth

import com.github.pnowy.starter.auth.jwt.JWTFilter
import com.github.pnowy.starter.config.AppProperties
import io.micrometer.core.annotation.Timed
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class AuthController(val authService: AuthService,
                     val appProperties: AppProperties) {

    @Timed
    @PostMapping("/auth")
    fun authorize(@Valid @RequestBody userPassRequest: UserPassRequest): ResponseEntity<JWTToken> {
        val jwtToken = authService.usernameAuthentication(userPassRequest)
        return ResponseEntity(jwtToken, buildAuthHeader(jwtToken), HttpStatus.OK)
    }

    @PostMapping("/auth/{provider}")
    fun socialLogging(@PathVariable provider: String, @RequestBody socialRequest: SocialProviderRequest): ResponseEntity<JWTToken> {
        val command = socialRequest.buildCommand(provider, appProperties)
        val jwtToken = authService.socialAuthentication(command)
        return ResponseEntity(jwtToken, buildAuthHeader(jwtToken), HttpStatus.OK)
    }

    private fun buildAuthHeader(jwtToken: JWTToken): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer ${jwtToken.accessToken}")
        return httpHeaders
    }

}
