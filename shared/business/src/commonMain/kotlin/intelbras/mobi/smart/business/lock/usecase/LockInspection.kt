package intelbras.mobi.smart.business.lock.usecase

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import kotlin.coroutines.cancellation.CancellationException

internal class LockInspection(
    private val lockRepository: LockRepository,
) {

    suspend operator fun invoke(lock: DeviceReference): LockStatusResult = try {
        LockStatusResult.Known(lockRepository.readOpeningStatus(lock).isOpen)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toStatusResult()
    }

    private fun Throwable.toStatusResult(): LockStatusResult = when (asLockFailureKind()) {
        LockFailureKind.DeviceOffline -> LockStatusResult.DeviceOffline
        LockFailureKind.InvalidToken -> LockStatusResult.InvalidToken
        LockFailureKind.NetworkUnavailable -> LockStatusResult.NetworkUnavailable
        LockFailureKind.Refused,
        LockFailureKind.PlatformFailure,
        LockFailureKind.Unexpected,
        -> LockStatusResult.Error(this)
    }
}
