Feature: Version List by Visibility

  Scenario: Versions which are visible or don't contain the `visible` field are displayed
    Given the Candidate
      | candidate | name | description   | default    | websiteUrl           | distribution      |
      | java      | Java | Java Platform | 17.0.0-tem | https://adoptium.net | PLATFORM_SPECIFIC |
    And the Versions
      | candidate | version       | vendor | platform  | url                                                                                                                  | visible |
      | java      | 8.0.222.hs     | tem   | LINUX_X64 | https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz | true    |
      | java      | 8.0.272.hs     | tem   | LINUX_X64 | https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u272-b10/OpenJDK8U-jdk_x64_linux_hotspot_8u272b10.tar.gz |         |
      | java      | 8.0.275.hs     | tem   | LINUX_X64 | https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u275-b01/OpenJDK8U-jdk_x64_linux_hotspot_8u275b01.tar.gz | false   |
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor         | Use | Version            | Identifier
    |--------------------------------------------------------------------------------
    | Temurin        |     | 8.0.272.hs         | 8.0.272.hs-tem
    |                |     | 8.0.222.hs         | 8.0.222.hs-tem
    |================================================================================
    | > in use   * installed   + local only
    |--------------------------------------------------------------------------------
    | $ sdk install java <Identifier>    install a specific version
    | $ sdk install java                 install the default: 17.0.0-tem
    | $ sdk install java [TAB]           complete an available identifier
    |================================================================================
    """
