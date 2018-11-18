package com.github.pnowy.starter.config

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

private val log = KotlinLogging.logger {}

@Configuration
class WebConfigurer(val appProperties: AppProperties) {

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = appProperties.cors
        if (config.allowedOrigins != null && !config.allowedOrigins!!.isEmpty()) {
            log.debug("Registering CORS filter")
            source.registerCorsConfiguration("/api/**", config)
        }
        return CorsFilter(source)
    }

}
