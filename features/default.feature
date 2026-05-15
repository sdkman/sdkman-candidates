Feature: Default Candidate Version

  Background:
    Given the Candidates
      | candidate | name      | description             | default | websiteUrl                  | distribution      |
      | scala     | Scala     | The Scala Language      |         | http://www.scala-lang.org/  | UNIVERSAL         |
      | java      | Java      | The Java Language       |         | https://www.oracle.com      | MULTI_PLATFORM    |
      | cuba      | Cuba      | The Cuba Platform       |         | https://www.cuba-platform.com | PLATFORM_SPECIFIC |
      | micronaut | Micronaut | The Micronaut Framework |         | http://micronaut.io         | UNIVERSAL         |

  Scenario: A Default Version is provided for a Universal Candidate
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor | version |
      | scala     | lts | UNIVERSAL |        | 2.12.0  |
    When a request is made to /default/scala
    Then a 200 status code is received
    And the response body is "2.12.0"

  Scenario: A Default Version is provided for the java Candidate with the Temurin distribution
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor  | version |
      | java      | lts | LINUX_X64 | TEMURIN | 21.0.5  |
    When a request is made to /default/java
    Then a 200 status code is received
    And the response body is "21.0.5"

  Scenario: A Default Version is provided for a Platform Specific Candidate other than java
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor | version |
      | cuba      | lts | LINUX_X64 |        | 7.2.0   |
    When a request is made to /default/cuba
    Then a 200 status code is received
    And the response body is "7.2.0"

  Scenario: A Default Version is requested but the State API has none
    Given no default Version for micronaut tag lts of platform UNIVERSAL on the remote service
    When a request is made to /default/micronaut
    Then a 400 status code is received
    And the response body is ""

  Scenario: A Default Version is requested for an unknown Candidate
    When a request is made to /default/groovy
    Then a 400 status code is received
    And the response body is ""
