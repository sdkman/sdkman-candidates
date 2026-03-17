Feature: Version List by Platform

  Background:
    Given the Candidate
      | candidate | name      | description         | default    | websiteUrl                  | distribution      |
      | java      | Java      | The Java Language   | 8u111-open | https://www.oracle.com      | PLATFORM_SPECIFIC |
      | scala     | Scala     | The Scala Language  | 2.12.6     | https://www.scala-lang.org/ | UNIVERSAL         |
      | micronaut | Micronaut | Micronaut framework | 2.0.0      | https://micronaut.io/       | MIXED             |

    And the Versions
      | candidate | version     | vendor | platform    | url                                                                                       |
      | java      | 8u111-open  | open   | LINUX_X64   | http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.tar.gz          |
      | java      | 8u121-open  | open   | LINUX_X64   | http://download.oracle.com/otn-pub/java/jdk/8u121-b14/jdk-8u121-linux-x64.tar.gz          |
      | java      | 8u131-open  | open   | LINUX_X64   | http://download.oracle.com/otn-pub/java/jdk/8u131-b14/jdk-8u131-linux-x64.tar.gz          |
      | java      | 9ea163-open | open   | LINUX_X64   | http://download.java.net/java/jdk9/archive/163/binaries/jdk-9-ea+163_linux-x64_bin.tar.gz |
      | java      | 8u131-open  | open   | MAC_X64     | http://download.oracle.com/otn-pub/java/jdk/8u131-b14/jdk-8u131-osx-x64.tar.gz            |
      | java      | 9ea163-open | open   | MAC_X64     | http://download.java.net/java/jdk9/archive/163/binaries/jdk-9-ea+163_osx-x64_bin.tar.gz   |
      | java      | 8u111-open  | open   | WINDOWS_X64 | http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-windows-x64.tar.gz        |
      | java      | 8u121-open  | open   | WINDOWS_X64 | http://download.oracle.com/otn-pub/java/jdk/8u121-b14/jdk-8u121-windows-x64.tar.gz        |
      | scala     | 2.12.6      |        | UNIVERSAL   | http://dl/scala/2.12.0/scala-2.12.0.zip                                                   |
      | micronaut | 2.0.0       |        | LINUX_X64   | http://dl/micronaut/2.0.0/mn-1.3.5-linux.zip                                              |
      | micronaut | 2.0.0       |        | MAC_X64     | http://dl/micronaut/2.0.0/mn-1.3.5-osx.zip                                                |
      | micronaut | 2.0.0       |        | WINDOWS_X64 | http://dl/micronaut/2.0.0/mn-1.3.5-windows.zip                                            |
      | micronaut | 1.3.5       |        | UNIVERSAL   | http://dl/micronaut/1.3.5/mn-1.3.5.zip                                                    |

  Scenario: Show a Version List of a Platform Specific Linux 64 Candidate
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 9ea163       | open    |            | 9ea163-open
    |               |     | 8u131        | open    |            | 8u131-open
    |               |     | 8u121        | open    |            | 8u121-open
    |               |     | 8u111        | open    |            | 8u111-open
    |================================================================================
    |Omit Identifier to install default version 8u111-open:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 8u111-open
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: Show a Version List of a Platform Specific Mac OSX Candidate
    When a request is made to /candidates/java/darwinx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for macOS 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 9ea163       | open    |            | 9ea163-open
    |               |     | 8u131        | open    |            | 8u131-open
    |================================================================================
    |Omit Identifier to install default version 8u111-open:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 8u111-open
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: Show a Version List of a Platform Specific Windows Candidate
    When a request is made to /candidates/java/windowsx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Cygwin
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 8u121        | open    |            | 8u121-open
    |               |     | 8u111        | open    |            | 8u111-open
    |================================================================================
    |Omit Identifier to install default version 8u111-open:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 8u111-open
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: Show a Version List for a Universal Candidate
    Given no Versions for scala of platform WINDOWS_X64 on the remote service
    When a request is made to /candidates/scala/windowsx64/versions/list
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

  Scenario: Show a Version List for a Mixed Platform and Universal Candidate
    When a request is made to /candidates/micronaut/darwinx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Micronaut Versions
      |================================================================================
      |     2.0.0
      |     1.3.5
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
