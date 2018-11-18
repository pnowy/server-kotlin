package com.github.pnowy.starter.auth.exceptions

import org.springframework.security.core.AuthenticationException

class UserNotActivatedException : AuthenticationException {

    constructor(message: String, t: Throwable) : super(message, t)

    constructor(message: String) : super(message)

}
