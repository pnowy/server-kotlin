package com.github.pnowy.starter.common

import com.github.pnowy.starter.exceptions.CustomParametrizedException
import com.github.pnowy.starter.exceptions.ErrorDefinition
import org.zalando.problem.Status

object WebPreconditions {

    fun checkArgument(expression: Boolean, code: String, httpStatus: Status) {
        if (!expression) {
            throw CustomParametrizedException(ErrorDefinition.of(code, httpStatus))
        }
    }

    fun checkArgument(expression: Boolean, errorDefinition: ErrorDefinition) {
        if (!expression) {
            throw CustomParametrizedException(errorDefinition)
        }
    }

}
