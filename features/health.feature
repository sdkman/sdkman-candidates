Feature: Health Check endpoint

	Scenario: Check if the service is alive
		When a request is made to /alive
		Then a 200 status code is received
		And the payload has a "status" of "OK"