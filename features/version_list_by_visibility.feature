Feature: Version List by Visibility

  Scenario: Versions which are visible or don't contain the `visible` field are displayed
    Given the Candidate
      | candidate | name | description   | default        | websiteUrl                | distribution      |
      | java      | Java | Java Platform | 11.0.9.hs-adpt | https://adoptopenjdk.net/ | PLATFORM_SPECIFIC |
    And the Versions
      | candidate | version         | vendor | platform | url                                                                                                                              | visible |
      | java      | 8.0.222.hs-adpt | adpt   | LINUX_64 | https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz | true    |
      | java      | 8.0.272.hs-adpt | adpt   | LINUX_64 | https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u272-b10/OpenJDK8U-jdk_x64_linux_hotspot_8u272b10.tar.gz |         |
      | java      | 8.0.275.hs-adpt | adpt   | LINUX_64 | https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u275-b01/OpenJDK8U-jdk_x64_linux_hotspot_8u275b01.tar.gz | false   |
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | AdoptOpenJDK  |     | 8.0.272.hs   | adpt    |            | 8.0.272.hs-adpt
    |               |     | 8.0.222.hs   | adpt    |            | 8.0.222.hs-adpt
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """