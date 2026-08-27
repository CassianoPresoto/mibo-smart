package intelbras.mobi.smart.player

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener

@UnstableApi
internal class RecordingDataSourceFactory(
    private val delegate: DataSource.Factory,
    private val sink: LiveClipSink,
) : DataSource.Factory {

    override fun createDataSource(): DataSource = RecordingDataSource(delegate.createDataSource(), sink)
}

@UnstableApi
private class RecordingDataSource(
    private val delegate: DataSource,
    private val sink: LiveClipSink,
) : DataSource {

    override fun open(dataSpec: DataSpec): Long {
        sink.onStreamOpened()
        return delegate.open(dataSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val read = delegate.read(buffer, offset, length)
        if (read > 0) sink.onBytesRead(buffer, offset, read)
        return read
    }

    override fun addTransferListener(transferListener: TransferListener) {
        delegate.addTransferListener(transferListener)
    }

    override fun getUri(): Uri? = delegate.uri

    override fun getResponseHeaders(): Map<String, List<String>> = delegate.responseHeaders

    override fun close() {
        delegate.close()
    }
}
