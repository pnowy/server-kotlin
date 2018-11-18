package com.github.pnowy.starter.exceptions

import java.net.URI

class ErrorConstants {
    companion object {
        val DEFAULT_TYPE = URI.create("about:blank")!!

        const val CODE = "sc-code"
        const val PARAMS = "sc-params"
    }
}
