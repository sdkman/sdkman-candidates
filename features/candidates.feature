Feature: Candidates

	Scenario: Find all Candidate names in the order the State API returns them
		Given the Candidates
			| candidate | name      | description             | default | websiteUrl                  |
			| scala     | Scala     | The Scala Language      | 2.12.0  | http://www.scala-lang.org/  |
			| groovy    | Groovy    | The Groovy Language     | 2.4.7   | http://www.groovy-lang.org/ |
			| java      | Java      | The Java Language       |         | https://www.oracle.com      |
			| micronaut | Micronaut | The Micronaut Framework |         | http://micronaut.io         |
		When a request is made to /candidates/all
		Then a 200 status code is received
		And the response body is "scala,groovy,java,micronaut"
		And the State API received exactly 1 request for the candidate registry
