Feature: Java Version List Footer

  Scenario: An over-long candidate default is truncated in the footer
    Given the default Version on the remote service
      | candidate | tag | platform  | vendor | version              |
      | java      | lts | LINUX_X64 | tem    | 21.0.12-crac+1.2.3.4 |
    And the Candidate
      | candidate | name | description       | default | websiteUrl           |
      | java      | Java | The Java Language |         | https://adoptium.net |
    And the Versions
      | candidate | version | vendor | platform  | url                                       |
      | java      | 8.0.212 | tem    | LINUX_X64 | http://tem.example.org/tem-8.0.212.tar.gz |

    And the installed Versions 8.0.212-tem
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And every response line is at most 80 characters with no trailing whitespace
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor         | Use | Version            | Identifier
    |--------------------------------------------------------------------------------
    | Temurin        |   * | 8.0.212            | 8.0.212-tem
    |================================================================================
    | > in use   * installed   + local only
    |--------------------------------------------------------------------------------
    | $ sdk install java <Identifier>    install a specific version
    | $ sdk install java                 install the default: 21.0.12-crac+1.2.3.4-t>
    | $ sdk install java [TAB]           complete an available identifier
    |================================================================================
    """

  Scenario: The footer falls back to a current lts identifier when no java default resolves
    Given the Candidate
      | candidate | name | description       | default | websiteUrl           |
      | java      | Java | The Java Language |         | https://adoptium.net |
    And the Versions
      | candidate | version | vendor | platform  | url                                       |
      | java      | 8.0.212 | tem    | LINUX_X64 | http://tem.example.org/tem-8.0.212.tar.gz |

    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And every response line is at most 80 characters with no trailing whitespace
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor         | Use | Version            | Identifier
    |--------------------------------------------------------------------------------
    | Temurin        |     | 8.0.212            | 8.0.212-tem
    |================================================================================
    | > in use   * installed   + local only
    |--------------------------------------------------------------------------------
    | $ sdk install java <Identifier>    install a specific version
    | $ sdk install java                 install the default: 25.0.0.0-tem
    | $ sdk install java [TAB]           complete an available identifier
    |================================================================================
    """
