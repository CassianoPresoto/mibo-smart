package intelbras.mobi.smart.domain.sensor

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.sensor.model.HumidityAndTemperature
import intelbras.mobi.smart.domain.sensor.model.OpeningSensorStatus
import intelbras.mobi.smart.domain.sensor.model.SensorArmedStatus
import intelbras.mobi.smart.domain.sensor.model.ZigbeeSignalStrength

interface SensorRepository {
    suspend fun readArmedStatus(reference: DeviceReference): SensorArmedStatus

    suspend fun readOpeningSensor(reference: DeviceReference): OpeningSensorStatus

    suspend fun readZigbeeSignal(reference: DeviceReference): ZigbeeSignalStrength

    suspend fun readHumidityAndTemperature(reference: DeviceReference): HumidityAndTemperature
}
