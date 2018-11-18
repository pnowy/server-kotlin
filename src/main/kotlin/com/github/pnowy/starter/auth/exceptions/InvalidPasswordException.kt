package com.github.pnowy.starter.auth.exceptions

import com.github.pnowy.starter.exceptions.CustomParametrizedException
import com.github.pnowy.starter.exceptions.ErrorDefinition

class InvalidPasswordException : CustomParametrizedException(ErrorDefinition.INVALID_PASSWORD)
