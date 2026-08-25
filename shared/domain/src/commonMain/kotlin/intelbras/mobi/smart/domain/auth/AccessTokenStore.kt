package intelbras.mobi.smart.domain.auth

interface AccessTokenStore : AccessTokenProvider {
    suspend fun save(accessToken: String)

    suspend fun clear()
}
