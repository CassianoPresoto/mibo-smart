package intelbras.mobi.smart.persistence.di

import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.persistence.SmartHomePersistenceFactory
import intelbras.mobi.smart.persistence.auth.AccessTokenSecretStore
import intelbras.mobi.smart.persistence.auth.ProtectedAccessTokenStore
import intelbras.mobi.smart.persistence.createSmartHomeDatabase
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun persistenceModule(persistenceFactory: SmartHomePersistenceFactory): Module = module {
    single<SmartHomeDatabase> { createSmartHomeDatabase(persistenceFactory) }
    single<AccessTokenSecretStore> { persistenceFactory.createSecretStore() }
    single<AccessTokenStore> { ProtectedAccessTokenStore(get(), get(), Dispatchers.Default) }
}
