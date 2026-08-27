package intelbras.mobi.smart.business.lock.usecase

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import kotlin.coroutines.cancellation.CancellationException

internal class LockVolumeReading(
    private val lockRepository: LockRepository,
    private val volumeMemory: LockVolumeMemory,
) {

    suspend operator fun invoke(lock: DeviceReference): LockVolumeResult = try {
        LockVolumeResult.Known(lockRepository.readVolume(lock).volume)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toVolumeResult(lock)
    }

    private suspend fun Throwable.toVolumeResult(lock: DeviceReference): LockVolumeResult =
        when (asLockFailureKind()) {
            LockFailureKind.PlatformFailure ->
                LockVolumeResult.Remembered(volumeMemory.lastLevelOf(lock))

            LockFailureKind.DeviceOffline -> LockVolumeResult.DeviceOffline
            LockFailureKind.InvalidToken -> LockVolumeResult.InvalidToken
            LockFailureKind.NetworkUnavailable -> LockVolumeResult.NetworkUnavailable
            LockFailureKind.Refused, LockFailureKind.Unexpected -> LockVolumeResult.Error(this)
        }
}
