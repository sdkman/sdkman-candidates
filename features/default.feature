Feature: Default Candidate Version

  Background:
    Given the Candidates
      | candidate | name      | description             | default   | websiteUrl                    | distribution  |
      | scala     | Scala     | The Scala Language      | 2.12.0    | http://www.scala-lang.org/    | UNIVERSAL     |
      | java      | Java      | The Java Language       | 8u111     | https://www.oracle.com        | MULTI_PLATFORM|
      | micronaut | Micronaut | The Micronaut Framework |           | http://micronaut.io           | UNIVERSAL     |
    And the Versions
      | candidate | version | vendor | platform  | url                                           |
      | scala     | 2.12.0  |        | UNIVERSAL | http://dl/scala/2.12.0/scala-2.12.0.zip       |
      | java      | 8u111   | open   | MAC_OSX   | http://dl/8u111-b14/jdk-8u111-macosx-x64.dmg  |

  Scenario: A Default Version is provided for a Universal Candidate
    When a request is made to /default/scala
    Then a 200 status code is received
    And the response body is "2.12.0"

  Scenario: A Default Version is provided for a Platform Specific Candidate
    When a request is made to /default/java
    Then a 200 status code is received
    And the response body is "8u111"

  Scenario: A Default Version is requested for a Candidate with no Default
    When a request is made to /default/micronaut
    Then a 400 status code is received
    And the response body is ""

  Scenario: A Default Version is requested for an unknown Candidate
    When a request is made to /default/groovy
    Then a 400 status code is received
    And the response body is ""
