package it.wlp.reactor.auth

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class JWTHeadersExchangeMatcher : ServerWebExchangeMatcher {
    override fun matches(exchange: ServerWebExchange?): Mono<ServerWebExchangeMatcher.MatchResult> {
        val request = Mono.justOrEmpty(exchange).map(ServerWebExchange::getRequest)

        /* Check for header "Authorization" */
        return request.map(ServerHttpRequest::getHeaders)
            .filter{h -> h.containsKey(HttpHeaders.AUTHORIZATION)}
        .flatMap{ServerWebExchangeMatcher.MatchResult.match()}
        .switchIfEmpty(ServerWebExchangeMatcher.MatchResult.notMatch());
    }
}