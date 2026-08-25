package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.rest.SmartHomeDeviceOfflineException
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
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

    private fun Throwable.toStatusResult(): LockStatusResult = when {
        rejectsTheAccessToken() -> LockStatusResult.InvalidToken
        this is SmartHomeDeviceOfflineException -> LockStatusResult.DeviceOffline
        this is SmartHomeNotFoundException -> LockStatusResult.DeviceOffline
        this is SmartHomeNetworkException -> LockStatusResult.NetworkUnavailable
        else -> LockStatusResult.Error(this)
    }
}
