package com.github.pnowy.starter.exceptions

import org.zalando.problem.Status
import org.zalando.problem.StatusType

data class ErrorDefinition(val code: String, val httpStatus: StatusType,
                           val title: String, val params: Map<String, Any>) {

    constructor(code: String, httpStatus: StatusType) : this(code, httpStatus, code, mapOf())

    fun withHttpStatus(httpStatus: StatusType): ErrorDefinition {
        return ErrorDefinition(this.code, httpStatus, this.title, this.params)
    }

    fun withTitle(title: String): ErrorDefinition {
        return ErrorDefinition(this.code, this.httpStatus, title, this.params)
    }

    fun withOkHttpStatus(): ErrorDefinition {
        return ErrorDefinition(this.code, Status.OK, this.title, this.params)
    }

    fun withParam(key: String, value: Any): ErrorDefinition {
        return withParams(mapOf(Pair(key, value)))
    }

    fun withParams(params: Map<String, Any>): ErrorDefinition {
        return ErrorDefinition(this.code, this.httpStatus, this.title, params)
    }

    fun withIdParam(id: Any): ErrorDefinition {
        return withParam("id", id.toString())
    }

    companion object {

        val EMAIL_ALREADY_EXIST = ErrorDefinition("EMAIL_ALREADY_EXIST", Status.BAD_REQUEST)
        val INVALID_PASSWORD = ErrorDefinition("INVALID_PASSWORD", Status.UNAUTHORIZED)
        val INVALID_SOCIAL_GRANT = ErrorDefinition("INVALID_SOCIAL_GRANT", Status.UNAUTHORIZED)
        val CONSTRAINT_VIOLATION = ErrorDefinition("CONSTRAINT_VIOLATION", Status.BAD_REQUEST)
        val ENTITY_NOT_FOUND = ErrorDefinition("ENTITY_NOT_FOUND", Status.NOT_FOUND)
        val CONCURRENCY_FAILURE = ErrorDefinition("CONCURRENCY_FAILURE", Status.CONFLICT)

        val VALIDATION_ERROR = ErrorDefinition("VALIDATION_ERROR", Status.BAD_REQUEST)
        val ENTITY_ALREADY_EXIST = ErrorDefinition("ENTITY_ALREADY_EXIST", Status.BAD_REQUEST)
        val ENTITY_ILLEGAL_STATE = ErrorDefinition("ENTITY_ILLEGAL_STATE", Status.BAD_REQUEST)
        val APPLICATION_ILLEGAL_STATE = ErrorDefinition("APPLICATION_ILLEGAL_STATE", Status.INTERNAL_SERVER_ERROR)
        val ILLEGAL_ACCESS = ErrorDefinition("ILLEGAL_ACCESS", Status.UNAUTHORIZED)
        val ARGUMENT_NOT_VALID = ErrorDefinition("ARGUMENT_NOT_VALID", Status.BAD_REQUEST)

        fun of(code: String, httpStatus: StatusType): ErrorDefinition {
            return ErrorDefinition(code, httpStatus)
        }

    }

}
