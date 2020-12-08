package xyz.malkki.distributed_systems_2020.analysis_server

import xyz.malkki.distributed_systems_2020.common.model.*
import xyz.malkki.distributed_systems_2020.common.utils.JsonHelper
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class AnalysisServer(private val dataServerAddress: String, private val dataServerPort: Int, private val port: Int = 9998) {
    private val bicycleData = mutableMapOf<BicycleDataKey, BicycleDataValue>()

    private var serverSocket: ServerSocket? = null

    private var count = 0 //Number of threads created
    private val executorService = Executors.newCachedThreadPool { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread.name = "AnalysisServerThread-${count++}"
        thread
    }

    private val started: Boolean
        get() = serverSocket != null

    fun start() {
        if (started) {
            throw IllegalStateException("The server was already started")
        }

        serverSocket = ServerSocket(port)
        println("Server started on ${serverSocket!!.localSocketAddress}")
        Thread {
            while(started) {
                val socket = serverSocket!!.accept()
                println("Accepted connection from ${socket.inetAddress}")
                executorService.submit {
                    socket.use {
                        val input = BufferedInputStream(socket.getInputStream())
                        val output = BufferedOutputStream(socket.getOutputStream())

                        try {
                            val request: AnalysisRequest = JsonHelper.parse(input)

                            val stationId = request.stationId
                            val year = request.year
                            val month = request.month

                            val bicycleDataKey = BicycleDataKey(stationId, year, month)
                            if (!bicycleData.containsKey(bicycleDataKey)) {
                                println("No data found for station $stationId, $year-$month. Fetching from the data server...")
                                bicycleData[bicycleDataKey] = fetchDataFromDataServer(stationId, year, month)
                            }

                            val bicycleData = bicycleData[bicycleDataKey]
                            val ratio = if (bicycleData == null) {
                                0.0
                            } else {
                                bicycleData.journeysFrom.size.toDouble() / bicycleData.journeysTo.size
                            }

                            println("Sending response for station $stationId: $ratio")
                            JsonHelper.write(AnalysisResponse(true, null, stationId, ratio), output)
                        } catch (e: Exception) {
                            println("Error handling request: ${e.message}")
                            JsonHelper.write(AnalysisResponse(false, "Error handling request: ${e.message}", null, null), output)
                        }
                    }
                }
            }
        }.start()
    }

    private fun fetchDataFromDataServer(stationId: Int, year: Int, month: Int): BicycleDataValue {
        val socket = Socket(dataServerAddress, dataServerPort)

        socket.use {
            val input = BufferedInputStream(socket.getInputStream())
            val output = BufferedOutputStream(socket.getOutputStream())

            JsonHelper.write(Request(year, month, stationId), output)

            val response: Response = JsonHelper.parse(input)
            if (response.success) {
                val journeysFrom = response.journeysFromStation!!.filterInvalidJourneys()
                val journeysTo = response.journeysToStation!!.filterInvalidJourneys()

                return BicycleDataValue(journeysFrom, journeysTo)
            } else {
                println("Error requesting data from the server: ${response.message}")
                throw IOException("Failed to fetch data from the server")
            }
        }
    }

    fun stop() {
        serverSocket!!.close()
        serverSocket = null
    }

    //The dataset seems to contain some invalid data where the length of the journey is zero
    private fun List<BicycleJourney>.filterInvalidJourneys() = filter { it.duration > 0 }

    data class BicycleDataKey(val stationId: Int, val year: Int, val month: Int)

    data class BicycleDataValue(val journeysTo: List<BicycleJourney>, val journeysFrom: List<BicycleJourney>)
}