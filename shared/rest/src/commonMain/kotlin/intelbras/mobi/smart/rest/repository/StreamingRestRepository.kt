package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.core.EmptyRequest
import intelbras.mobi.smart.domain.streaming.StreamingRepository
import intelbras.mobi.smart.domain.streaming.model.StreamingQuota
import intelbras.mobi.smart.domain.streaming.model.StreamingSession
import intelbras.mobi.smart.domain.streaming.model.StreamingSessionReference
import intelbras.mobi.smart.rest.client.ApiRoutes
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller
import kotlinx.serialization.builtins.ListSerializer

internal class StreamingRestRepository(
    private val caller: SmartHomeApiCaller,
) : StreamingRepository {

    override suspend fun readAvailableQuota(): StreamingQuota =
        caller.query(
            route = ApiRoutes.AVAILABLE_QUOTA,
            body = EmptyRequest,
            bodySerializer = EmptyRequest.serializer(),
            responseDeserializer = StreamingQuota.serializer(),
        )

    override suspend fun listSessions(): List<StreamingSession> =
        caller.query(
            route = ApiRoutes.MY_SESSIONS,
            body = EmptyRequest,
            bodySerializer = EmptyRequest.serializer(),
            responseDeserializer = ListSerializer(StreamingSession.serializer()),
        )

    override suspend fun readSession(reference: StreamingSessionReference): StreamingSession =
        caller.query(
            route = ApiRoutes.SESSION_INFO,
            body = reference,
            bodySerializer = StreamingSessionReference.serializer(),
            responseDeserializer = StreamingSession.serializer(),
        )

    override suspend fun endSession(reference: StreamingSessionReference) =
        caller.command(
            route = ApiRoutes.END_SESSION,
            body = reference,
            bodySerializer = StreamingSessionReference.serializer(),
        )
}
