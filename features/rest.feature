Feature: REST endpoint

	Scenario: Check the status of service
		When a request is made to the /status endpoint
		Then an 200 status is received
		And the payload's "status" is "OK"