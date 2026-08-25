package intelbras.mobi.smart.domain.lock.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LockControlRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("aberto") val open: Boolean,
)

@Serializable
data class LockVolumeRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("volume") val volume: LockVolumeLevel,
)

@Serializable
data class LockHistoryRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("quantidade") val limit: Int = DEFAULT_HISTORY_SIZE,
) {
    companion object {
        const val DEFAULT_HISTORY_SIZE = 50
    }
}

@Serializable
data class RemoteOpeningRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("habilitar") val enabled: Boolean,
)

@Serializable
data class SinglePasswordRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("senha") val password: String,
)

@Serializable
data class DynamicPasswordRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("senha") val password: String,
)

@Serializable
data class PeriodicPasswordRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("senha") val password: String,
    @SerialName("comeco") val startsAt: String,
    @SerialName("limite") val endsAt: String,
)

@Serializable
data class PasswordDeletionRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("idSenha") val passwordId: Int,
)
