package intelbras.mobi.smart.rest.client

import intelbras.mobi.smart.rest.RestConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

fun createSmartHomeHttpClient(
    configuration: RestConfiguration,
    engine: HttpClientEngine? = null,
): HttpClient {
    val setup: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
        expectSuccess = false

        install(HttpTimeout) {
            requestTimeoutMillis = configuration.requestTimeoutInMillis
            connectTimeoutMillis = configuration.requestTimeoutInMillis
            socketTimeoutMillis = configuration.requestTimeoutInMillis
        }

        install(Logging) {
            level = if (configuration.logRequests) LogLevel.INFO else LogLevel.NONE
            sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
        }

        defaultRequest {
            url(configuration.baseUrl)
            accept(ContentType.Application.Json)
        }
    }
    return if (engine == null) HttpClient(setup) else HttpClient(engine, setup)
}
