package intelbras.mobi.smart.business.lock.usecase

data class LockDetails(
    val batteryPercentage: Int? = null,
    val signalStrength: Int? = null,
    val remoteOpeningEnabled: Boolean? = null,
)
