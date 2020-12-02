package xyz.malkki.distributed_systems_2020.common.model

/**
 * Response from the server
 * @property message Possible error message, null if the request was successful
 * @property journeysFromStation Journeys that start from the station, null if the request was not successful
 * @property journeysToStation Journeys that end at the station, null if the request was not successful
 */
data class Response(val success: Boolean,
                    val stationId: Int?,
                    val message: String?,
                    val journeysFromStation: List<BicycleJourney>?,
                    val journeysToStation: List<BicycleJourney>?)
