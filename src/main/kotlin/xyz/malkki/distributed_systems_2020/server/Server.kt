package xyz.malkki.distributed_systems_2020.server

import xyz.malkki.distributed_systems_2020.common.model.Request
import xyz.malkki.distributed_systems_2020.common.model.Response
import xyz.malkki.distributed_systems_2020.common.utils.JsonHelper
import java.io.BufferedOutputStream
import java.net.ServerSocket
import java.util.concurrent.Executors

class Server(private val port: Int = 9999, private val bicycleJourneySource: BicycleJourneySource) {
    private var serverSocket: ServerSocket? = null

    private var count = 0 //Number of threads created
    private val executorService = Executors.newCachedThreadPool { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread.name = "ServerThread-${count++}"
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
                        val input = socket.getInputStream()
                        val output = BufferedOutputStream(socket.getOutputStream())

                        try {
                            val request: Request = JsonHelper.parse(input)
                            println("Request received: $request")
                            val (journeysFromStation, journeysToStation) = bicycleJourneySource
                                .getDataForMonth(request.year, request.month)
                                .filter { journey -> journey.fromStationId == request.station || journey.toStationId == request.station }
                                .partition { bicycleJourney -> bicycleJourney.fromStationId == request.station }

                            println("Sending data for ${journeysFromStation.size + journeysToStation.size} bicycle journeys")
                            val response = Response(true, request.station, null, journeysFromStation, journeysToStation)
                            JsonHelper.write(response, output)
                        } catch (e: Exception) {
                            println("Error handling request: ${e.message}")
                            JsonHelper.write(Response(false, null, e.message, null, null), output)
                        }
                    }
                }
            }
        }.start()
    }

    fun stop() {
        serverSocket!!.close()
        serverSocket = null
    }
}