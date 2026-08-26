package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.sensor.SensorRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class LockDetailsReading(
    private val deviceRepository: DeviceRepository,
    private val sensorRepository: SensorRepository,
    private val lockRepository: LockRepository,
) {

    suspend operator fun invoke(lock: DeviceReference): LockDetails = coroutineScope {
        val battery = async { readOrNull { deviceRepository.readBatteryLevel(lock).percentage } }
        val signal = async { readOrNull { sensorRepository.readZigbeeSignal(lock).strength } }
        val remoteOpening = async {
            readOrNull { lockRepository.readRemoteOpeningStatus(lock).isEnabled }
        }

        LockDetails(
            batteryPercentage = battery.await(),
            signalStrength = signal.await(),
            remoteOpeningEnabled = remoteOpening.await(),
        )
    }

    private suspend fun <T : Any> readOrNull(read: suspend () -> T): T? = try {
        read()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Throwable) {
        null
    }
}
