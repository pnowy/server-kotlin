package com.github.pnowy.starter.account

import com.github.pnowy.starter.auth.AuthoritiesConstants
import com.github.pnowy.starter.auth.exceptions.InvalidPasswordException
import com.github.pnowy.starter.auth.SecurityUtils
import com.github.pnowy.starter.auth.SocialProfileData
import com.github.pnowy.starter.common.MailService
import com.github.pnowy.starter.common.RandomUtil
import com.github.pnowy.starter.config.AppProperties
import com.github.pnowy.starter.exceptions.EntityNotFoundException
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class UserService(val userRepository: UserRepository,
                  val passwordEncoder: PasswordEncoder,
                  val authorityRepository: AuthorityRepository,
                  val socialConnectionRepository: SocialConnectionRepository,
                  val cacheManager: CacheManager,
                  val appProperties: AppProperties,
                  val mailService: MailService) {

    companion object {
        const val FALLBACK_LOCALE = "en"

        const val USERS_BY_EMAIL_CACHE = "usersByEmail"
    }

    /**
     * Username/password user registration
     */
    @Transactional
    fun registerUser(userDto: UserDto, password: String): User {
        userRepository.findOneByEmailIgnoreCase(userDto.email!!).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw EmailAlreadyExistException()
            }
        }
        val encryptedPassword = passwordEncoder.encode(password)
        val isActivationRequired = appProperties.system.emailActivation

        val authorities = HashSet<Authority>()
        authorityRepository.findById(AuthoritiesConstants.ROLE_USER).ifPresent { authority -> authorities.add(authority) }
        val newUser = User(
                password = encryptedPassword,
                firstName = userDto.firstName,
                lastName = userDto.lastName,
                email = userDto.email!!.toLowerCase(),
                socialOnly = false,
                imageUrl = userDto.imageUrl,
                locale = userDto.locale ?: FALLBACK_LOCALE,
                activated = !isActivationRequired,
                activationKey = RandomUtil.generateActivationKey(),
                authorities = authorities
        )
        userRepository.save(newUser)
        this.clearUserCaches(newUser)
        if (isActivationRequired) {
            mailService.sendActivationEmail(newUser)
        }
        log.debug("Created Information for user: {}", newUser)
        return newUser
    }

    /**
     * Social user registration.
     */
    @Transactional
    fun registerUser(socialProfile: SocialProfileData): User {
        userRepository.findOneByEmailIgnoreCase(socialProfile.email).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw EmailAlreadyExistException()
            }
        }

        val encryptedPassword = passwordEncoder.encode(RandomStringUtils.randomAlphabetic(32))
        val isActivationRequired = appProperties.system.emailActivation

        val authorities = HashSet<Authority>()
        authorityRepository.findById(AuthoritiesConstants.ROLE_USER).ifPresent { authority -> authorities.add(authority) }
        val newUser = User(
                password = encryptedPassword,
                firstName = socialProfile.firstName,
                lastName = socialProfile.lastName,
                email = socialProfile.email.toLowerCase(),
                socialOnly = true,
                imageUrl = socialProfile.picture,
                locale = socialProfile.locale ?: FALLBACK_LOCALE,
                activated = !isActivationRequired,
                activationKey = RandomUtil.generateActivationKey(),
                authorities = authorities,
                socialConnections = mutableListOf(SocialConnection(socialProfile))
        )
        userRepository.save(newUser)
        this.clearUserCaches(newUser)
        if (isActivationRequired) {
            mailService.sendActivationEmail(newUser)
        }
        log.debug("Created Information for user: {}", newUser)
        return newUser
    }

    @Transactional
    fun updateSocialConnection(user: User, socialProfile: SocialProfileData) {
        log.debug { "Updating social connection for user: ${user.id}" }
        socialConnectionRepository.findByUserIdAndProvider(user.id!!, socialProfile.provider.name)
                .map { connection -> connection.apply(socialProfile) }
                .orElseGet {
                    val newConnection = SocialConnection(user, socialProfile)
                    user.socialConnections.add(newConnection)
                    newConnection
                }
        userRepository.save(user)
    }

    @Transactional
    fun activateRegistration(key: String): Optional<User> {
        log.debug("Activating user for activation key {}", key)
        return userRepository.findOneByActivationKey(key)
                .map { user ->
                    // activate given user for the registration key
                    user.activated = true
                    user.activationKey = null
                    this.clearUserCaches(user)
                    log.debug("Activated user: {}", user.email)
                    user
                }
    }

    @Transactional
    fun changePassword(currentClearTextPassword: String, newPassword: String) {
        SecurityUtils.getCurrentUserEmail()
                .flatMap { userRepository.findOneByEmailIgnoreCase(it) }
                .ifPresent { user ->
                    val currentEncryptedPassword = user.password
                    if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                        throw InvalidPasswordException()
                    }
                    val encryptedPassword = passwordEncoder.encode(newPassword)
                    user.password = encryptedPassword
                    this.clearUserCaches(user)
                    log.info { "Changed password for user: $user" }
                }
    }

    private fun removeNonActivatedUser(existingUser: User): Boolean {
        if (existingUser.activated) {
            return false
        }
        userRepository.delete(existingUser)
        userRepository.flush()
        this.clearUserCaches(existingUser)
        return true
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(): Optional<User> {
        return SecurityUtils.getCurrentUserEmail().flatMap { userRepository.findOneByEmailIgnoreCase(it) }
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = [USERS_BY_EMAIL_CACHE], key = "#email", unless = "#result == null")
    fun findOneByEmail(email: String): Optional<User> {
        return userRepository.findOneByEmailIgnoreCase(email)
    }

    @Transactional
    fun initUserPasswordReset(email: String): User {
        val user = userRepository
                .findOneByEmailIgnoreCase(email)
                .filter { it.activated }
                .filter { !it.socialOnly }
                .map { user ->
                    user.resetKey = RandomUtil.generateResetKey()
                    user.resetDate = Instant.now()
                    this.clearUserCaches(user)
                    user
                }
                .orElseThrow { EntityNotFoundException(User::class.simpleName!!, email) }
        mailService.sendPasswordResetMail(user)
        return user
    }

    @Transactional
    fun completePasswordReset(newPassword: String, key: String): User {
        log.debug("Reset user password for reset key {}", key)
        return userRepository.findOneByResetKey(key)
                .filter { user -> user.resetKey != null && user.resetDate != null }
                .filter { user -> user.resetDate!!.isAfter(Instant.now().minusSeconds(appProperties.security.authentication.jwt.tokenValidityInSeconds)) }
                .map { user ->
                    user.password = passwordEncoder.encode(newPassword)
                    user.resetKey = null
                    user.resetDate = null
                    this.clearUserCaches(user)
                    user
                }.orElseThrow { EntityNotFoundException("No user was found for this reset key") }
    }

    @Transactional
    fun updateProfile(firstName: String?, lastName: String?, email: String): User {
        val userCurrentEmail = SecurityUtils.getCurrentUserEmail().orElseThrow { AuthenticationCredentialsNotFoundException("User is not authenticated") }
        val user = userRepository.findOneByEmailIgnoreCase(userCurrentEmail)
                .map { user ->
                    user.apply {
                        this.firstName = firstName
                        this.lastName = lastName
                        this.email = email
                    }
                    this.clearUserCaches(user)
                    user
                }.orElseThrow { EntityNotFoundException(User::class.simpleName!!, email) }
        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun getAllUsers(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    private fun clearUserCaches(user: User) {
        Objects.requireNonNull<Cache>(cacheManager.getCache(USERS_BY_EMAIL_CACHE)).evict(user.email)
    }

}
