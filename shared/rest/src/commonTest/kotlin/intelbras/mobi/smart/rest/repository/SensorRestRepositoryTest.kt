package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.rest.PRODUCT_ID
import intelbras.mobi.smart.rest.SUBDEVICE_SERIAL_NUMBER
import intelbras.mobi.smart.rest.bodyText
import intelbras.mobi.smart.rest.respondEnvelope
import intelbras.mobi.smart.rest.testApiCaller
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SensorRestRepositoryTest {

    private val reference = DeviceReference(
        serialNumber = SUBDEVICE_SERIAL_NUMBER,
        productId = PRODUCT_ID,
    )
    private val referenceBody = """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID"}"""

    @Test
    fun `readArmedStatus posts the sensor reference and returns whether it is armed`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = SensorRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"armado":true}""") }
        )

        val status = repository.readArmedStatus(reference)

        assertRequest(captured, "/sensores/armado/v1")
        assertTrue(status.isArmed)
    }

    @Test
    fun `readOpeningSensor posts the sensor reference and returns whether it is open`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = SensorRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"aberto":false}""") }
        )

        val status = repository.readOpeningSensor(reference)

        assertRequest(captured, "/sensores/sensor-de-abertura/v1")
        assertFalse(status.isOpen)
    }

    @Test
    fun `readZigbeeSignal posts the sensor reference and returns the signal strength`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = SensorRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"sinal":-70}""") }
        )

        val signal = repository.readZigbeeSignal(reference)

        assertRequest(captured, "/sensores/sinal-zigbee/v1")
        assertEquals(-70, signal.strength)
    }

    @Test
    fun `readHumidityAndTemperature posts the sensor reference and returns both readings`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = SensorRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"umidade":55.5,"temperatura":23.4}""") }
        )

        val reading = repository.readHumidityAndTemperature(reference)

        assertRequest(captured, "/sensores/umidadeTemperatura/v1")
        assertEquals(55.5, reading.humidity)
        assertEquals(23.4, reading.temperature)
    }

    private fun assertRequest(captured: List<HttpRequestData>, route: String) {
        val recorded = captured.single()
        assertEquals(HttpMethod.Post, recorded.method)
        assertEquals(route, recorded.url.encodedPath)
        assertEquals(referenceBody, recorded.bodyText())
    }
}
