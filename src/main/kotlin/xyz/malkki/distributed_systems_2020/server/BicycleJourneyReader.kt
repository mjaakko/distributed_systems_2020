package xyz.malkki.distributed_systems_2020.server

import org.apache.commons.csv.CSVFormat
import xyz.malkki.distributed_systems_2020.common.model.BicycleJourney
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.time.LocalDateTime

object BicycleJourneyReader {
    private val csvFormat = CSVFormat.RFC4180.withFirstRecordAsHeader()

    fun parse(inputStream: InputStream): List<BicycleJourney> {
        return csvFormat.parse(BufferedReader(InputStreamReader(inputStream)))
            .mapNotNull { record -> try {
                    BicycleJourney(
                            LocalDateTime.parse(record[0]),
                            LocalDateTime.parse(record[1]),
                            record[2].toInt(),
                            record[3],
                            record[4].toInt(),
                            record[5],
                            record[6].toFloat(),
                            record[7].toInt())
                } catch (e: Exception) {
                    println("Failed to parse record: $record (${e.message})")
                    null
                }
            }
            .toList()
    }
}