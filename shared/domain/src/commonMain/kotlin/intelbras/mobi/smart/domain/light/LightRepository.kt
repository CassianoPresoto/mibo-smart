package intelbras.mobi.smart.domain.light

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.light.model.LightBrightnessRequest
import intelbras.mobi.smart.domain.light.model.LightColorRequest
import intelbras.mobi.smart.domain.light.model.LightContrastRequest
import intelbras.mobi.smart.domain.light.model.LightModeRequest
import intelbras.mobi.smart.domain.light.model.LightPowerRequest
import intelbras.mobi.smart.domain.light.model.LightTemperatureRequest
import intelbras.mobi.smart.domain.light.model.LightTimerRequest

interface LightRepository {
    suspend fun switch(request: LightPowerRequest)

    suspend fun startTimer(request: LightTimerRequest)

    suspend fun stopTimer(reference: DeviceReference)

    suspend fun changeBrightness(request: LightBrightnessRequest)

    suspend fun changeContrast(request: LightContrastRequest)

    suspend fun changeColor(request: LightColorRequest)

    suspend fun changeMode(request: LightModeRequest)

    suspend fun changeTemperature(request: LightTemperatureRequest)
}
