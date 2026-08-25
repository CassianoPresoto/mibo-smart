package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockControlRequest
import intelbras.mobi.smart.rest.SmartHomeDeviceOfflineException
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeOperationRejectedException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay

private const val FIRST_ATTEMPT = 1

internal class LockSwitching(
    private val lockRepository: LockRepository,
    private val confirmationPolicy: LockConfirmationPolicy,
) {

    suspend operator fun invoke(lock: DeviceReference, open: Boolean): LockOperationResult = try {
        lockRepository.control(lock.toControlRequest(open))
        lock.confirm(open)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toOperationResult()
    }

    private suspend fun DeviceReference.confirm(expected: Boolean): LockOperationResult {
        var lastReading: Boolean? = null

        for (attempt in FIRST_ATTEMPT..confirmationPolicy.attempts) {
            delay(confirmationPolicy.waitBefore(attempt))

            val reading = readOpeningOrNull()
            if (reading == expected) return LockOperationResult.Done(reading, confirmed = true)
            if (reading != null) lastReading = reading
        }

        return LockOperationResult.Done(
            isOpen = lastReading ?: expected,
            confirmed = false,
        )
    }

    private suspend fun DeviceReference.readOpeningOrNull(): Boolean? = try {
        lockRepository.readOpeningStatus(this).isOpen
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (unreadableLock: Throwable) {
        null
    }

    private fun DeviceReference.toControlRequest(open: Boolean) = LockControlRequest(
        serialNumber = serialNumber,
        productId = productId,
        open = open,
    )

    private fun Throwable.toOperationResult(): LockOperationResult = when {
        rejectsTheAccessToken() -> LockOperationResult.InvalidToken
        this is SmartHomeOperationRejectedException -> LockOperationResult.Refused
        this is SmartHomeDeviceOfflineException -> LockOperationResult.DeviceOffline
        this is SmartHomeNotFoundException -> LockOperationResult.DeviceOffline
        this is SmartHomeNetworkException -> LockOperationResult.NetworkUnavailable
        else -> LockOperationResult.Error(this)
    }
}
