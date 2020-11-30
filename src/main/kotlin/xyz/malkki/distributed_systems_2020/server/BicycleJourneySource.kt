package xyz.malkki.distributed_systems_2020.server

import xyz.malkki.distributed_systems_2020.common.model.BicycleJourney

interface BicycleJourneySource {
    fun getDataForMonth(year: Int, month: Int): List<BicycleJourney>
}