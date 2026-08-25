package intelbras.mobi.smart.domain.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable(with = TestLevelSerializer::class)
internal enum class TestLevel(override val code: Int) : IntCoded {
    Low(1),
    High(9),
}

internal object TestLevelSerializer :
    IntCodedSerializer<TestLevel>("TestLevel", TestLevel.entries.toTypedArray())

class IntCodedSerializerTest {

    @Test
    fun `writes the enum as its integer code`() {
        assertEquals("9", Json.encodeToString(TestLevelSerializer, TestLevel.High))
    }

    @Test
    fun `reads the enum back from its integer code`() {
        assertEquals(TestLevel.Low, Json.decodeFromString(TestLevelSerializer, "1"))
    }

    @Test
    fun `refuses a code the enum does not declare`() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString(TestLevelSerializer, "7")
        }
    }
}
