Feature: Candidate Registry Outage

	# With no candidate set, the candidate endpoints answer 503 with an empty
	# body: any non-empty body would be written into the CLI's candidates cache.

	Scenario: Find all Candidate names with no candidate set
		Given the candidate registry is cold and the State API fails with status 503
		When a request is made to /candidates/all
		Then a 503 status code is received
		And the response body is ""

	Scenario: Render the Candidate list with no candidate set
		Given the candidate registry is cold and the State API fails with status 503
		When a request is made to /candidates/list
		Then a 503 status code is received
		And the response body is ""

	Scenario: Resolve a Candidate default with no candidate set
		Given the candidate registry is cold and the State API fails with status 503
		When a request is made to /default/scala
		Then a 503 status code is received
		And the response body is ""

	Scenario: List Candidate Versions with no candidate set
		Given the candidate registry is cold and the State API fails with status 503
		When a request is made to /candidates/scala/linuxx64/versions/list
		Then a 503 status code is received
		And the response body is ""

	# The java listing needs no candidate record; its footer takes the fallback.
	Scenario: List Java Versions with no candidate set
		Given the candidate registry is cold and the State API fails with status 503
		And the Versions
			| candidate | version | vendor | platform  | url                                       |
			| java      | 8.0.212 | tem    | LINUX_X64 | http://tem.example.org/tem-8.0.212.tar.gz |
		When a request is made to /candidates/java/linuxx64/versions/list
		Then a 200 status code is received
		And the response body is
		"""
		|================================================================================
		|Available Java Versions for Linux 64bit
		|================================================================================
		| Vendor         | Use | Version            | Identifier
		|--------------------------------------------------------------------------------
		| Temurin        |     | 8.0.212            | 8.0.212-tem
		|================================================================================
		| > in use   * installed   + local only
		|--------------------------------------------------------------------------------
		| $ sdk install java <Identifier>    install a specific version
		| $ sdk install java                 install the default: 25.0.0.0-tem
		| $ sdk install java [TAB]           complete an available identifier
		|================================================================================
		"""

	# /ping backs the CLI availability probe; failing it prints the
	# INTERNET NOT REACHABLE banner in every shell.
	Scenario: Ping with no candidate set
		Given the candidate registry is cold and the State API fails with status 503
		When a request is made to /ping
		Then a 200 status code is received

	Scenario: A failed refresh keeps serving the previous candidate set
		Given the Candidates
			| candidate | name   | description         | default | websiteUrl                  | distribution |
			| scala     | Scala  | The Scala Language  | 2.12.0  | http://www.scala-lang.org/  | UNIVERSAL    |
			| groovy    | Groovy | The Groovy Language | 2.4.7   | http://www.groovy-lang.org/ | UNIVERSAL    |
		And the candidate registry refresh fails with status 503
		When a request is made to /candidates/all
		Then a 200 status code is received
		And the response body is "scala,groovy"
