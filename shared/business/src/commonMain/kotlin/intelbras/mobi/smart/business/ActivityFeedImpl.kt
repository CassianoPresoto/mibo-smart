package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.HomeActivityReading
import intelbras.mobi.smart.business.usecase.HomeActivityResult

internal class ActivityFeedImpl(
    private val homeActivityReading: HomeActivityReading,
) : ActivityFeed {

    override suspend fun recentActivity(limitPerLock: Int): HomeActivityResult =
        homeActivityReading(limitPerLock)
}
