package intelbras.mobi.smart.persistence.auth

interface AccessTokenSecretStore {
    fun read(): String?

    fun write(token: String)

    fun clear()
}
