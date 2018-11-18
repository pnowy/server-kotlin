package com.github.pnowy.starter.audit

import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuditEventConverter {

    fun convertDataToStrings(data: Map<String, Any>?): Map<String, String> {
        val results = HashMap<String, String>()
        data?.forEach { entry ->
            val key = entry.key
            val value = entry.value
            if (value is WebAuthenticationDetails) {
                results["remoteAddress"] = value.remoteAddress
                results["sessionId"] = value.sessionId
            } else {
                results[key] = Objects.toString(value)
            }
        }
        return results
    }

    /**
     * Convert a PersistentAuditEvent to an AuditEvent
     *
     * @param persistentAuditEvent the event to convert
     * @return the converted list.
     */
    fun convertToAuditEvent(persistentAuditEvent: PersistentAuditEvent?): AuditEvent? {
        return if (persistentAuditEvent == null) {
            null
        } else AuditEvent(persistentAuditEvent.auditEventDate, persistentAuditEvent.principal,
                persistentAuditEvent.auditEventType, convertDataToObjects(persistentAuditEvent.data))
    }

    fun convertToAuditEvent(persistentAuditEvents: Iterable<PersistentAuditEvent>?): List<AuditEvent> {
        return persistentAuditEvents?.map { convertToAuditEvent(it)!! }?.toList() ?: emptyList()
    }

    /**
     * Internal conversion. This is needed to support the current SpringBoot actuator AuditEventRepository interface
     *
     * @param data the data to convert
     * @return a map of String, Object
     */
    fun convertDataToObjects(data: Map<String, String>?): Map<String, Any> {
        val results = HashMap<String, Any>()

        if (data != null) {
            for ((key, value) in data) {
                results[key] = value
            }
        }
        return results
    }


}
