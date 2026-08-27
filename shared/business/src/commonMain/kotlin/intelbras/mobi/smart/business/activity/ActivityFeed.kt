package intelbras.mobi.smart.business.activity

import intelbras.mobi.smart.business.activity.usecase.HomeActivityResult

interface ActivityFeed {
    suspend fun recentActivity(limitPerLock: Int): HomeActivityResult
}
