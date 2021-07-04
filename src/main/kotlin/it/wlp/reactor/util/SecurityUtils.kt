package it.wlp.reactor.util

import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.User
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

object SecurityUtils {


    fun getTokenFromRequest(serverWebExchange : ServerWebExchange ) : String {
        val token = serverWebExchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);
        return token.orEmpty()
    }

    fun getUserFromRequest(serverWebExchange : ServerWebExchange) : Mono<String> {
        return serverWebExchange.getPrincipal<UsernamePasswordAuthenticationToken>()
            .cast(UsernamePasswordAuthenticationToken::class.java)
                .map(UsernamePasswordAuthenticationToken::getPrincipal)
                .cast(User::class.java)
                    .map(User::getUsername);
    }

}