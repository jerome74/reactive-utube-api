package it.wlp.reactor.handler

import it.wlp.reactor.dto.ResultDTO
import it.wlp.reactor.entity.Users
import it.wlp.reactor.exception.InputException
import it.wlp.reactor.exception.ProcessingException
import it.wlp.reactor.exception.SimpleProcessException
import it.wlp.reactor.jwt.JWTReactiveAuthenticationManager
import it.wlp.reactor.jwt.TokenProvider
import it.wlp.reactor.model.CredentialModel
import it.wlp.reactor.model.FindModel
import it.wlp.reactor.model.SearchResult
import it.wlp.reactor.model.VideoInfoModel
import it.wlp.reactor.repository.UsersRepository
import it.wlp.reactor.service.UTubeDService
import it.wlp.reactor.util.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class ApiHandler {


    @Autowired
    lateinit var uTubeDService: UTubeDService

    @Autowired
    lateinit var usersRepository: UsersRepository

    @Autowired
    lateinit var repositoryReactiveAuthenticationManager: JWTReactiveAuthenticationManager

    @Autowired
    lateinit var tokenProvider: TokenProvider


    fun listUsers(request: ServerRequest): Mono<ServerResponse> {

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(usersRepository.findAll(), Users::class.java)
    }

    @Throws(ProcessingException::class)
    fun doFind(request: ServerRequest): Mono<ServerResponse> {

        return request.bodyToMono(FindModel::class.java)
            .switchIfEmpty(Mono.defer(this::raiseInputException))
            .map { uTubeDService.findVideo(it.research) }
            .onErrorResume { Mono.error { SimpleProcessException("error on elaboration") } }
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(it, SearchResult::class.java)
            }
    }

    @Throws(ProcessingException::class)
    fun doDownload(request: ServerRequest): Mono<ServerResponse> {

        return request.bodyToMono(VideoInfoModel::class.java)
            .switchIfEmpty(Mono.defer(this::raiseInputException)).map {

            if(it.type == Constants.MP3)
                  return@map uTubeDService.downloadMp3(it)
                else
                    return@map uTubeDService.downloadMp4(it)
            }
            .onErrorResume { Mono.error { SimpleProcessException("error on elaboration") } }
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(it, InputStreamResource::class.java)
            }

    }

    @Throws(BadCredentialsException::class)
    fun doLogin(request: ServerRequest): Mono<ServerResponse> {

        return request.bodyToMono(CredentialModel::class.java)
            .map { UsernamePasswordAuthenticationToken(it.username, it.password) }
            .flatMap { repositoryReactiveAuthenticationManager.authenticate(it); }
            .doOnError { BadCredentialsException("Bad crendentials") }
            .flatMap {
                ReactiveSecurityContextHolder.withAuthentication(it);
                ServerResponse.status(HttpStatus.OK).bodyValue(ResultDTO(true, tokenProvider.createToken(it)));
            }
    }


    fun <T> raiseInputException(): Mono<T> {
        return Mono.error(InputException("error on input value"));
    }
}