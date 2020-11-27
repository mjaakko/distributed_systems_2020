package xyz.malkki.distributed_systems_2020.common.model

import java.time.LocalDateTime

/**
 * Describes a bicycle journey from station A to station B
 * @property distance Distance of the journey in meters
 * @property duration Duration of the journey in seconds
 */
data class BicycleJourney(val startTime: LocalDateTime,
                     val endTime: LocalDateTime,
                     val fromStationId: Int,
                     val fromStationName: String,
                     val toStationId: Int,
                     val toStationName: String,
                     val distance: Float,
                     val duration: Int) {
    val averageSpeed: Float = distance / duration
}