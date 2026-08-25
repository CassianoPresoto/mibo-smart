package intelbras.mobi.smart.domain.lock.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LockOpeningStatus(
    @SerialName("aberto") val isOpen: Boolean = false,
)

@Serializable
data class LockVolumeStatus(
    @SerialName("volume") val volume: LockVolumeLevel = LockVolumeLevel.Mute,
)

@Serializable
data class RemoteOpeningStatus(
    @SerialName("habilitado") val isEnabled: Boolean = false,
)

@Serializable
data class LockOpeningRecord(
    @SerialName("data") val timestamp: String = "",
    @SerialName("tipo") val type: String = "",
    @SerialName("usuario") val user: String = "",
    @SerialName("idUsuario") val userId: Int? = null,
)
