package xyz.malkki.distributed_systems_2020

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import xyz.malkki.distributed_systems_2020.analysis_server.AnalysisServer
import xyz.malkki.distributed_systems_2020.client.Client
import xyz.malkki.distributed_systems_2020.data_server.CachedBicycleJourneySource
import xyz.malkki.distributed_systems_2020.data_server.FileBicycleJourneySource
import xyz.malkki.distributed_systems_2020.data_server.DataServer
import java.io.File
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(vararg args: String) {
    val cliOptions = Options().apply {
        addOption("i", true, "Address of the data server (in format host:port)")
        addOption("d", true, "Path to the directory that contains bicycle journey data")
        addOption("p", true, "Port which is used when running in server mode (defaults: 9999 for data server, 9998 for analysis server)")
        addOption("y", true, "List of analysis servers (in format: host1:port1,host2:port2,...)")
        addOption("s", "Run the application in data server mode")
        addOption("a", "Run the application in analysis server mode")
        addOption("c", "Run the application in client mode")
        addOption("h", "help", false, "Show options")
    }
    val cli = DefaultParser().parse(cliOptions, args)
    if (cli.hasOption("h")) {
        HelpFormatter().printHelp("app", cliOptions)
        return
    }

    when {
        cli.hasOption("s") -> {
            println("Running the application in data server mode")
            val dataDirectory = File(cli.getOptionValue("d"))
            DataServer(bicycleJourneySource = CachedBicycleJourneySource(FileBicycleJourneySource(dataDirectory))).start()
        }
        cli.hasOption("a") -> {
            println("Running the application in analysis mode")
            val (serverHost, serverPort) = cli.getOptionValue("i").split(":", limit = 2)
            val port = cli.getOptionValue("p").toInt()
            AnalysisServer(serverHost, serverPort.toInt(), port = port).start()
        }
        cli.hasOption("c") -> {
            println("Running the application in client mode")
            val servers = cli.getOptionValue("y")
                    .split(",")
                    .map { server -> server.split(":", limit = 2) }
                    .map { (host, port) -> host to port.toInt() }
            Client(servers).run()
        }
        else -> {
            println("The application has to be run either in client (option -c) or server (option -s) mode")
        }
    }
}