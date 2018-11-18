package com.github.pnowy.starter.account

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SocialConnectionRepository : JpaRepository<SocialConnection, Long> {
    fun findByUserIdAndProvider(userId: Long, provider: String): Optional<SocialConnection>
}
