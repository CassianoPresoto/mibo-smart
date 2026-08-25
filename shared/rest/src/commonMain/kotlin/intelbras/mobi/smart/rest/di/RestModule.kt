package intelbras.mobi.smart.rest.di

import intelbras.mobi.smart.domain.auth.AuthenticationRepository
import intelbras.mobi.smart.domain.camera.CameraRepository
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.streaming.StreamingRepository
import intelbras.mobi.smart.rest.RestConfiguration
import intelbras.mobi.smart.rest.client.EnvelopeReader
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller
import intelbras.mobi.smart.rest.client.createSmartHomeHttpClient
import intelbras.mobi.smart.rest.client.restJson
import intelbras.mobi.smart.rest.repository.AuthenticationRestRepository
import intelbras.mobi.smart.rest.repository.CameraRestRepository
import intelbras.mobi.smart.rest.repository.DeviceRestRepository
import intelbras.mobi.smart.rest.repository.LockRestRepository
import intelbras.mobi.smart.rest.repository.StreamingRestRepository
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

    single<DeviceRepository> { DeviceRestRepository(get()) }
    single<AuthenticationRepository> { AuthenticationRestRepository(get()) }
    single<CameraRepository> { CameraRestRepository(get()) }
    single<StreamingRepository> { StreamingRestRepository(get()) }
    single<LockRepository> { LockRestRepository(get()) }
}
