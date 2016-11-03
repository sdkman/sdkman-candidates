Feature: Candidate Version Validation by Platform

	Background:
		Given a "java" Candidate of Version "8u111" for platform "MAC_OSX" at "http://dl/8u111-b14/jdk-8u111-macosx-x64.dmg"
		And a "java" Candidate of Version "8u111" for platform "LINUX" at "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"
		And a "java" Candidate of Version "8u111" for platform "CYGWIN64" at "http://dl/8u111-b14/jdk-8u111-windows-x64.exe"

	Scenario: Validation succeeds for known platform
		When I attempt validation at endpoint /candidates/java/8u111/linux with "plain/text" Accept Header
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation fails for unsupported platform
		When I attempt validation at endpoint /candidates/java/8u111/freebsd with "plain/text" Accept Header
		Then a 404 status code is received
		And the response body is "invalid"

	Scenario: Validation fails for unknown platform
		When I attempt validation at endpoint /candidates/java/8u111/zxspectrum with "plain/text" Accept Header
		Then a 404 status code is received
		And the response body is "invalid"

	@pending
	Scenario: Validation succeeds in JSON
		When I attempt validation at endpoint /candidates/java/8u111/linux with "application/json" Accept Header
		Then a 200 status code is received
		And the payload has a "valid" of "true"

	@pending
	Scenario: Validation fails in JSON
		When I attempt validation at endpoint /candidates/java/8u111/freebsd with "application/json" Accept Header
		Then a 404 status code is received
		And the payload has a "valid" of "false"
