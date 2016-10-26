#Play Scala REST service seed with Cucumber

This template forms the base of a REST microservice.

Testing is performed from outside-in by providing Cucumber and Scalatest.

Feature files can be found in the `features` folder, with step defs living beneath `test/steps`. The Cucumber runtime is bootstrapped by `RunCukes.scala`.

##Testing

A simple end-to-end scenario is provided in the `health.feature`, testing a `HealthController` with an `/alive` endpoint.

To run the Cucumber suite, spin up the service as follows:

	$ sbt run

Once running, the tests can be run from within an IDE, or else from the command line using:

	$ sbt test

Enjoy!!!