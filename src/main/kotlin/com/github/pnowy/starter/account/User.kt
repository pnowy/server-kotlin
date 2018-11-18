package com.github.pnowy.starter.account

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.pnowy.starter.domain.AbstractAuditingEntity
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "sc_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
        @SequenceGenerator(name = "sequenceGenerator")
        var id: Long? = null,

        @JsonIgnore
        @NotNull
        @Size(min = 60, max = 60)
        @Column(name = "password_hash", length = 60, nullable = false)
        var password: String,
        @Email
        @Size(min = 5, max = 254)
        @Column(length = 254, unique = true)
        var email: String,
        @NotNull
        @Column(name = "social_only")
        var socialOnly: Boolean,
        @JsonIgnore
        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "sc_user_authority",
                joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
                inverseJoinColumns = [JoinColumn(name = "authority_name", referencedColumnName = "name")])
        @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
        @BatchSize(size = 20)
        var authorities: Set<Authority> = hashSetOf(),

        @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
        @JoinColumn(name = "user_id", nullable = false)
        var socialConnections: MutableList<SocialConnection> = mutableListOf(),

        @Size(max = 50)
        @Column(name = "first_name", length = 50)
        var firstName: String?,
        @Size(max = 50)
        @Column(name = "last_name", length = 50)
        var lastName: String?,
        @NotNull
        @Column(nullable = false)
        var activated: Boolean = false,
        @Size(min = 2, max = 6)
        @Column(name = "locale", length = 6)
        val locale: String,
        @Size(max = 256)
        @Column(name = "image_url", length = 256)
        val imageUrl: String?,
        @Size(max = 20)
        @Column(name = "activation_key", length = 20)
        @JsonIgnore
        var activationKey: String?,
        @Size(max = 20)
        @Column(name = "reset_key", length = 20)
        @JsonIgnore
        var resetKey: String? = null,
        @Column(name = "reset_date")
        var resetDate: Instant? = null

) : AbstractAuditingEntity() {
    override fun toString(): String {
        return "User{$id, $email}"
    }
}
