package intelbras.mobi.smart.persistence

import intelbras.mobi.smart.persistence.db.SmartHomeDatabase

internal fun createSmartHomeDatabase(persistenceFactory: SmartHomePersistenceFactory): SmartHomeDatabase =
    SmartHomeDatabase(persistenceFactory.createDriver())
