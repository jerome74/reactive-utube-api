package it.wlp.reactor.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp
import java.time.Instant


data class ResultDTO(
                    var ok : Boolean
                    ,var message : String )


data class ProfilesDTO(@JsonProperty("nickname") var nickname : String = ""
                       , @JsonProperty("email") var email : String = ""
                       , @JsonProperty("avatarname") var avatarname : String = ""
                       , @JsonProperty("avatarcolor") var avatarcolor : String = ""
                       , @JsonProperty("active") var active : String = ""
                       , @JsonProperty("startdate") var startdate : String = ""
                       , @JsonProperty("enddate") var enddate : String?)


data class UsersDTO(@JsonProperty("username") var username : String = ""
                       , @JsonProperty("email") var email : String = ""
                       , @JsonProperty("password") var password : String = ""
                       , @JsonProperty("active") var active : String = ""
                       , @JsonProperty("startdate") var startdate : String = ""
                       , @JsonProperty("enddate") var enddate : String?)

