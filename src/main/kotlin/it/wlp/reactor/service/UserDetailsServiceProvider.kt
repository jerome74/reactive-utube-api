package it.wlp.reactor.service

import it.wlp.reactor.config.ConfigProperties
import it.wlp.reactor.exception.ProcessingException
import it.wlp.reactor.repository.UsersRepository
import it.wlp.reactor.util.UtilCrypt
import org.apache.commons.lang3.ObjectUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserDetailsServiceProvider : ReactiveUserDetailsService {

    @Autowired
    lateinit var usersRepository: UsersRepository

    @Autowired
    lateinit var configProperties: ConfigProperties


    override fun findByUsername(username: String?): Mono<UserDetails> {

        return usersRepository.findByUsername(username!!)
            .filter{
                it.active == 1 && !ObjectUtils.notEqual(it.enddate, null)}
            .map {
                var builder  = User.withUsername(it.username);
                builder!!.password(UtilCrypt.crypto.encode(it.password));
                builder!!.authorities(configProperties.getGrantedAuthority(username!!))

                return@map builder!!.build()
            }
            .doOnError { Mono.error<ProcessingException> { ProcessingException("on loadUserByUsername", it) } }
    }

}