package com.github.pnowy.starter.common

import org.apache.commons.lang3.RandomStringUtils

class RandomUtil {
    companion object {

        private const val DEF_COUNT = 20

        /**
         * Generate a password.
         *
         * @return the generated password
         */
        fun generatePassword() = RandomStringUtils.randomAlphanumeric(DEF_COUNT)

        /**
         * Generate an activation key.
         *
         * @return the generated activation key
         */
        fun generateActivationKey() = RandomStringUtils.randomNumeric(DEF_COUNT)

        /**
         * Generate a reset key.
         *
         * @return the generated reset key
         */
        fun generateResetKey() = RandomStringUtils.randomNumeric(DEF_COUNT)

    }
}
