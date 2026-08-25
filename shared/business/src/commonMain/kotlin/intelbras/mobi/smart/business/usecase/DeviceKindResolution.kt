package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceSerial
import intelbras.mobi.smart.domain.lock.LockRepository
import kotlin.coroutines.cancellation.CancellationException

private const val LIVE_VIDEO_CAPABILITY = "RTSV"

internal class DeviceKindResolution(
    private val deviceRepository: DeviceRepository,
    private val lockRepository: LockRepository,
) {

    suspend operator fun invoke(device: Device): DeviceKind {
        val announced = announcedKind(device.serial())
        if (announced != DeviceKind.Unknown) return announced

        return if (device.isSubdevice && respondsAsALock(device)) DeviceKind.Lock else DeviceKind.Unknown
    }

    suspend fun announcedKind(serial: DeviceSerial): DeviceKind =
        deviceRepository.readCapabilities(serial).toKind()

    private suspend fun respondsAsALock(device: Device): Boolean = try {
        lockRepository.readOpeningStatus(device.reference())
        true
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (notALock: Throwable) {
        false
    }

    private fun DeviceCapabilities.toKind(): DeviceKind = when {
        announces(LIVE_VIDEO_CAPABILITY) -> DeviceKind.Camera
        else -> DeviceKind.Unknown
    }

    private fun DeviceCapabilities.announces(capability: String): Boolean =
        values.any { it.startsWith(capability, ignoreCase = true) }
}
