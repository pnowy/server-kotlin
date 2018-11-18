package com.github.pnowy.starter.auth

import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

@Component
class SpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        return Optional.of(SecurityUtils.getCurrentUserEmail().orElse(AuthoritiesConstants.SYSTEM_ACCOUNT))
    }
}
