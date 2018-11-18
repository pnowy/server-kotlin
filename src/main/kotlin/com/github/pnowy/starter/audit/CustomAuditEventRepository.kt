package com.github.pnowy.starter.audit

import mu.KotlinLogging
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

private val log = KotlinLogging.logger {}

@Repository
class CustomAuditEventRepository(val persistenceAuditEventRepository: PersistenceAuditEventRepository,
                                 val converter: AuditEventConverter) : AuditEventRepository {
    companion object {
        const val EVENT_DATA_COLUMN_MAX_LENGTH = 255

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun add(event: AuditEvent) {
        val eventData = converter.convertDataToStrings(event.data)
        val persistentAuditEvent = PersistentAuditEvent(
                id = null,
                principal = event.principal,
                auditEventType = event.type,
                auditEventDate = event.timestamp,
                data = truncate(eventData)
        )
        persistenceAuditEventRepository.save(persistentAuditEvent)
    }

    override fun find(principal: String?, after: Instant?, type: String?): List<AuditEvent> {
        // TODO add parameters to search dynamically
        val persistentAuditEvents = persistenceAuditEventRepository.findAll()
        return converter.convertToAuditEvent(persistentAuditEvents)
    }

    /**
     * Truncate event data that might exceed column length.
     */
    private fun truncate(data: Map<String, String>?): Map<String, String> {
        val results = HashMap<String, String>()
        if (data != null) {
            for (entry in data.entries) {
                var value: String? = entry.value
                if (value != null) {
                    val length = value.length
                    if (length > EVENT_DATA_COLUMN_MAX_LENGTH) {
                        value = value.substring(0, EVENT_DATA_COLUMN_MAX_LENGTH)
                        log.warn("Event data for {} too long ({}) has been truncated to {}. Consider increasing column width.",
                                entry.key, length, EVENT_DATA_COLUMN_MAX_LENGTH)
                    }
                    results[entry.key] = value
                }
            }
        }
        return results
    }

}
