package xyz.malkki.distributed_systems_2020.server

import xyz.malkki.distributed_systems_2020.common.model.BicycleJourney
import xyz.malkki.distributed_systems_2020.common.utils.LockByKey
import java.lang.ref.SoftReference

class CachedBicycleJourneySource(private val bicycleJourneySource: BicycleJourneySource) : BicycleJourneySource {
    private val cache = mutableMapOf<Pair<Int, Int>, SoftReference<List<BicycleJourney>>>()
    private val lock = LockByKey<Pair<Int, Int>>()

    override fun getDataForMonth(year: Int, month: Int): List<BicycleJourney> {
        val key = year to month

        return lock.run(key) {
            val maybeBicycleJourneys = cache[key]?.get()
            if (maybeBicycleJourneys != null) {
                return@run maybeBicycleJourneys
            } else {
                val bicycleJourneys = bicycleJourneySource.getDataForMonth(year, month)
                cache[key] = SoftReference(bicycleJourneys)
                return@run bicycleJourneys
            }
        }
    }
}