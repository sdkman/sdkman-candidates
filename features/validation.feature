Feature: Candidate Version Validation by Platform

	Background:
		Given a "java" Candidate of Version "8u111" for platform "MAC_OSX" at "http://dl/8u111-b14/jdk-8u111-macosx-x64.dmg"
		And a "java" Candidate of Version "8u111" for platform "LINUX" at "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"
		And a "java" Candidate of Version "8u111" for platform "CYGWIN64" at "http://dl/8u111-b14/jdk-8u111-windows-x64.exe"

	Scenario: Validation succeeds in JSON
		When I attempt validation at endpoint /candidates/java/8u111/linux with "application/json" Accept Header
		Then a 200 status code is received
		And the response JSON contains valid field as true

	Scenario: Validation fails in JSON
		When I attempt validation at endpoint /candidates/java/8u111/freebsd with "application/json" Accept Header
		Then a 200 status code is received
		And the response JSON contains valid field as false

	Scenario: Validation succeeds in Plain Text
		When I attempt validation at endpoint /candidates/java/8u111/linux with "plain/text" Accept Header
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation fails in Plain Text
		When I attempt validation at endpoint /candidates/java/8u111/freebsd with "plain/text" Accept Header
		Then a 200 status code is received
		And the response body is "invalid"

