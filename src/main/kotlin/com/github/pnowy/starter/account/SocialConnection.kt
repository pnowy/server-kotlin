package com.github.pnowy.starter.account

import com.github.pnowy.starter.auth.SocialProfileData
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "sc_social_user_connection")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class SocialConnection(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
        @SequenceGenerator(name = "sequenceGenerator")
        var id: Long? = null,

        @Column(name = "user_id", insertable = false, updatable = false)
        var userId: Long?,

        @Column(name = "provider", nullable = false)
        var provider: String,

        @Column(name = "external_id", nullable = false)
        var externalId: String,

        @Column(name = "access_token", nullable = false)
        var accessToken: String,

        @Column(name = "display_name")
        var displayName: String?,

        @Column(name = "first_name")
        var firstName: String?,

        @Column(name = "last_name")
        var lastName: String?,

        @Column(name = "image_url")
        var imageUrl: String?
) : Serializable {

    fun apply(socialAccountData: SocialProfileData): SocialConnection {
        this.externalId = socialAccountData.sub
        this.accessToken = socialAccountData.accessToken
        this.firstName = socialAccountData.firstName
        this.lastName = socialAccountData.lastName
        this.displayName = socialAccountData.displayName
        this.imageUrl = socialAccountData.picture
        return this
    }

    constructor(socialData: SocialProfileData) : this(null, socialData)

    constructor(user: User?, socialData: SocialProfileData) : this(null, user?.id, socialData.provider.name, socialData.sub, socialData.accessToken,
            socialData.displayName, socialData.firstName, socialData.lastName, socialData.picture)
}
