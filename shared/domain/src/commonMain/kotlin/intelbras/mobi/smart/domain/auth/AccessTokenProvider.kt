package intelbras.mobi.smart.domain.auth

interface AccessTokenProvider {
    suspend fun currentAccessToken(): String?
}
