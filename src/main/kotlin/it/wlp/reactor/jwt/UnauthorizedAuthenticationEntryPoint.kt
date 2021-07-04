package it.wlp.reactor.jwt

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component("entryPoint")
class UnauthorizedAuthenticationEntryPoint : ServerAuthenticationEntryPoint {
    override fun commence(exchange: ServerWebExchange?, p1: AuthenticationException?): Mono<Void> {
        return Mono.fromRunnable { exchange!!.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED) }
    }
}