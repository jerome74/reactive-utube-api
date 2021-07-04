package it.wlp.reactor.model

data class UserModel(
    val username: String, val email: String, val password: String
)

data class UserprofileModel(
    val nickname: String, val email: String, val avatarname: String, val avatarcolor: String
)

data class CredentialModel(
    val username: String, val password: String
)

data class FindModel(val research: String)

class SearchResult(
    var id: String = "",
    var etag: String = "",
    var kind: String = "",
    var channelId: String = "",
    var channelTitle: String = "",
    var description: String = "",
    var title: String = "",
    var thumbnails: String = "",
    var length: String = ""
)
