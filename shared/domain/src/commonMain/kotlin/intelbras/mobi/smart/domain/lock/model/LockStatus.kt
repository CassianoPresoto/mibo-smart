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
    @SerialName("tempoLocal") val localTime: String = "",
    @SerialName("nome") val user: String = "",
    @SerialName("tipo") val way: String = "",
)
