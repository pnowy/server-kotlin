package com.github.pnowy.starter.audit

import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "sc_persistent_audit_event")
data class PersistentAuditEvent(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
        @SequenceGenerator(name = "sequenceGenerator")
        @Column(name = "event_id")
        var id: Long? = null,

        @NotNull
        @Column(nullable = false)
        var principal: String,

        @Column(name = "event_date")
        var auditEventDate: Instant,

        @Column(name = "event_type")
        var auditEventType: String,

        @ElementCollection(fetch = FetchType.EAGER)
        @MapKeyColumn(name = "name")
        @Column(name = "value")
        @CollectionTable(name = "sc_persistent_audit_evt_data", joinColumns = [JoinColumn(name = "event_id")])
        var data: Map<String, String> = mutableMapOf()
)
