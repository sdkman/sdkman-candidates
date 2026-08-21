Feature: Java Version List Footer

  Background:
    Given the Candidate
      | candidate | name | description       | default                     | websiteUrl           | distribution      |
      | java      | Java | The Java Language | 21.0.12-crac+1.2.3.4-librca | https://adoptium.net | PLATFORM_SPECIFIC |

  Scenario: An over-long candidate default is truncated in the footer
    # The candidate default is interpolated after a fixed 57-character prefix, so a
    # default longer than 23 characters used to push that footer line past 80
    # characters — the one line width the layout spec could not otherwise guarantee.
    # The `>` marker keeps the elision visible: the shown value is not installable.
    Given the Versions
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
    | $ sdk install java                 install the default: 21.0.12-crac+1.2.3.4-l>
    | $ sdk install java [TAB]           complete an available identifier
    |================================================================================
    """
