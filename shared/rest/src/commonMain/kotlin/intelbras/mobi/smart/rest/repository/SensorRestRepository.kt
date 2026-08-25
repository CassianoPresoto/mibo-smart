package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.sensor.SensorRepository
import intelbras.mobi.smart.domain.sensor.model.HumidityAndTemperature
import intelbras.mobi.smart.domain.sensor.model.OpeningSensorStatus
import intelbras.mobi.smart.domain.sensor.model.SensorArmedStatus
import intelbras.mobi.smart.domain.sensor.model.ZigbeeSignalStrength
import intelbras.mobi.smart.rest.client.ApiRoutes
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller

internal class SensorRestRepository(
    private val caller: SmartHomeApiCaller,
) : SensorRepository {

    override suspend fun readArmedStatus(reference: DeviceReference): SensorArmedStatus =
        caller.query(
            route = ApiRoutes.SENSOR_ARMED,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = SensorArmedStatus.serializer(),
        )

    override suspend fun readOpeningSensor(reference: DeviceReference): OpeningSensorStatus =
        caller.query(
            route = ApiRoutes.SENSOR_OPENING,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = OpeningSensorStatus.serializer(),
        )

    override suspend fun readZigbeeSignal(reference: DeviceReference): ZigbeeSignalStrength =
        caller.query(
            route = ApiRoutes.SENSOR_ZIGBEE_SIGNAL,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = ZigbeeSignalStrength.serializer(),
        )

    override suspend fun readHumidityAndTemperature(
        reference: DeviceReference,
    ): HumidityAndTemperature =
        caller.query(
            route = ApiRoutes.SENSOR_HUMIDITY_TEMPERATURE,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = HumidityAndTemperature.serializer(),
        )
}
