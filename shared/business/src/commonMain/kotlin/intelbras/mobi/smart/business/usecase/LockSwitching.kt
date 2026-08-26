package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockControlRequest
import kotlin.coroutines.cancellation.CancellationException

internal class LockSwitching(
    private val lockRepository: LockRepository,
    private val confirmation: LockConfirmation,
) {

    suspend operator fun invoke(lock: DeviceReference, open: Boolean): LockOperationResult = try {
        lockRepository.control(lock.toControlRequest(open))
        confirmation.await(expected = open) { lockRepository.readOpeningStatus(lock).isOpen }
            .toOperationResult()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toOperationResult()
    }

    private fun LockConfirmation.Reading<Boolean>.toOperationResult() =
        LockOperationResult.Done(isOpen = value, confirmed = confirmed)

    private fun DeviceReference.toControlRequest(open: Boolean) = LockControlRequest(
        serialNumber = serialNumber,
        productId = productId,
        open = open,
    )

    private fun Throwable.toOperationResult(): LockOperationResult = when (asLockFailureKind()) {
        LockFailureKind.Refused -> LockOperationResult.Refused
        LockFailureKind.DeviceOffline -> LockOperationResult.DeviceOffline
        LockFailureKind.InvalidToken -> LockOperationResult.InvalidToken
        LockFailureKind.NetworkUnavailable -> LockOperationResult.NetworkUnavailable
        LockFailureKind.PlatformFailure,
        LockFailureKind.Unexpected,
        -> LockOperationResult.Error(this)
    }
}
