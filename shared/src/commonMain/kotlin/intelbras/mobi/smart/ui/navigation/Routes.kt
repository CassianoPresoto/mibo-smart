package intelbras.mobi.smart.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SessionCheckRoute

@Serializable
data class TokenEntryRoute(val sessionExpired: Boolean = false)

@Serializable
data object DeviceListRoute

@Serializable
data object ActivityRoute

@Serializable
data object AccountRoute

@Serializable
data class LiveVideoRoute(
    val address: String,
    val productId: String,
    val name: String,
    val model: String,
)

@Serializable
data class LockRoute(
    val address: String,
    val productId: String,
    val name: String,
    val model: String,
)

@Serializable
data class LockHistoryRoute(
    val address: String,
    val productId: String,
    val name: String,
)
