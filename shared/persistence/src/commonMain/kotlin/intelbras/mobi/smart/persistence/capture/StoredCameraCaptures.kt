package intelbras.mobi.smart.persistence.capture

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import intelbras.mobi.smart.domain.capture.CameraCaptureStore
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class StoredCameraCaptures(
    database: SmartHomeDatabase,
    private val ioDispatcher: CoroutineDispatcher,
) : CameraCaptureStore {

    private val queries = database.cameraCaptureQueries

    override fun capturesOf(deviceSerialNumber: String): Flow<List<CameraCapture>> =
        queries.selectByDevice(deviceSerialNumber)
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.mapNotNull { row -> row.toCaptureOrNull() } }

    override suspend fun save(capture: CameraCapture): Unit = withContext(ioDispatcher) {
        queries.replaceCapture(
            id = capture.id,
            deviceSerialNumber = capture.deviceSerialNumber,
            kind = capture.kindCode(),
            fileName = capture.fileName,
            previewFileName = capture.previewFileName,
            capturedAtEpochMilliseconds = capture.capturedAtEpochMilliseconds,
            durationMilliseconds = capture.durationOrNull(),
            sizeBytes = capture.sizeBytes,
        )
    }

    override suspend fun remove(captureId: String): Unit = withContext(ioDispatcher) {
        queries.deleteCapture(captureId)
    }
}
