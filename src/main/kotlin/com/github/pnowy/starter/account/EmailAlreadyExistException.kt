package com.github.pnowy.starter.account

import com.github.pnowy.starter.exceptions.CustomParametrizedException
import com.github.pnowy.starter.exceptions.ErrorDefinition

class EmailAlreadyExistException : CustomParametrizedException(ErrorDefinition.EMAIL_ALREADY_EXIST)
