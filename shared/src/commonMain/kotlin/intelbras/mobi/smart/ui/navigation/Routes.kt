package intelbras.mobi.smart.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SessionCheckRoute

@Serializable
data class TokenEntryRoute(val sessionExpired: Boolean = false)

@Serializable
data object DeviceListRoute

@Serializable
data class LiveVideoRoute(
    val serialNumber: String,
    val productId: String,
    val name: String,
    val model: String,
)
