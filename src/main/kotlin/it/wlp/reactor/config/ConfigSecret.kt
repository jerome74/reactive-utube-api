package it.wlp.reactor.config

import it.wlp.reactor.util.AuthObj
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

@Component
class ConfigSecret{


    @Value("\${authentication.expiretime}")
    lateinit var expiretime : String

    @Value("\${authentication.authsecretid}")
    lateinit var authsecretid : String

    @Value("\${authentication.key}")
    lateinit var key : String

    fun getAuthSecret() : ByteArray {
        if(AuthObj.sha256 == null)
            return MessageDigest.getInstance("SHA-256").digest(authsecretid.toByteArray(Charsets.UTF_8))

        return AuthObj.sha256!!
    }

    fun getSecret() : String{
        return Base64.getEncoder().encodeToString(authsecretid.toByteArray(Charsets.UTF_8));
    }
}