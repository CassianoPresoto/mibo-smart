package intelbras.mobi.smart.rest

data class RestConfiguration(
    val baseUrl: String = DEFAULT_BASE_URL,
    val logRequests: Boolean = false,
    val requestTimeoutInMillis: Long = DEFAULT_TIMEOUT_IN_MILLIS,
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://open-casainteligente.intelbras.com.br"
        const val DEFAULT_TIMEOUT_IN_MILLIS = 30_000L
    }
}
