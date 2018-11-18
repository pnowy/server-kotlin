package com.github.pnowy.starter.exceptions

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Problem
import org.zalando.problem.ThrowableProblem
import java.util.HashMap

open class CustomParametrizedException : AbstractThrowableProblem {

    override fun getCause(): Exceptional? {
        return if (super.cause != null) super.cause as ThrowableProblem else null
    }

    constructor(definition: ErrorDefinition)
            : super(ErrorConstants.DEFAULT_TYPE, definition.title, definition.httpStatus, null, null, null, toProblemParameters(definition))

    constructor(problem: Problem)
        : super(problem.type, problem.title, problem.status, problem.detail, problem.instance, null, problem.parameters)

    companion object {
        private fun toProblemParameters(definition: ErrorDefinition): Map<String, Any> {
            val parameters = HashMap<String, Any>()
            parameters[ErrorConstants.CODE] = definition.code
            if (definition.params.isNotEmpty()) {
                parameters[ErrorConstants.PARAMS] = definition.params
            }
            return parameters
        }
    }

}
