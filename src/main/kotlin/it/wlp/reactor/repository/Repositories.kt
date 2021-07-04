package it.wlp.reactor.repository

import it.wlp.reactor.entity.Profiles
import it.wlp.reactor.entity.Users
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProfilesRepository : ReactiveMongoRepository<Profiles, Int> {
    fun findByEmail(email: String): Mono<Profiles>
}

@Repository
interface UsersRepository : ReactiveMongoRepository<Users, Int> {

    fun findByUsername(username: String): Mono<Users>
}