package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.DeviceConnection
import intelbras.mobi.smart.business.usecase.DeviceConnectionResult
import intelbras.mobi.smart.business.usecase.DisconnectionResult
import intelbras.mobi.smart.domain.device.model.DeviceReference

interface DeviceConnector {
    suspend fun connect(device: DeviceReference): DeviceConnectionResult

    suspend fun disconnect(connection: DeviceConnection): DisconnectionResult
}
