package com.github.pnowy.starter.auth.jwt

import com.github.pnowy.starter.config.AppProperties
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.security.Key
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct

private val log = KotlinLogging.logger {}

@Component
class TokenProvider(val appProperties: AppProperties) {

    companion object {
        const val AUTHORITIES_KEY = "auth"
    }

    private var key: Key? = null

    private var tokenValidityInMilliseconds: Long = 3600

    private var tokenValidityInMillisecondsForRememberMe: Long = 2592000

    @PostConstruct
    fun init() {
        val base64Secret = appProperties.security.authentication.jwt.base64Secret
        if (StringUtils.isEmpty(base64Secret)) {
            throw IllegalStateException("The base64 secret key is required!")
        }
        val keyBytes: ByteArray = Decoders.BASE64.decode(base64Secret)
        this.key = Keys.hmacShaKeyFor(keyBytes)
        this.tokenValidityInMilliseconds = 1000 * appProperties.security.authentication.jwt.tokenValidityInSeconds
        this.tokenValidityInMillisecondsForRememberMe = 1000 * appProperties.security.authentication.jwt.tokenValidityInSecondsForRememberMe
    }

    fun createToken(authentication: Authentication, rememberMe: Boolean): String {
        val authorities = authentication.authorities.stream()
                .map { it.authority }
                .collect(Collectors.joining(","))

        val now = Date().time
        val validity: Date
        validity = if (rememberMe) {
            Date(now + this.tokenValidityInMillisecondsForRememberMe)
        } else {
            Date(now + this.tokenValidityInMilliseconds)
        }

        return Jwts.builder()
                .setSubject(authentication.name)
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .body

        val authorities = Arrays.stream(claims[AUTHORITIES_KEY].toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { SimpleGrantedAuthority(it) }
                .collect(Collectors.toList())

        val principal = User(claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(authToken)
            return true
        } catch (e: io.jsonwebtoken.security.SecurityException) {
            log.info("Invalid JWT signature.")
            log.trace("Invalid JWT signature trace: {}", e)
        } catch (e: MalformedJwtException) {
            log.info("Invalid JWT signature.")
            log.trace("Invalid JWT signature trace: {}", e)
        } catch (e: ExpiredJwtException) {
            log.info("Expired JWT token.")
            log.trace("Expired JWT token trace: {}", e)
        } catch (e: UnsupportedJwtException) {
            log.info("Unsupported JWT token.")
            log.trace("Unsupported JWT token trace: {}", e)
        } catch (e: IllegalArgumentException) {
            log.info("JWT token compact of handler are invalid.")
            log.trace("JWT token compact of handler are invalid trace: {}", e)
        }

        return false
    }

}
