Feature: Default Candidate Version

  Scenario: A Default Version is provided for a Candidate from its cached record
    Given the Candidates
      | candidate | name      | description             | default | websiteUrl                 |
      | scala     | Scala     | The Scala Language      | 2.12.0  | http://www.scala-lang.org/ |
      | micronaut | Micronaut | The Micronaut Framework |         | http://micronaut.io        |
    When a request is made to /default/scala
    Then a 200 status code is received
    And the response body is "2.12.0"
    And the State API received exactly 0 tag lookups for scala

  Scenario: A Candidate whose cached record has no Default Version
    Given the Candidates
      | candidate | name      | description             | default | websiteUrl                 |
      | scala     | Scala     | The Scala Language      | 2.12.0  | http://www.scala-lang.org/ |
      | micronaut | Micronaut | The Micronaut Framework |         | http://micronaut.io        |
    When a request is made to /default/micronaut
    Then a 400 status code is received
    And the response body is ""
    And the State API received exactly 0 tag lookups for micronaut

  Scenario: A Default Version is provided for the java Candidate with the Temurin distribution
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor | version |
      | java      | lts | LINUX_X64 | tem    | 21.0.5  |
    And the Candidates
      | candidate | name | description       | default | websiteUrl             |
      | java      | Java | The Java Language |         | https://www.oracle.com |
    When a request is made to /default/java
    Then a 200 status code is received
    And the response body is "21.0.5-tem"
    And the State API received exactly 1 tag lookup for java at platform LINUX_X64 with distribution TEMURIN
    And the State API received exactly 1 tag lookup for java

  Scenario: A Default Version for java is absent
    Given no default Version for java tag lts of platform LINUX_X64 on the remote service
    And the Candidates
      | candidate | name | description       | default | websiteUrl             |
      | java      | Java | The Java Language |         | https://www.oracle.com |
    When a request is made to /default/java
    Then a 400 status code is received
    And the response body is ""
    And the State API received exactly 1 tag lookup for java at platform LINUX_X64 with distribution TEMURIN
    And the State API received exactly 1 tag lookup for java

  Scenario: A Default Version is requested for an unknown Candidate
    Given the Candidates
      | candidate | name  | description        | default | websiteUrl                 |
      | scala     | Scala | The Scala Language | 2.12.0  | http://www.scala-lang.org/ |
    When a request is made to /default/groovy
    Then a 400 status code is received
    And the response body is ""
    And the State API received exactly 0 tag lookups for groovy
