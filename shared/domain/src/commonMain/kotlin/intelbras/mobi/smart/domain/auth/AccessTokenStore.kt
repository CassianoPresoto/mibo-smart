package intelbras.mobi.smart.domain.auth

import intelbras.mobi.smart.domain.auth.model.AccessToken

interface AccessTokenStore {
    suspend fun read(): AccessToken?

    suspend fun save(accessToken: AccessToken)

    suspend fun clear()
}
