# SDKMAN! Candidates Service

![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/sdkman/sdkman-candidates)

This service supersedes the [Legacy Candidates Service](https://github.com/sdkman/sdkman-candidates-legacy).

## Running Locally

Make sure you have mongodb running locally or in a Docker Container:

    $ docker run -d --net=host --name mongo mongo:3.2

Run all tests:

    $ sbt test
