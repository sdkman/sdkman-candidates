Feature: Versions

  Scenario: Find all Versions for a given Candidate and Platform
    Given the Candidate
      | candidate | name | description       | default | websiteUrl             | distribution      |
      | java      | Java | The Java Language | 8u111   | https://www.oracle.com | PLATFORM_SPECIFIC |
    And the Versions
      | candidate | version | platform | url                                                                                       |
      | java      | 8u111   | LINUX_64 | http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.tar.gz          |
      | java      | 8u121   | LINUX_64 | http://download.oracle.com/otn-pub/java/jdk/8u121-b14/jdk-8u121-linux-x64.tar.gz          |
      | java      | 8u131   | LINUX_64 | http://download.oracle.com/otn-pub/java/jdk/8u131-b14/jdk-8u131-linux-x64.tar.gz          |
      | java      | 9ea163  | LINUX_64 | http://download.java.net/java/jdk9/archive/163/binaries/jdk-9-ea+163_linux-x64_bin.tar.gz |
    When a request is made to /candidates/java/linux64/versions/all
    Then a 200 status code is received
    And the response body is "8u111,8u121,8u131,9ea163"

  Scenario: Find no Versions for a a given Candidate with no Default Version
    Given the Candidate
      | candidate | name      | description             | default | websiteUrl             | distribution   |
      | micronaut | Micronaut | The Micronaut Framework |         | http://micronaut.io    | UNIVERSAL      |
    When a request is made to /candidates/micronaut/linux64/versions/all
    Then a 200 status code is received
    And the response body is ""