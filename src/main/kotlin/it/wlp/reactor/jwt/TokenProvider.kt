package it.wlp.reactor.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import it.wlp.reactor.util.AuthObj
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

@Component
class TokenProvider {


    val log = LoggerFactory.getLogger(TokenProvider::class.java);


    fun createToken(authentication : Authentication) : String {

        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, AuthObj.configSecret!!.expiretime.toInt())
        val to30min = cal.time


        return Jwts.builder()
            .setSubject(authentication!!.name)
            .claim("authorities", authentication!!.authorities)
            .setIssuedAt(Date())
            .setExpiration(to30min)
            .signWith(Keys.hmacShaKeyFor(AuthObj.configSecret!!.getAuthSecret()))
            .compact()


    }

    fun getAuthentication(token : String) : Authentication {
        if (StringUtils.isEmpty(token) || !validateToken(token)) {
            throw BadCredentialsException("Invalid token");
        }
        val claimJwts = Jwts.parser()
            .setSigningKey(AuthObj!!.configSecret!!.getAuthSecret())
            .parseClaimsJws(token)

        val username = claimJwts.body.subject
        val authorities = claimJwts.body.get("authorities") as List<Map<String, String>>

        val simpleGrantedAuthority = authorities.stream().map { SimpleGrantedAuthority(it.get("authority")) }.collect(
            Collectors.toSet())
        val authenticationAuth = UsernamePasswordAuthenticationToken(username,token,simpleGrantedAuthority)


        return authenticationAuth
    }

    fun validateToken(authToken : String) : Boolean {
        try {
            Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(AuthObj!!.configSecret!!.getAuthSecret()))
                .parseClaimsJws(authToken)
            return true;
        } catch ( e : SignatureException) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
        } catch (e : MalformedJwtException) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace: {}", e);
        } catch (e : ExpiredJwtException) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
        } catch (e : UnsupportedJwtException) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
        } catch (e : IllegalArgumentException) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        }
        return false;
    }

}