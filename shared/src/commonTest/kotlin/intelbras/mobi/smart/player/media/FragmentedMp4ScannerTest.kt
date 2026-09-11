package intelbras.mobi.smart.player.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val BOX_HEADER_SIZE = 8

class FragmentedMp4ScannerTest {

    private val scanner = FragmentedMp4Scanner()

    @Test
    fun `reads every top level box of a fragmented stream`() {
        val stream = box("ftyp", payloadSize = 16) +
            box("moov", payloadSize = 40) +
            box("moof", payloadSize = 24) +
            box("mdat", payloadSize = 64)

        val boundaries = scanner.scan(stream, offset = 0, length = stream.size)

        assertEquals(
            listOf("ftyp" to 0L, "moov" to 24L, "moof" to 72L, "mdat" to 104L),
            boundaries.map { boundary -> boundary.boxType to boundary.streamPosition },
        )
    }

    @Test
    fun `keeps the fragment position when its header arrives split in two blocks`() {
        val stream = box("ftyp", payloadSize = 8) + box("moof", payloadSize = 16)
        val cut = 19

        val firstBlock = scanner.scan(stream, offset = 0, length = cut)
        val secondBlock = scanner.scan(stream, offset = cut, length = stream.size - cut)

        assertEquals(listOf("ftyp"), firstBlock.map { boundary -> boundary.boxType })
        assertEquals(
            listOf(FRAGMENT_BOX_TYPE to 16L),
            secondBlock.map { boundary -> boundary.boxType to boundary.streamPosition },
        )
    }

    @Test
    fun `gives up when the stream is not made of boxes`() {
        val stream = ByteArray(64) { index -> (index * 7).toByte() }

        val boundaries = scanner.scan(stream, offset = 0, length = stream.size)

        assertTrue(boundaries.isEmpty())
        assertFalse(scanner.readsBoxes)
    }

    @Test
    fun `stops reading boxes once the stream stopped making sense`() {
        val stream = box("ftyp", payloadSize = 8) + ByteArray(BOX_HEADER_SIZE) { 0 }

        scanner.scan(stream, offset = 0, length = stream.size)

        assertFalse(scanner.readsBoxes)
    }

    @Test
    fun `starts over when the connection is opened again`() {
        val stream = box("ftyp", payloadSize = 8)
        scanner.scan(stream, offset = 0, length = stream.size)

        scanner.reset()
        val boundaries = scanner.scan(stream, offset = 0, length = stream.size)

        assertEquals(listOf(0L), boundaries.map { boundary -> boundary.streamPosition })
    }

    private fun box(type: String, payloadSize: Int): ByteArray {
        val size = BOX_HEADER_SIZE + payloadSize
        val bytes = ByteArray(size)
        bytes[0] = (size ushr 24).toByte()
        bytes[1] = (size ushr 16).toByte()
        bytes[2] = (size ushr 8).toByte()
        bytes[3] = size.toByte()
        type.forEachIndexed { index, character -> bytes[4 + index] = character.code.toByte() }
        return bytes
    }
}
