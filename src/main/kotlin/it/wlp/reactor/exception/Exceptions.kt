package it.wlp.reactor.exception

data class ProcessingException (val step : String, val throwable : Throwable) : Exception()
data class InputException(override val message: String) : Exception()
data class SimpleProcessException(override val message: String) : Exception()
