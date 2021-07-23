package it.wlp.reactor.config

import com.github.kiulian.downloader.YoutubeDownloader
import it.wlp.reactor.auth.JWTHeadersExchangeMatcher
import it.wlp.reactor.jwt.JWTReactiveAuthenticationManager
import it.wlp.reactor.jwt.TokenProvider
import it.wlp.reactor.jwt.UnauthorizedAuthenticationEntryPoint
import it.wlp.reactor.service.UserDetailsServiceProvider
import it.wlp.reactor.util.AuthObj
import it.wlp.reactor.util.SecurityUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

@Configuration
class SecurityConfiguration {

    @Autowired
    lateinit var service: UserDetailsServiceProvider

    @Autowired
    lateinit var tokenProvider: TokenProvider

    @Autowired
    lateinit var configSecret: ConfigSecret

    @Autowired
    lateinit var entryPoint: UnauthorizedAuthenticationEntryPoint

    val BEARER = "Bearer ";


    @Bean
    fun youtubeDownloader(): YoutubeDownloader {

        var downloader = YoutubeDownloader();
        return downloader
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =

        http.httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .logout().disable()
            .exceptionHandling()
            .authenticationEntryPoint(entryPoint)
            .and()
            .authorizeExchange()
            .matchers(EndpointRequest.to("health", "info"))
            .permitAll()
            .and()
            .authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS)
            .permitAll()
            .and()
            .authorizeExchange()
            .matchers(EndpointRequest.toAnyEndpoint())
            .hasAuthority("ROLE_ADMIN")
            .and()
            .addFilterAt(webFilter(), SecurityWebFiltersOrder.AUTHORIZATION)
            .authorizeExchange()
            .pathMatchers(
                "/reactive/login",
                "/reactive/users",
                "/reactive/find",
                "/reactive/download"
            ).permitAll()
            .anyExchange().authenticated().and().build();


    @Bean
    fun webFilter(): AuthenticationWebFilter {


        var authenticationWebFilter = AuthenticationWebFilter(repositoryReactiveAuthenticationManager())
        authenticationWebFilter.setServerAuthenticationConverter(this::authenticationConverter)
        authenticationWebFilter.setRequiresAuthenticationMatcher(JWTHeadersExchangeMatcher());
        authenticationWebFilter.setSecurityContextRepository(WebSessionServerSecurityContextRepository())

        AuthObj.configSecret = configSecret

        return authenticationWebFilter;
    }

    @Bean
    fun repositoryReactiveAuthenticationManager(): JWTReactiveAuthenticationManager {
        return JWTReactiveAuthenticationManager(service, passwordEncoder())
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    fun authenticationConverter(serverWebExchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(serverWebExchange)
            .map(SecurityUtils::getTokenFromRequest)
            .filter(Objects::nonNull)
            .filter { it -> it.length > BEARER.length }
            .map { it -> it.substring(BEARER.length, it.length) }
            .filter { token -> !StringUtils.isEmpty(token) }
            .map(tokenProvider::getAuthentication)
            .filter(Objects::nonNull);

    }
}