Feature: Versions

  Scenario: Find all Versions for a given Platform Specific Candidate
    Given the Candidate
      | candidate | name | description       | default | websiteUrl             | distribution      |
      | java      | Java | The Java Language | 8u111   | https://www.oracle.com | PLATFORM_SPECIFIC |
    And the Versions
      | candidate | version | vendor | platform  | url                                                                                       |
      | java      | 8u111   | open   | LINUX_X64 | http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.tar.gz          |
      | java      | 8u121   | open   | LINUX_X64 | http://download.oracle.com/otn-pub/java/jdk/8u121-b14/jdk-8u121-linux-x64.tar.gz          |
      | java      | 8u131   | open   | LINUX_X64 | http://download.oracle.com/otn-pub/java/jdk/8u131-b14/jdk-8u131-linux-x64.tar.gz          |
      | java      | 9ea163  | open   | LINUX_X64 | http://download.java.net/java/jdk9/archive/163/binaries/jdk-9-ea+163_linux-x64_bin.tar.gz |
    And no Versions for java of platform UNIVERSAL on the remote service
    When a request is made to /candidates/java/linuxx64/versions/all
    Then a 200 status code is received
    And the response body is "8u111,8u121,8u131,9ea163"

  Scenario: Find all Versions for a given Universal Candidate
    Given the Candidate
      | candidate | name  | description        | default | websiteUrl                  | distribution |
      | scala     | Scala | The Scala Language | 2.12.6  | https://www.scala-lang.org/ | UNIVERSAL    |
    And the Versions
      | candidate | version | vendor | platform  | url                                     |
      | scala     | 2.12.6  |        | UNIVERSAL | http://dl/scala/2.12.6/scala-2.12.6.zip |
      | scala     | 2.12.5  |        | UNIVERSAL | http://dl/scala/2.12.5/scala-2.12.5.zip |
      | scala     | 2.11.8  |        | UNIVERSAL | http://dl/scala/2.11.8/scala-2.11.8.zip |
    And no Versions for scala of platform LINUX_X64 on the remote service
    When a request is made to /candidates/scala/linuxx64/versions/all
    Then a 200 status code is received
    And the response body is "2.11.8,2.12.5,2.12.6"

  Scenario: Find no Versions for a given Candidate with no Default Version
    Given the Candidate
      | candidate | name      | description             | default | websiteUrl          | distribution |
      | micronaut | Micronaut | The Micronaut Framework |         | http://micronaut.io | LINUX_X64    |
    And no Versions for micronaut of platform LINUX_X64 on the remote service
    And no Versions for micronaut of platform UNIVERSAL on the remote service
    When a request is made to /candidates/micronaut/linuxx64/versions/all
    Then a 200 status code is received
    And the response body is ""

  Scenario: Find all Versions for a Candidate registered as UNIVERSAL but hosting platform-specific Versions
    Given the Candidate
      | candidate | name | description         | default    | websiteUrl              | distribution |
      | jmc       | JMC  | JDK Mission Control | 9.1.1-zulu | https://jdk.java.net/jmc | UNIVERSAL    |
    And the Versions
      | candidate | version | vendor | platform  | url                                                    |
      | jmc       | 8.3.0   | zulu   | LINUX_X64 | https://downloads/jmc/8.3.0/jmc-8.3.0-linux-x64.tar.gz |
      | jmc       | 9.1.1   | zulu   | LINUX_X64 | https://downloads/jmc/9.1.1/jmc-9.1.1-linux-x64.tar.gz |
    And no Versions for jmc of platform UNIVERSAL on the remote service
    When a request is made to /candidates/jmc/linuxx64/versions/all
    Then a 200 status code is received
    And the response body is "8.3.0,9.1.1"