# SDKMAN! Candidates Service

![Build status](https://github.com/sdkman/sdkman-candidates/actions/workflows/release.yml/badge.svg)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/sdkman/sdkman-candidates)

This service supersedes the [Legacy Candidates Service](https://github.com/sdkman/sdkman-candidates-legacy).

## Tests

```
$ ./sbt test
```

## Run local

Make sure you have mongodb running locally or in a Docker Container:

```
$ docker run -d --net=host --name mongo mongo:3.2
```

Start the app:

```
$ ./sbt run
```
