package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.light.model.LightBrightnessRequest
import intelbras.mobi.smart.domain.light.model.LightColorRequest
import intelbras.mobi.smart.domain.light.model.LightContrastRequest
import intelbras.mobi.smart.domain.light.model.LightMode
import intelbras.mobi.smart.domain.light.model.LightModeRequest
import intelbras.mobi.smart.domain.light.model.LightPowerRequest
import intelbras.mobi.smart.domain.light.model.LightTemperatureRequest
import intelbras.mobi.smart.domain.light.model.LightTimerRequest
import intelbras.mobi.smart.rest.PRODUCT_ID
import intelbras.mobi.smart.rest.SERIAL_NUMBER
import intelbras.mobi.smart.rest.bodyText
import intelbras.mobi.smart.rest.respondAcknowledgement
import intelbras.mobi.smart.rest.testApiCaller
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class LightRestRepositoryTest {

    private val reference = DeviceReference(serialNumber = SERIAL_NUMBER, productId = PRODUCT_ID)
    private val captured = mutableListOf<HttpRequestData>()
    private val repository = LightRestRepository(
        testApiCaller(captured) { respondAcknowledgement() }
    )

    @Test
    fun `switch posts the requested power state`() = runTest {
        repository.switch(
            LightPowerRequest(serialNumber = SERIAL_NUMBER, productId = PRODUCT_ID, on = true)
        )

        assertRequest(
            route = "/lampadas/ligada/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID","ligada":true}""",
        )
    }

    @Test
    fun `startTimer posts the delay in seconds and what to do afterwards`() = runTest {
        repository.startTimer(
            LightTimerRequest(
                serialNumber = SERIAL_NUMBER,
                productId = PRODUCT_ID,
                seconds = 600,
                turnOn = true,
            )
        )

        assertRequest(
            route = "/lampadas/iniciar-temporizador/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID","tempo":600,"ligar":true}""",
        )
    }

    @Test
    fun `stopTimer posts the device reference`() = runTest {
        repository.stopTimer(reference)

        assertRequest(
            route = "/lampadas/parar-temporizador/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID"}""",
        )
    }

    @Test
    fun `changeBrightness posts the brightness`() = runTest {
        repository.changeBrightness(
            LightBrightnessRequest(serialNumber = SERIAL_NUMBER, productId = PRODUCT_ID, brightness = 75)
        )

        assertRequest(
            route = "/lampadas/mudar-brilho/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID","brilho":75}""",
        )
    }

    @Test
    fun `changeContrast posts the contrast`() = runTest {
        repository.changeContrast(
            LightContrastRequest(serialNumber = SERIAL_NUMBER, productId = PRODUCT_ID, contrast = 40)
        )

        assertRequest(
            route = "/lampadas/mudar-contraste/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID","contraste":40}""",
        )
    }

    @Test
    fun `changeColor posts the hue`() = runTest {
        repository.changeColor(
            LightColorRequest(serialNumber = SERIAL_NUMBER, productId = PRODUCT_ID, hue = 180)
        )

        assertRequest(
            route = "/lampadas/mudar-cor/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID","cor":180}""",
        )
    }

    @Test
    fun `changeMode posts the mode as the integer the platform expects`() = runTest {
        repository.changeMode(
            LightModeRequest(serialNumber = SERIAL_NUMBER, productId = PRODUCT_ID, mode = LightMode.Color)
        )

        assertRequest(
            route = "/lampadas/mudar-modo/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID","modo":1}""",
        )
    }

    @Test
    fun `changeTemperature posts the temperature in kelvin`() = runTest {
        repository.changeTemperature(
            LightTemperatureRequest(
                serialNumber = SERIAL_NUMBER,
                productId = PRODUCT_ID,
                temperature = 4000,
            )
        )

        assertRequest(
            route = "/lampadas/mudar-temperatura/v1",
            body = """{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID","temperatura":4000}""",
        )
    }

    private fun assertRequest(route: String, body: String) {
        val recorded = captured.single()
        assertEquals(HttpMethod.Post, recorded.method)
        assertEquals(route, recorded.url.encodedPath)
        assertEquals(body, recorded.bodyText())
    }
}
