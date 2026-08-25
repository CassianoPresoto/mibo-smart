package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val CAPABILITY_SEPARATOR = ','

@Serializable
data class DeviceCapabilities(
    @SerialName("funcoes") val rawCapabilities: String = "",
) {
    val values: List<String>
        get() = rawCapabilities.split(CAPABILITY_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun supports(capability: String): Boolean =
        values.any { it.equals(capability, ignoreCase = true) }
}
