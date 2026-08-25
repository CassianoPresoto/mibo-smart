package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.light.LightRepository
import intelbras.mobi.smart.domain.light.model.LightBrightnessRequest
import intelbras.mobi.smart.domain.light.model.LightColorRequest
import intelbras.mobi.smart.domain.light.model.LightContrastRequest
import intelbras.mobi.smart.domain.light.model.LightModeRequest
import intelbras.mobi.smart.domain.light.model.LightPowerRequest
import intelbras.mobi.smart.domain.light.model.LightTemperatureRequest
import intelbras.mobi.smart.domain.light.model.LightTimerRequest
import intelbras.mobi.smart.rest.client.ApiRoutes
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller

internal class LightRestRepository(
    private val caller: SmartHomeApiCaller,
) : LightRepository {

    override suspend fun switch(request: LightPowerRequest) =
        caller.command(
            route = ApiRoutes.LIGHT_POWER,
            body = request,
            bodySerializer = LightPowerRequest.serializer(),
        )

    override suspend fun startTimer(request: LightTimerRequest) =
        caller.command(
            route = ApiRoutes.LIGHT_START_TIMER,
            body = request,
            bodySerializer = LightTimerRequest.serializer(),
        )

    override suspend fun stopTimer(reference: DeviceReference) =
        caller.command(
            route = ApiRoutes.LIGHT_STOP_TIMER,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
        )

    override suspend fun changeBrightness(request: LightBrightnessRequest) =
        caller.command(
            route = ApiRoutes.LIGHT_BRIGHTNESS,
            body = request,
            bodySerializer = LightBrightnessRequest.serializer(),
        )

    override suspend fun changeContrast(request: LightContrastRequest) =
        caller.command(
            route = ApiRoutes.LIGHT_CONTRAST,
            body = request,
            bodySerializer = LightContrastRequest.serializer(),
        )

    override suspend fun changeColor(request: LightColorRequest) =
        caller.command(
            route = ApiRoutes.LIGHT_COLOR,
            body = request,
            bodySerializer = LightColorRequest.serializer(),
        )

    override suspend fun changeMode(request: LightModeRequest) =
        caller.command(
            route = ApiRoutes.LIGHT_MODE,
            body = request,
            bodySerializer = LightModeRequest.serializer(),
        )

    override suspend fun changeTemperature(request: LightTemperatureRequest) =
        caller.command(
            route = ApiRoutes.LIGHT_TEMPERATURE,
            body = request,
            bodySerializer = LightTemperatureRequest.serializer(),
        )
}
