Feature: Candidates

	Scenario: Find all Candidate names regardless of Distribution
		Given the Candidates
			| candidate | name      | description             | default   | websiteUrl                    | distribution  |
			| scala     | Scala     | The Scala Language      | 2.12.0    | http://www.scala-lang.org/    | UNIVERSAL     |
			| groovy    | Groovy    | The Groovy Language     | 2.4.7     | http://www.groovy-lang.org/   | UNIVERSAL     |
			| java      | Java      | The Java Language       | 8u111     | https://www.oracle.com        | MULTI_PLATFORM|
			| micronaut | Micronaut | The Micronaut Framework |           | http://micronaut.io           | UNIVERSAL     |
		When a request is made to /candidates/all
		Then a 200 status code is received
		And the response body is "groovy,java,micronaut,scala"