package it.wlp.reactor.util


import it.wlp.reactor.exception.InputException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono

object UtilCrypt {
    val crypto = BCryptPasswordEncoder(12)
}

object Constants {
    val MP3 = "mp3"
    val MP4 = "mp4"
}