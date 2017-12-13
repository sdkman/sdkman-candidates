# SDKMAN! Candidates Service

This service supersedes the [Legacy Candidates Service](https://github.com/sdkman/sdkman-candidates-legacy).

## Running Locally

Make sure you have mongodb running locally or in a Docker Container:

    $ docker run -d --net=host --name mongo mongo:3.2

Start the application:

    $ sbt run

Run the cukes:

    $ sbt test


