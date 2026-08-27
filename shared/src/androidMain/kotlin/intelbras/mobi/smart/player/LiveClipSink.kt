package intelbras.mobi.smart.player

import intelbras.mobi.smart.domain.capture.model.ClipRecordingOutcome
import intelbras.mobi.smart.domain.capture.model.ClipRecordingStart
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import intelbras.mobi.smart.player.media.FRAGMENT_BOX_TYPE
import intelbras.mobi.smart.player.media.FragmentedMp4Scanner
import intelbras.mobi.smart.player.media.Mp4BoxBoundary
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private const val INIT_SEGMENT_LIMIT_BYTES = 4 * 1024 * 1024

internal class LiveClipSink {

    private val lock = Any()
    private val scanner = FragmentedMp4Scanner()
    private val openingBytes = ByteArrayOutputStream()

    private var initSegment: ByteArray? = null
    private var streamPosition = 0L
    private var recording: ClipRecording? = null

    fun onStreamOpened() = synchronized(lock) {
        scanner.reset()
        openingBytes.reset()
        initSegment = null
        streamPosition = 0L
    }

    fun onBytesRead(bytes: ByteArray, offset: Int, length: Int) = synchronized(lock) {
        val bufferStart = streamPosition
        val boundaries = scanner.scan(bytes, offset, length)
        streamPosition += length

        rememberInitSegment(bytes, offset, length, boundaries)
        recording?.write(bytes, offset, length, bufferStart, boundaries)
    }

    fun startRecording(destination: MediaFileDestination): ClipRecordingStart = synchronized(lock) {
        val opening = initSegment ?: return ClipRecordingStart.Unsupported
        if (recording != null) return ClipRecordingStart.Unsupported

        return try {
            recording = ClipRecording(destination, opening)
            ClipRecordingStart.Started
        } catch (failure: Throwable) {
            recording = null
            ClipRecordingStart.Failed(failure)
        }
    }

    fun finishRecording(): ClipRecordingOutcome = synchronized(lock) {
        val current = recording ?: return ClipRecordingOutcome.NothingRecorded
        recording = null
        current.close()
    }

    private fun rememberInitSegment(
        bytes: ByteArray,
        offset: Int,
        length: Int,
        boundaries: List<Mp4BoxBoundary>,
    ) {
        if (initSegment != null) return
        if (!scanner.readsBoxes || openingBytes.size() > INIT_SEGMENT_LIMIT_BYTES) {
            openingBytes.reset()
            return
        }

        openingBytes.write(bytes, offset, length)
        val firstFragment = boundaries.firstOrNull { boundary -> boundary.boxType == FRAGMENT_BOX_TYPE }
            ?: return

        initSegment = openingBytes.toByteArray().copyOf(firstFragment.streamPosition.toInt())
        openingBytes.reset()
    }
}

private class ClipRecording(
    private val destination: MediaFileDestination,
    initSegment: ByteArray,
) {
    private val file = File(destination.directoryPath, destination.fileName)
    private val output = FileOutputStream(file)
    private var writesFragments = false

    init {
        output.write(initSegment)
    }

    fun write(
        bytes: ByteArray,
        offset: Int,
        length: Int,
        bufferStart: Long,
        boundaries: List<Mp4BoxBoundary>,
    ) {
        if (writesFragments) {
            output.write(bytes, offset, length)
            return
        }

        val fragment = boundaries.firstOrNull { boundary ->
            boundary.boxType == FRAGMENT_BOX_TYPE && boundary.streamPosition >= bufferStart
        } ?: return

        val fragmentIndex = offset + (fragment.streamPosition - bufferStart).toInt()
        writesFragments = true
        output.write(bytes, fragmentIndex, length - (fragmentIndex - offset))
    }

    fun close(): ClipRecordingOutcome = try {
        output.flush()
        output.close()
        outcomeOf()
    } catch (failure: Throwable) {
        file.delete()
        ClipRecordingOutcome.Failed(failure)
    }

    private fun outcomeOf(): ClipRecordingOutcome {
        if (!writesFragments) {
            file.delete()
            return ClipRecordingOutcome.NothingRecorded
        }
        return ClipRecordingOutcome.Recorded(
            fileName = destination.fileName,
            sizeBytes = file.length(),
        )
    }
}
