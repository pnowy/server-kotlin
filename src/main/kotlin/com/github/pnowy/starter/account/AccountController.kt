package com.github.pnowy.starter.account

import com.github.pnowy.starter.auth.AuthService
import com.github.pnowy.starter.auth.AuthoritiesConstants
import com.github.pnowy.starter.auth.SecurityUtils
import com.github.pnowy.starter.auth.exceptions.InvalidPasswordException
import com.github.pnowy.starter.exceptions.EntityNotFoundException
import org.apache.commons.lang3.StringUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class AccountController(val userService: UserService, val authService: AuthService) {

    @PostMapping("/account/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerAccount(@Valid @RequestBody registerUserRequest: RegisterUserRequest) {
        if (!checkPasswordLength(registerUserRequest.password)) {
            throw InvalidPasswordException()
        }
        userService.registerUser(registerUserRequest, registerUserRequest.password!!)
    }

    @GetMapping("/account")
    fun getAccount(): UserDto {
        return userService.getCurrentUser()
                .map { UserDto(it) }
                .orElseThrow { EntityNotFoundException("User could not be found", SecurityUtils.getCurrentUserEmail()) }
    }

    @GetMapping("/account/activate")
    fun activateAccount(@RequestParam(value = "key") key: String) {
        val user = userService.activateRegistration(key)
        if (!user.isPresent) {
            throw EntityNotFoundException("No user was found for this activation key")
        }
    }

    @PostMapping("/account/change-password")
    fun changePassword(@Valid @RequestBody passwordChangeDto: ChangePasswordRequest) {
        if (!checkPasswordLength(passwordChangeDto.newPassword)) {
            throw InvalidPasswordException()
        }
        userService.changePassword(passwordChangeDto.currentPassword, passwordChangeDto.newPassword)
    }

    @PostMapping("/account/reset-password/init")
    fun requestPasswordReset(@Valid @RequestBody reset: ResetPasswordInitRequest) {
        userService.initUserPasswordReset(reset.email)
    }

    @PostMapping("/account/reset-password/finish")
    fun finishPasswordReset(@Valid @RequestBody keyAndPassword: ResetPasswordFinishRequest) {
        if (!checkPasswordLength(keyAndPassword.newPassword)) {
            throw InvalidPasswordException()
        }
        userService.completePasswordReset(keyAndPassword.newPassword, keyAndPassword.key)
    }

    @PatchMapping("/account")
    fun patchProfile(@Valid @RequestBody modifyProfileRequest: ModifyProfileRequest): ModifyProfileResponse {
        val updatedUser = userService.updateProfile(modifyProfileRequest.firstName, modifyProfileRequest.lastName, modifyProfileRequest.email)
        val newToken = authService.refreshToken(updatedUser)
        return ModifyProfileResponse(UserDto(updatedUser), newToken.accessToken)
    }

    @GetMapping("/accounts")
    @Secured(AuthoritiesConstants.ROLE_ADMIN)
    fun getAccounts(pageable: Pageable): Page<UserDto> {
        return userService.getAllUsers(pageable).map { UserDto(it) }
    }

    private fun checkPasswordLength(password: String?): Boolean {
        return StringUtils.isNotEmpty(password) &&
                password!!.length >= RegisterUserRequest.PASSWORD_MIN_LENGTH &&
                password.length <= RegisterUserRequest.PASSWORD_MAX_LENGTH
    }

}
