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
      | java      | lts | LINUX_X64 | tem     | 21.0.5  |
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

  Scenario: A UNIVERSAL-labelled Candidate whose lts exists only at LINUX_X64 falls back
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor | version |
      | micronaut | lts | LINUX_X64 |        | 4.2.0   |
    And no default Version for micronaut tag lts of platform UNIVERSAL on the remote service
    When a request is made to /default/micronaut
    Then a 200 status code is received
    And the response body is "4.2.0"

  Scenario: A UNIVERSAL-labelled Candidate hosting lts at both platforms resolves the preferred UNIVERSAL version
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor | version |
      | scala     | lts | UNIVERSAL |        | 2.13.0  |
      | scala     | lts | LINUX_X64 |        | 2.12.0  |
    When a request is made to /default/scala
    Then a 200 status code is received
    And the response body is "2.13.0"

  Scenario: A non-UNIVERSAL Candidate whose lts exists only at UNIVERSAL falls back
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor | version |
      | cuba      | lts | UNIVERSAL |        | 7.3.0   |
    And no default Version for cuba tag lts of platform LINUX_X64 on the remote service
    When a request is made to /default/cuba
    Then a 200 status code is received
    And the response body is "7.3.0"

  Scenario: A Default Version is requested but the State API has none at either platform
    Given no default Version for micronaut tag lts of platform UNIVERSAL on the remote service
    And no default Version for micronaut tag lts of platform LINUX_X64 on the remote service
    When a request is made to /default/micronaut
    Then a 400 status code is received
    And the response body is ""

  Scenario: A Default Version is requested for an unknown Candidate
    When a request is made to /default/groovy
    Then a 400 status code is received
    And the response body is ""
