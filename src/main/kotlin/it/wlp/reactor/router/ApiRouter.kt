package it.wlp.reactor

import it.wlp.reactor.handler.ApiHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router


@Configuration
class ApiRouter {


    @Bean
    fun routerUsersFunction(handler: ApiHandler): RouterFunction<ServerResponse> = router {
        ("/reactive/users").invoke {req -> handler.listUsers(req)}
        }

    @Bean
    fun routerLoginFunction(handler: ApiHandler): RouterFunction<ServerResponse> = router {
        ("/reactive/login").invoke {req -> handler.doLogin(req)}
    }

    @Bean
    fun routerFindFunction(handler: ApiHandler): RouterFunction<ServerResponse> = router {
        ("/reactive/find").invoke {req -> handler.doFind(req)}
    }

    @Bean
    fun routerDownloadFunction(handler: ApiHandler): RouterFunction<ServerResponse> = router {
        ("/reactive/download").invoke {req -> handler.doDownload(req)}
    }

}