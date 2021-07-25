package it.wlp.reactor.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import it.wlp.reactor.dto.ProfilesDTO
import it.wlp.reactor.dto.UsersDTO
import it.wlp.reactor.entity.Profiles
import it.wlp.reactor.entity.Users
import it.wlp.reactor.exception.ProcessingException
import it.wlp.reactor.exception.SimpleProcessException
import it.wlp.reactor.repository.ProfilesRepository
import it.wlp.reactor.repository.UsersRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.sql.Timestamp

@Configuration
class ConfigMongo {

    @Value("classpath:json/users.json")
    lateinit var usersResourceData: Resource

    @Value("classpath:json/profiles.json")
    lateinit var profilesResourceData: Resource


    @Autowired
    lateinit var usersRepository: UsersRepository

    @Autowired
    lateinit var profilesRepository: ProfilesRepository

    val log = LoggerFactory.getLogger(ConfigMongo::class.java.getName());

    @Bean
    fun init() =
        CommandLineRunner {
            val listUser = (ObjectMapper()).readValue(
                usersResourceData.getInputStream(),
                object : TypeReference<List<UsersDTO>>() {})
            val listProfile = (ObjectMapper()).readValue(
                profilesResourceData.getInputStream(),
                object : TypeReference<List<ProfilesDTO>>() {})


            val users = usersRepository.saveAll(
                Flux.fromIterable(listUser)
                    .map {
                        Users(
                            it.username,
                            it.email,
                            it.password,
                            it.active.toInt(),
                            Timestamp.valueOf(it.startdate),
                            null
                        )
                    }
                    .onErrorResume { Mono.error { SimpleProcessException("on saveAll Users") }
                    })



                val profiles = profilesRepository.saveAll(
                        Flux.fromIterable(listProfile)
                        .map {
                            Profiles(
                                it.nickname,
                                it.email,
                                it.avatarname,
                                it.avatarcolor,
                                it.active.toInt(),
                                Timestamp.valueOf(it.startdate),
                                null
                            )
                        }
                            .onErrorResume { Mono.error { SimpleProcessException("on saveAll Profiles") }
                        })

            Flux.concat(users,profiles).subscribe {
              println("##### $it")
            }
        }

}