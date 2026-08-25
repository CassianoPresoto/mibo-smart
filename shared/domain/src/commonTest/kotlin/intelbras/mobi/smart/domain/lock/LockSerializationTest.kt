package intelbras.mobi.smart.domain.lock

import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import intelbras.mobi.smart.domain.lock.model.LockVolumeRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class LockSerializationTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `volume level travels as an integer`() {
        val request = LockVolumeRequest(
            serialNumber = "NS_1",
            productId = "42",
            volume = LockVolumeLevel.Medium,
        )

        assertEquals(
            """{"ns":"NS_1","idProduto":"42","volume":2}""",
            json.encodeToString(LockVolumeRequest.serializer(), request),
        )
    }

    @Test
    fun `volume level is rebuilt from the integer`() {
        val request = json.decodeFromString(
            LockVolumeRequest.serializer(),
            """{"ns":"NS_1","idProduto":"42","volume":3}""",
        )

        assertEquals(LockVolumeLevel.High, request.volume)
    }
}
