package com.github.pnowy.starter.exceptions

import org.springframework.dao.ConcurrencyFailureException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.DefaultProblem
import org.zalando.problem.Problem
import org.zalando.problem.spring.web.advice.ProblemHandling
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait
import org.zalando.problem.violations.ConstraintViolationProblem
import javax.servlet.http.HttpServletRequest

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 */
@ControllerAdvice
class ExceptionTranslator : ProblemHandling, SecurityAdviceTrait {

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed
     */
    override fun process(entity: ResponseEntity<Problem>, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = entity.body
        val builder = Problem.builder()
                .withType(problem?.type)
                .withStatus(problem?.status)
                .withTitle(problem?.title)
                .with("path", request.getNativeRequest(HttpServletRequest::class.java)!!.requestURI)

        when(problem) {
            is ConstraintViolationProblem ->
                builder
                        .with(ErrorConstants.PARAMS, problem.violations)
                        .with(ErrorConstants.CODE, ErrorDefinition.CONSTRAINT_VIOLATION.code)
            is DefaultProblem ->
                builder.withCause(problem.cause)
        }

        builder
                .withDetail(problem?.detail)
                .withInstance(problem?.instance)
        problem?.parameters?.forEach { key, value -> builder.with(key, value) }
        return ResponseEntity(builder.build(), entity.headers, entity.statusCode)
    }

    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, request: NativeWebRequest): ResponseEntity<Problem> {
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors.map { FieldErrorDto(it.objectName, it.field, it.code) }

        val problem = Problem.builder()
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("Method argument not valid")
                .withStatus(defaultConstraintViolationStatus())
                .with(ErrorConstants.CODE, ErrorDefinition.CONSTRAINT_VIOLATION.code)
                .with(ErrorConstants.PARAMS, fieldErrors)
                .build()
        return create(ex, problem, request)
    }

    @ExceptionHandler
    fun handleNoSuchElementException(ex: NoSuchElementException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = Problem.builder()
                .withStatus(ErrorDefinition.ENTITY_NOT_FOUND.httpStatus)
                .with(ErrorConstants.CODE, ErrorDefinition.ENTITY_NOT_FOUND.code)
                .build()
        return create(ex, problem, request)
    }

    @ExceptionHandler
    fun handleConcurrencyFailure(ex: ConcurrencyFailureException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = Problem.builder()
                .withStatus(ErrorDefinition.CONCURRENCY_FAILURE.httpStatus)
                .with(ErrorConstants.CODE, ErrorDefinition.CONCURRENCY_FAILURE.code)
                .build()
        return create(ex, problem, request)
    }

}
