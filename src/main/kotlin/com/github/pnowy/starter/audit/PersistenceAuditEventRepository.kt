package com.github.pnowy.starter.audit

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface PersistenceAuditEventRepository : JpaRepository<PersistentAuditEvent, Long>
