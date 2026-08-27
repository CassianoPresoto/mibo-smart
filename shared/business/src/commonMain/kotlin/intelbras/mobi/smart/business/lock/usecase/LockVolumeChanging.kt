package intelbras.mobi.smart.business.lock.usecase

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import intelbras.mobi.smart.domain.lock.model.LockVolumeRequest
import kotlin.coroutines.cancellation.CancellationException

internal class LockVolumeChanging(
    private val lockRepository: LockRepository,
    private val confirmation: LockConfirmation,
    private val volumeMemory: LockVolumeMemory,
) {

    suspend operator fun invoke(
        lock: DeviceReference,
        level: LockVolumeLevel,
    ): LockVolumeChangeResult = try {
        lockRepository.changeVolume(lock.toVolumeRequest(level))
        volumeMemory.remember(lock, level)
        confirmation.await(expected = level) { lockRepository.readVolume(lock).volume }
            .toChangeResult()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toChangeResult()
    }

    private fun LockConfirmation.Reading<LockVolumeLevel>.toChangeResult() =
        LockVolumeChangeResult.Done(level = value, confirmed = confirmed)

    private fun DeviceReference.toVolumeRequest(level: LockVolumeLevel) = LockVolumeRequest(
        serialNumber = serialNumber,
        productId = productId,
        volume = level,
    )

    private fun Throwable.toChangeResult(): LockVolumeChangeResult = when (asLockFailureKind()) {
        LockFailureKind.Refused -> LockVolumeChangeResult.Refused
        LockFailureKind.DeviceOffline -> LockVolumeChangeResult.DeviceOffline
        LockFailureKind.InvalidToken -> LockVolumeChangeResult.InvalidToken
        LockFailureKind.NetworkUnavailable -> LockVolumeChangeResult.NetworkUnavailable
        LockFailureKind.PlatformFailure,
        LockFailureKind.Unexpected,
        -> LockVolumeChangeResult.Error(this)
    }
}
