package intelbras.mobi.smart.business.device.usecase

sealed interface DisconnectionResult {
    data object Released : DisconnectionResult

    data class Failed(val cause: Throwable) : DisconnectionResult
}
