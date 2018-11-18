package com.github.pnowy.starter.common

import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils

typealias Supplier<T> = () -> T

object AppPreconditions {

    /**
     * Checks if provided expression is true. In case of false the provided exception by supplier will be thrown. It's better
     * to provide the supplier in case of more complicated exception because we can avoid to create that exception until this is
     * really necessary.
     *
     * @param expression a boolean expression
     * @param exceptionSupplier exception supplier
     */
    fun <T> check(expression: Boolean, exceptionSupplier: Supplier<T>) where T: Throwable {
        if (!expression) {
            throw exceptionSupplier()
        }
    }

    fun isNotBlank(value: String): String {
        Preconditions.checkArgument(StringUtils.isNotBlank(value), "The string cannot be blank!")
        return value
    }

}
