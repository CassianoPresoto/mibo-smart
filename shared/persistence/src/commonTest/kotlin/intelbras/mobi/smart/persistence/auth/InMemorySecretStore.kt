package intelbras.mobi.smart.persistence.auth

internal class InMemorySecretStore(private var secret: String? = null) : AccessTokenSecretStore {

    override fun read(): String? = secret

    override fun write(token: String) {
        secret = token
    }

    override fun clear() {
        secret = null
    }
}
