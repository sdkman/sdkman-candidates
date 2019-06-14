Feature: Version List by Platform

  Background:
    Given the Candidate
      | candidate | name  | description        | default | websiteUrl                  | distribution      |
      | java      | Java  | The Java Language  | 8u111   | https://www.oracle.com      | PLATFORM_SPECIFIC |
      | scala     | Scala | The Scala Language | 2.12.6  | https://www.scala-lang.org/ | UNIVERSAL         |

    And the Versions
      | candidate | version      | vendor | platform   | url                                                                                       |
      | java      | 8u111-open   | open   | LINUX_64   | http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.tar.gz          |
      | java      | 8u121-open   | open   | LINUX_64   | http://download.oracle.com/otn-pub/java/jdk/8u121-b14/jdk-8u121-linux-x64.tar.gz          |
      | java      | 8u131-open   | open   | LINUX_64   | http://download.oracle.com/otn-pub/java/jdk/8u131-b14/jdk-8u131-linux-x64.tar.gz          |
      | java      | 9ea163-open  | open   | LINUX_64   | http://download.java.net/java/jdk9/archive/163/binaries/jdk-9-ea+163_linux-x64_bin.tar.gz |
      | java      | 8u131-open   | open   | MAC_OSX    | http://download.oracle.com/otn-pub/java/jdk/8u131-b14/jdk-8u131-osx-x64.tar.gz            |
      | java      | 9ea163-open  | open   | MAC_OSX    | http://download.java.net/java/jdk9/archive/163/binaries/jdk-9-ea+163_osx-x64_bin.tar.gz   |
      | java      | 8u111-open   | open   | WINDOWS_64 | http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-windows-x64.tar.gz        |
      | java      | 8u121-open   | open   | WINDOWS_64 | http://download.oracle.com/otn-pub/java/jdk/8u121-b14/jdk-8u121-windows-x64.tar.gz        |
      | scala     | 2.12.6       | none   | UNIVERSAL  | http://dl/scala/2.12.0/scala-2.12.0.zip                                                   |

  Scenario: Show a Version List of a Platform Specific Linux 64 Candidate
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version       | Dist   | Status     | Identifier
    |--------------------------------------------------------------------------------
    | java.net      |     | 9ea163        | open   |            | 9ea163-open
    |               |     | 8u131         | open   |            | 8u131-open
    |               |     | 8u121         | open   |            | 8u121-open
    |               |     | 8u111         | open   |            | 8u111-open
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: Show a Version List of a Platform Specific Mac OSX Candidate
    When a request is made to /candidates/java/darwin/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version       | Dist   | Status     | Identifier
    |--------------------------------------------------------------------------------
    | java.net      |     | 9ea163        | open   |            | 9ea163-open
    |               |     | 8u131         | open   |            | 8u131-open
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: Show a Version List of a Platform Specific Cygwin Candidate
    When a request is made to /candidates/java/CYGWIN_NT-6.3/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version       | Dist   | Status     | Identifier
    |--------------------------------------------------------------------------------
    | java.net      |     | 8u121         | open   |            | 8u121-open
    |               |     | 8u111         | open   |            | 8u111-open
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: Show a Version List for a Universal Candidate
    When a request is made to /candidates/scala/CYGWIN_NT-6.3/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      |     2.12.6
      |
      |
      |
      |
      |
      |
      |
      |
      |
      |
      |
      |
      |
      |
      |
      |================================================================================
      |+ - local version
      |* - installed
      |> - currently in use
      |================================================================================
    """