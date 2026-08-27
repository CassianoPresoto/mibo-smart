package intelbras.mobi.smart.business.device

import intelbras.mobi.smart.business.device.usecase.DeviceConnection
import intelbras.mobi.smart.business.device.usecase.DeviceConnectionResult
import intelbras.mobi.smart.business.device.usecase.DisconnectionResult
import intelbras.mobi.smart.domain.device.model.DeviceReference

interface DeviceConnector {
    suspend fun connect(device: DeviceReference): DeviceConnectionResult

    suspend fun disconnect(connection: DeviceConnection): DisconnectionResult
}
