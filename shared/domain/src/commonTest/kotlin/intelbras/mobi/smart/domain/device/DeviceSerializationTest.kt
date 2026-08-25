package intelbras.mobi.smart.domain.device

import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class DeviceSerializationTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `origin filter travels with the terms the platform accepts`() {
        val query = DeviceListQuery(page = 1, pageSize = 20, origin = DeviceOriginFilter.Shared)

        assertEquals(
            """{"pagina":1,"tamanhoPagina":20,"origem":"compartilhados"}""",
            json.encodeToString(DeviceListQuery.serializer(), query),
        )
    }

    @Test
    fun `capabilities arrive as a comma separated list`() {
        val capabilities = DeviceCapabilities("RTSV1, AudioTalk ,LocalRecord")

        assertEquals(listOf("RTSV1", "AudioTalk", "LocalRecord"), capabilities.values)
        assertTrue(capabilities.supports("audiotalk"))
        assertFalse(capabilities.supports("CloudRecord"))
    }
}
