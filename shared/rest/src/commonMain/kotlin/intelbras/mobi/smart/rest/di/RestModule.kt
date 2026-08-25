package intelbras.mobi.smart.rest.di

import intelbras.mobi.smart.rest.RestConfiguration
import intelbras.mobi.smart.rest.client.EnvelopeReader
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller
import intelbras.mobi.smart.rest.client.createSmartHomeHttpClient
import intelbras.mobi.smart.rest.client.restJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun restModule(
    configuration: RestConfiguration = RestConfiguration(),
    engine: HttpClientEngine? = null,
): Module = module {
    single { configuration }
    single { restJson() }
    single<HttpClient> { createSmartHomeHttpClient(get(), engine) }
    single { EnvelopeReader(get<Json>()) }
    single { SmartHomeApiCaller(get(), get(), get(), get()) }
}
