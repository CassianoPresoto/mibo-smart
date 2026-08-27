package intelbras.mobi.smart.business.activity

import intelbras.mobi.smart.business.activity.usecase.HomeActivityReading
import intelbras.mobi.smart.business.activity.usecase.HomeActivityResult

internal class ActivityFeedImpl(
    private val homeActivityReading: HomeActivityReading,
) : ActivityFeed {

    override suspend fun recentActivity(limitPerLock: Int): HomeActivityResult =
        homeActivityReading(limitPerLock)
}
