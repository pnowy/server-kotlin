package com.github.pnowy.starter.auth

import com.github.pnowy.starter.auth.exceptions.UserNotActivatedException
import com.github.pnowy.starter.account.User
import com.github.pnowy.starter.account.UserRepository
import mu.KotlinLogging
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.stream.Collectors

private val log = KotlinLogging.logger {}

@Component("userDetailsService")
class UserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(email: String?): UserDetails {
        log.debug("Authenticating '{}'", email)

        if (email == null) {
            throw UsernameNotFoundException("Email cannot be null")
        }

        if (EmailValidator().isValid(email, null)) {
            return userRepository.findOneByEmailIgnoreCase(email)
                    .filter { user -> !user.socialOnly }
                    .map { user -> createSpringSecurityUser(email, user) }
                    .orElseThrow { UsernameNotFoundException("User with email $email was not found in the database") }
        } else {
            throw UsernameNotFoundException("User with email $email was not found in the database")
        }
    }

    fun loadSocialUser(email: String): UserDetails {
        log.debug("Authenticating social '{}'", email)

        if (EmailValidator().isValid(email, null)) {
            return userRepository.findOneByEmailIgnoreCase(email)
                    .map { user -> createSpringSecurityUser(email, user) }
                    .orElseThrow { UsernameNotFoundException("User with email $email was not found in the database") }
        } else {
            throw UsernameNotFoundException("User with email $email was not found in the database")
        }
    }

    private fun createSpringSecurityUser(userEmail: String, user: User): org.springframework.security.core.userdetails.User {
        if (!user.activated) {
            throw UserNotActivatedException("User $userEmail was not activated")
        }
        val grantedAuthorities = user.authorities.stream()
                .map { authority -> SimpleGrantedAuthority(authority.name) }
                .collect(Collectors.toList())
        return org.springframework.security.core.userdetails.User(user.email,
                user.password,
                grantedAuthorities)
    }

}
