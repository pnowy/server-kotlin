package com.github.pnowy.starter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.problem.ProblemModule

@Configuration
class JacksonConfiguration {
    @Bean
    internal fun problemModule(): ProblemModule {
        return ProblemModule()
    }
}
