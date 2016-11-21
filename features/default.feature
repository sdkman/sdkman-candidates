Feature: Default Candidate Version

  Background:
    Given the Candidates
      | candidate | name      | description           | default   | websiteUrl                    | distribution  |
      | scala     | Scala     | The Scala Language    | 2.12.0    | http://www.scala-lang.org/    | UNIVERSAL     |
      | java      | Java      | The Java Language     | 8u111     | https://www.oracle.com        | MULTI_PLATFORM|
    And a "scala" Candidate of Version "2.12.0" for platform "UNIVERSAL" at "http://dl/scala/2.12.0/scala-2.12.0.zip"
    And a "java" Candidate of Version "8u111" for platform "MAC_OSX" at "http://dl/8u111-b14/jdk-8u111-macosx-x64.dmg"

  Scenario: A Default Version is provided for a Universal Candidate
    When a request is made to the /default/scala endpoint
    Then a 200 status code is received
    And the response body is "2.12.0"

  Scenario: A Default Version is provided for a Platform Specific Candidate
    When a request is made to the /default/java endpoint
    Then a 200 status code is received
    And the response body is "8u111"

  Scenario: A Default Version is requested for an unknown Candidate
    When a request is made to the /default/groovy endpoint
    Then a 400 status code is received
    And the response body is ""
