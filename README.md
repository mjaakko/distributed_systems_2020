Project for Distributed Systems course.

## Usage

1. Build runnable jar with `./gradlew shadowJar`
2. Run the application with provided scripts

### Data server

1. Download bicycle journey data from [HSL open data portal](https://classic.hsl.fi/en/opendata) (section **City bike trips**) to `bikedata` directory.
2. Run the application with `./run_data_server.sh`

### Analysis server

1. Run the application with `./run_analysis_server.sh <port_number>`, where `<port_number>` is the port number which the analysis server should listen.

### Client

1. Run the client with `./run_client.sh <analysis_servers>` where `<analysis_servers>` contains a comma-separated list of the analysis servers (e.g. `./run_client.sh localhost:9000,localhost:9001,localhost:9002`)
