package intelbras.mobi.smart.rest.client

import intelbras.mobi.smart.rest.SmartHomeInvalidRequestException
import intelbras.mobi.smart.rest.SmartHomeOperationRejectedException
import intelbras.mobi.smart.rest.SmartHomeUnexpectedResponseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

internal class EnvelopeReader(private val json: Json) {

    fun <T> read(payload: String, deserializer: DeserializationStrategy<T>): T {
        val body = bodyOf(payload) ?: throw malformed(payload)
        failOnRejection(body)
        val data = body[DATA_FIELD] ?: throw SmartHomeUnexpectedResponseException(MISSING_DATA)
        return decode(data, deserializer)
    }

    fun readAcknowledgement(payload: String) {
        if (payload.isBlank()) return
        val body = bodyOf(payload) ?: throw malformed(payload)
        failOnRejection(body)
    }

    fun messageIn(payload: String): String {
        val root = rootOf(payload) ?: return ""
        (root[BODY_FIELD] as? JsonPrimitive)?.contentOrNull?.let { return it }

        val body = root[BODY_FIELD] as? JsonObject ?: root
        return body.text(MESSAGE_FIELD) ?: body.text(ALTERNATIVE_MESSAGE_FIELD).orEmpty()
    }

    fun reportedStatusIn(payload: String): Int? =
        (rootOf(payload)?.get(STATUS_CODE_FIELD) as? JsonPrimitive)?.intOrNull

    private fun <T> decode(data: JsonElement, deserializer: DeserializationStrategy<T>): T = try {
        json.decodeFromJsonElement(deserializer, data)
    } catch (failure: Exception) {
        throw SmartHomeUnexpectedResponseException(
            message = failure.message ?: UNREADABLE_DATA,
            cause = failure,
        )
    }

    private fun bodyOf(payload: String): JsonObject? {
        val root = rootOf(payload) ?: return null
        return root[BODY_FIELD] as? JsonObject ?: root
    }

    private fun rootOf(payload: String): JsonObject? =
        runCatching { json.parseToJsonElement(payload) }.getOrNull() as? JsonObject

    private fun failOnRejection(body: JsonObject) {
        val status = body.text(STATUS_FIELD) ?: return
        if (status.equals(SUCCESS_STATUS, ignoreCase = true)) return

        val message = body.text(MESSAGE_FIELD) ?: body.text(ALTERNATIVE_MESSAGE_FIELD).orEmpty()
        if (message.contains(INVALID_PARAMETER, ignoreCase = true)) {
            throw SmartHomeInvalidRequestException(message)
        }
        throw SmartHomeOperationRejectedException(message.ifBlank { GENERIC_REJECTION })
    }

    private fun JsonObject.text(field: String): String? =
        (this[field] as? JsonPrimitive)?.contentOrNull

    private fun malformed(payload: String) = SmartHomeUnexpectedResponseException(
        "$MALFORMED_BODY ${payload.take(MAX_REPORTED_PAYLOAD)}"
    )

    private companion object {
        const val BODY_FIELD = "body"
        const val STATUS_CODE_FIELD = "statusCode"
        const val DATA_FIELD = "data"
        const val STATUS_FIELD = "status"
        const val MESSAGE_FIELD = "msg"
        const val ALTERNATIVE_MESSAGE_FIELD = "message"
        const val SUCCESS_STATUS = "sucesso"
        const val INVALID_PARAMETER = "Parâmetro inválido"
        const val GENERIC_REJECTION = "The platform refused the operation"
        const val MISSING_DATA = "Response without the 'data' field"
        const val UNREADABLE_DATA = "Could not read the 'data' field"
        const val MALFORMED_BODY = "Response outside the expected format:"
        const val MAX_REPORTED_PAYLOAD = 120
    }
}
