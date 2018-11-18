package com.github.pnowy.starter.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class SecurityUtils {

    companion object {

        fun getCurrentUserEmail(): Optional<String> {
            val securityContext = SecurityContextHolder.getContext()
            val email: String? = securityContext.authentication?.let {
                when {
                    it.principal is UserDetails -> (it.principal as UserDetails).username
                    it.principal is String -> it.principal as String
                    else -> null
                }
            }
            return Optional.ofNullable(email)
        }

        fun getCurrentUserJWT(): Optional<String> {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable(securityContext.authentication)
                    .filter { authentication -> authentication.credentials is String }
                    .map { authentication -> authentication.credentials as String }
        }

        fun isAuthenticated(): Boolean {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable(securityContext.authentication)
                    .map { authentication ->
                        authentication.authorities.stream()
                                .noneMatch { grantedAuthority -> grantedAuthority.authority == AuthoritiesConstants.ROLE_ANONYMOUS }
                    }
                    .orElse(false)
        }

        fun isCurrentUserInRole(authority: String): Boolean {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable(securityContext.authentication)
                    .map<Boolean> { authentication ->
                        authentication.authorities.stream()
                                .anyMatch { grantedAuthority -> grantedAuthority.authority == authority }
                    }
                    .orElse(false)
        }

    }

}
