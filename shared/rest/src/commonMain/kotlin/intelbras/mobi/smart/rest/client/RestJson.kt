package intelbras.mobi.smart.rest.client

import kotlinx.serialization.json.Json

fun restJson(): Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    isLenient = true
}
