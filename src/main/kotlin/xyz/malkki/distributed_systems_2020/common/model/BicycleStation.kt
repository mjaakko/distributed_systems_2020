package xyz.malkki.distributed_systems_2020.common.model

import org.apache.commons.csv.CSVFormat
import java.io.InputStreamReader

data class BicycleStation(val stationId: Int, val name: String, val latitude: Double, val longitude: Double) {
    companion object {
        fun getBicycleStations(): List<BicycleStation> {
            return this::class.java.classLoader.getResourceAsStream("bicycle_stations.csv").use {
                CSVFormat.RFC4180.withFirstRecordAsHeader().parse(InputStreamReader(it)).records.map {
                    csvRecord -> BicycleStation(
                        csvRecord[0].toInt(),
                        csvRecord[1],
                        csvRecord[2].toDouble(),
                        csvRecord[3].toDouble())
                }
            }
        }
    }
}
