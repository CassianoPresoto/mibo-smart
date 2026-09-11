package intelbras.mobi.smart.player.media

private const val BOX_HEADER_SIZE = 8
private const val LARGE_BOX_HEADER_SIZE = 16
private const val LARGE_SIZE_MARKER = 1L
private const val BOX_TYPE_LENGTH = 4
private const val BYTE_MASK = 0xFFL
private const val BITS_PER_BYTE = 8

const val FRAGMENT_BOX_TYPE = "moof"

data class Mp4BoxBoundary(val streamPosition: Long, val boxType: String)

class FragmentedMp4Scanner {

    private val header = ByteArray(LARGE_BOX_HEADER_SIZE)
    private var headerSize = 0
    private var expectedHeaderSize = BOX_HEADER_SIZE
    private var remainingInBox = 0L
    private var boxStartPosition = 0L
    private var position = 0L

    var readsBoxes = true
        private set

    fun reset() {
        headerSize = 0
        expectedHeaderSize = BOX_HEADER_SIZE
        remainingInBox = 0L
        boxStartPosition = 0L
        position = 0L
        readsBoxes = true
    }

    fun scan(bytes: ByteArray, offset: Int, length: Int): List<Mp4BoxBoundary> {
        if (!readsBoxes) {
            position += length
            return emptyList()
        }

        val boundaries = mutableListOf<Mp4BoxBoundary>()
        val end = offset + length
        var index = offset

        while (index < end) {
            if (remainingInBox > 0) {
                index += skipBoxContent(availableFrom(index, end))
                continue
            }
            index += fillHeader(bytes, index, end)
            if (headerSize < expectedHeaderSize) break

            val box = readHeader() ?: return boundaries.also { giveUpFrom(index, end) }
            if (box.needsLargeSize) continue

            boundaries += Mp4BoxBoundary(boxStartPosition, box.type)
            remainingInBox = box.size - expectedHeaderSize
            headerSize = 0
            expectedHeaderSize = BOX_HEADER_SIZE
        }
        return boundaries
    }

    private fun availableFrom(index: Int, end: Int): Int =
        minOf(remainingInBox, (end - index).toLong()).toInt()

    private fun skipBoxContent(available: Int): Int {
        remainingInBox -= available
        position += available
        return available
    }

    private fun fillHeader(bytes: ByteArray, index: Int, end: Int): Int {
        if (headerSize == 0) boxStartPosition = position
        val taken = minOf(expectedHeaderSize - headerSize, end - index)
        bytes.copyInto(header, headerSize, index, index + taken)
        headerSize += taken
        position += taken
        return taken
    }

    private fun giveUpFrom(index: Int, end: Int) {
        readsBoxes = false
        position += end - index
    }

    private fun readHeader(): BoxHeader? {
        val declaredSize = unsignedInt(startingAt = 0)
        if (declaredSize == LARGE_SIZE_MARKER && expectedHeaderSize == BOX_HEADER_SIZE) {
            expectedHeaderSize = LARGE_BOX_HEADER_SIZE
            return BoxHeader(type = "", size = 0L, needsLargeSize = true)
        }

        val type = boxType()
        val size = if (expectedHeaderSize == LARGE_BOX_HEADER_SIZE) {
            unsignedLong(startingAt = BOX_HEADER_SIZE)
        } else {
            declaredSize
        }
        if (!type.looksLikeBoxType() || size < expectedHeaderSize) return null
        return BoxHeader(type = type, size = size, needsLargeSize = false)
    }

    private fun boxType(): String = buildString {
        repeat(BOX_TYPE_LENGTH) { offset -> append(header[BOX_TYPE_LENGTH + offset].toInt().toChar()) }
    }

    private fun unsignedInt(startingAt: Int): Long = bigEndian(startingAt, BOX_TYPE_LENGTH)

    private fun unsignedLong(startingAt: Int): Long = bigEndian(startingAt, BOX_HEADER_SIZE)

    private fun bigEndian(startingAt: Int, byteCount: Int): Long {
        var value = 0L
        repeat(byteCount) { offset ->
            value = (value shl BITS_PER_BYTE) or (header[startingAt + offset].toLong() and BYTE_MASK)
        }
        return value
    }

    private fun String.looksLikeBoxType(): Boolean =
        length == BOX_TYPE_LENGTH && all { character -> character.isLetterOrDigit() || character == ' ' }

    private data class BoxHeader(val type: String, val size: Long, val needsLargeSize: Boolean)
}
