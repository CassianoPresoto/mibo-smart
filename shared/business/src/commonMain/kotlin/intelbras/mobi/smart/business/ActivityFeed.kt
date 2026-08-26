package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.HomeActivityResult

interface ActivityFeed {
    suspend fun recentActivity(limitPerLock: Int): HomeActivityResult
}
