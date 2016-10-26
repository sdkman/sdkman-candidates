Feature: Health endpoint

	Scenario: Check the status of the service
		When a request is made to the /alive endpoint
		Then an 200 status is received
		And the payload's "status" is "OK"