package it.wlp.reactor.util

import it.wlp.reactor.config.ConfigSecret

object AuthObj {
    var sha256 : ByteArray? = null
    var configSecret: ConfigSecret? = null
}