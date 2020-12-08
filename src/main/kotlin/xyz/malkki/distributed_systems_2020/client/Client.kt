package xyz.malkki.distributed_systems_2020.client

import xyz.malkki.distributed_systems_2020.common.model.AnalysisRequest
import xyz.malkki.distributed_systems_2020.common.model.AnalysisResponse
import xyz.malkki.distributed_systems_2020.common.model.BicycleStation
import xyz.malkki.distributed_systems_2020.common.utils.JsonHelper
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.lang.Exception
import java.math.RoundingMode
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

class Client(private val analysisServers: List<Pair<String, Int>>) {
    //Number of threads created
    private var count = 0
    //Using max 16 threads to avoid too many simultaneous connections
    private val executorService = Executors.newFixedThreadPool(16) { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread.name = "ClientThread-${count++}"
        thread
    }

    private fun String?.isValidInput(): Boolean = this?.toIntOrNull() != null

    @ExperimentalTime
    fun run() {
        while(true) {
            println("Year? (Enter non-numeric value to stop the loop)")
            val yearStr = readLine()
            if (!yearStr.isValidInput()) {
                break
            }
            println("Month? (Enter non-numeric value to stop the loop)")
            val monthStr = readLine()
            if (!monthStr.isValidInput()) {
                break
            }

            val year = yearStr!!.toInt()
            val month = monthStr!!.toInt()

            val stations = BicycleStation.getBicycleStations().associateBy { it.stationId }

            val availableServers = mutableListOf(*analysisServers.toTypedArray())
            val tasks = mutableMapOf<Int, Future<Double>>()
            val results = mutableMapOf<Int, Double>()

            val start = TimeSource.Monotonic.markNow()

            fun scheduleTask(stationId: Int) {
                val (serverAddress, serverPort) = synchronized(this) {
                    if (availableServers.size == 0) {
                        throw IllegalStateException("No servers available")
                    }

                    val serverNumber = stationId % availableServers.size
                    return@synchronized availableServers[serverNumber]
                }

                tasks[stationId] = executorService.submit(Callable {
                    try {
                        val socket = Socket(serverAddress, serverPort)
                        socket.use {
                            val input = BufferedInputStream(socket.getInputStream())
                            val output = BufferedOutputStream(socket.getOutputStream())

                            JsonHelper.write(AnalysisRequest(stationId, year, month), output)
                            socket.shutdownOutput()

                            val response: AnalysisResponse = JsonHelper.parse(input)
                            if (response.success) {
                                return@Callable response.ratioInOut
                            } else {
                                println("Analysis of the data for station $stationId, year: $year, month: $month was not successful (${response.message})")
                                error("The server could not analyse the data")
                            }
                        }
                    } catch (exception: Exception) {
                        println("Failed to fetch data from analysis server (${exception.message}), retrying from another server..")
                        synchronized(this) {
                            availableServers.remove(serverAddress to serverPort)
                            println("Remaining available servers: $availableServers")
                        }
                        throw exception
                    }
                })
            }

            stations.keys.forEach { stationId -> scheduleTask(stationId) }
            while (results.size != tasks.size) {
                tasks.entries.forEach { stationIdAndFuture ->
                    val stationId = stationIdAndFuture.key
                    //We already have results for the station
                    if (results.containsKey(stationId)) {
                        return@forEach
                    }

                    val future = stationIdAndFuture.value
                    try {
                        results[stationId] = future.get()
                        println("Progress: ${(100.0 * results.size / tasks.size.toDouble()).toBigDecimal().setScale(2, RoundingMode.HALF_UP)}")
                    } catch (exception: Exception) {
                        //Retry task
                        scheduleTask(stationId = stationIdAndFuture.key)
                    }
                }
            }

            val sortedResults = results.entries
                    .filter { !it.value.isNaN() }
                    .associateBy(keySelector = { stations[it.key]!! }, valueTransform = { it.value })
                    .map { (bicycleStation, ratio) -> bicycleStation to ratio }
                    .sortedBy { it.second }

            sortedResults.forEach { (station, ratio) ->
                println("Ratio of journeys to / from station ${station.name}: $ratio")
            }
            println("Data analysed in ${start.elapsedNow().inSeconds} seconds")
        }
    }
}