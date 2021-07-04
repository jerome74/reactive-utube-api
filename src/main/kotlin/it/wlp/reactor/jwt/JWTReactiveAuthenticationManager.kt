package it.wlp.reactor.jwt

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class JWTReactiveAuthenticationManager(val userDetailsService: ReactiveUserDetailsService, val passwordEncoder: PasswordEncoder) : ReactiveAuthenticationManager {

    val log = LoggerFactory.getLogger(JWTReactiveAuthenticationManager::class.java);

    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        if (authentication!!.isAuthenticated) {
            return Mono.just(authentication);
        }

        return Mono.just(authentication)
            .switchIfEmpty(Mono.defer(this::raiseBadCredentials))
            .cast(UsernamePasswordAuthenticationToken::class.java)
                .flatMap(this::authenticateToken)
                .onErrorResume { raiseBadCredentials() }
                .filter { passwordEncoder.matches(authentication.credentials as String,it.password) }
            .switchIfEmpty(Mono.defer(this::raiseBadCredentials))
            .map{UsernamePasswordAuthenticationToken(authentication.principal, authentication.credentials, it.authorities)};


    }

     fun <T> raiseBadCredentials() : Mono<T> {
        return Mono.error(BadCredentialsException("Invalid Credentials"));
    }

    fun authenticateToken(authenticationToken : UsernamePasswordAuthenticationToken) : Mono<UserDetails> {
        val username = authenticationToken.name

        log.info("checking authentication for user $username");

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("authenticated user $username, setting security context");
            return this.userDetailsService.findByUsername(username);
        }

        return Mono.empty();
    }

}