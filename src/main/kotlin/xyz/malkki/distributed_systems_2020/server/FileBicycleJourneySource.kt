package xyz.malkki.distributed_systems_2020.server

import xyz.malkki.distributed_systems_2020.common.model.BicycleJourney
import java.io.File
import java.io.FileInputStream
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue

class FileBicycleJourneySource(private val directory: File) : BicycleJourneySource {
    init {
        if (!directory.isDirectory) {
            throw IllegalArgumentException("${directory.absolutePath} is not directory")
        }
    }

    @ExperimentalTime
    override fun getDataForMonth(year: Int, month: Int): List<BicycleJourney> {
        val fileName = "$year-${month.toString().padStart(2, '0')}.csv"
        val file = File(directory, fileName)
        if (!file.exists()) {
            throw IllegalArgumentException("No data found for $year-$month")
        }
        println("Reading bicycle journey data from ${file.absolutePath}")

        val timedValue = TimeSource.Monotonic.measureTimedValue {
            FileInputStream(file).use {
                BicycleJourneyReader.parse(it)
            }
        }
        println("Data read in ${timedValue.duration.inMilliseconds.toInt()}ms")
        return timedValue.value
    }
}