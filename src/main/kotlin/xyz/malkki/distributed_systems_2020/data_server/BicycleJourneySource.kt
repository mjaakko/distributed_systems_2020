package xyz.malkki.distributed_systems_2020.data_server

import xyz.malkki.distributed_systems_2020.common.model.BicycleJourney

interface BicycleJourneySource {
    fun getDataForMonth(year: Int, month: Int): List<BicycleJourney>
}