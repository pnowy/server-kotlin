package com.github.pnowy.starter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration

@Configuration
@ConfigurationProperties(prefix = "app")
class AppProperties {
    var cors = CorsConfiguration()

    var security = Security()

    var system = System()
}

class Security {
    var authentication = Authentication()
}

class Authentication {
    var jwt = Jwt()
    var social: Map<String, OAuth2ProviderConfig> = mutableMapOf()
}

class OAuth2ProviderConfig {
    var clientId: String? = null
    var clientSecret: String? = null
}

class System {
    // enable / disable registered account activation email
    var emailActivation: Boolean = false
    var emailFrom: String = "system@softwareclazz.com"
    var baseUrl: String = "http://localhost:3000"
}

class Jwt {
    var base64Secret: String? = null
    var tokenValidityInSeconds = 3600L // 1 hour
    var tokenValidityInSecondsForRememberMe = 2592000L // 30 hours;
}
