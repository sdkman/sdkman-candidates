Feature: Version List

  Background:
    Given the Candidate
      | candidate | name  | description        | default | websiteUrl                  | distribution |
      | scala     | Scala | The Scala Language | 2.12.6  | https://www.scala-lang.org/ | UNIVERSAL    |

  Scenario: A single column list of available uninstalled Versions are displayed
    Given the UNIVERSAL scala Versions 2.11.1 thru 2.11.8
    And the UNIVERSAL scala Versions 2.12.0 thru 2.12.6
    And these Versions are available on the remote service
    And no Versions for scala of platform LINUX_X64 on the remote service
    When a request is made to /candidates/scala/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      |     2.12.6
      |     2.12.5
      |     2.12.4
      |     2.12.3
      |     2.12.2
      |     2.12.1
      |     2.12.0
      |     2.11.8
      |     2.11.7
      |     2.11.6
      |     2.11.5
      |     2.11.4
      |     2.11.3
      |     2.11.2
      |     2.11.1
      |
      |================================================================================
      |+ - local version
      |* - installed
      |> - currently in use
      |================================================================================
    """

  Scenario: A multiple column list of available uninstalled Versions are displayed
    Given the UNIVERSAL scala Versions 2.9.0 thru 2.9.14
    And the UNIVERSAL scala Versions 2.10.0 thru 2.10.14
    And the UNIVERSAL scala Versions 2.11.0 thru 2.11.14
    And the UNIVERSAL scala Versions 2.12.0 thru 2.12.14
    And these Versions are available on the remote service
    And no Versions for scala of platform LINUX_X64 on the remote service
    When a request is made to /candidates/scala/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      |     2.12.14             2.11.14             2.10.14             2.9.14
      |     2.12.13             2.11.13             2.10.13             2.9.13
      |     2.12.12             2.11.12             2.10.12             2.9.12
      |     2.12.11             2.11.11             2.10.11             2.9.11
      |     2.12.10             2.11.10             2.10.10             2.9.10
      |     2.12.9              2.11.9              2.10.9              2.9.9
      |     2.12.8              2.11.8              2.10.8              2.9.8
      |     2.12.7              2.11.7              2.10.7              2.9.7
      |     2.12.6              2.11.6              2.10.6              2.9.6
      |     2.12.5              2.11.5              2.10.5              2.9.5
      |     2.12.4              2.11.4              2.10.4              2.9.4
      |     2.12.3              2.11.3              2.10.3              2.9.3
      |     2.12.2              2.11.2              2.10.2              2.9.2
      |     2.12.1              2.11.1              2.10.1              2.9.1
      |     2.12.0              2.11.0              2.10.0              2.9.0
      |
      |================================================================================
      |+ - local version
      |* - installed
      |> - currently in use
      |================================================================================
    """

  Scenario: Current version is displayed
    Given the UNIVERSAL scala Versions 2.9.0 thru 2.9.14
    And the UNIVERSAL scala Versions 2.10.0 thru 2.10.14
    And the UNIVERSAL scala Versions 2.11.0 thru 2.11.14
    And the UNIVERSAL scala Versions 2.12.0 thru 2.12.14
    And these Versions are available on the remote service
    And no Versions for scala of platform LINUX_X64 on the remote service
    And the scala Version 2.12.14 is installed
    And the scala Version 2.12.14 is set as current
    When a request is made to /candidates/scala/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      | > * 2.12.14             2.11.14             2.10.14             2.9.14
      |     2.12.13             2.11.13             2.10.13             2.9.13
      |     2.12.12             2.11.12             2.10.12             2.9.12
      |     2.12.11             2.11.11             2.10.11             2.9.11
      |     2.12.10             2.11.10             2.10.10             2.9.10
      |     2.12.9              2.11.9              2.10.9              2.9.9
      |     2.12.8              2.11.8              2.10.8              2.9.8
      |     2.12.7              2.11.7              2.10.7              2.9.7
      |     2.12.6              2.11.6              2.10.6              2.9.6
      |     2.12.5              2.11.5              2.10.5              2.9.5
      |     2.12.4              2.11.4              2.10.4              2.9.4
      |     2.12.3              2.11.3              2.10.3              2.9.3
      |     2.12.2              2.11.2              2.10.2              2.9.2
      |     2.12.1              2.11.1              2.10.1              2.9.1
      |     2.12.0              2.11.0              2.10.0              2.9.0
      |
      |================================================================================
      |+ - local version
      |* - installed
      |> - currently in use
      |================================================================================
    """

  Scenario: Installed Versions are displayed
    Given the UNIVERSAL scala Versions 2.9.0 thru 2.9.14
    And the UNIVERSAL scala Versions 2.10.0 thru 2.10.14
    And the UNIVERSAL scala Versions 2.11.0 thru 2.11.14
    And the UNIVERSAL scala Versions 2.12.0 thru 2.12.14
    And these Versions are available on the remote service
    And no Versions for scala of platform LINUX_X64 on the remote service
    And the scala Versions 2.12.14,2.12.13,2.12.12,2.11.14,2.10.14,2.9.14 are installed
    When a request is made to /candidates/scala/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      |   * 2.12.14           * 2.11.14           * 2.10.14           * 2.9.14
      |   * 2.12.13             2.11.13             2.10.13             2.9.13
      |   * 2.12.12             2.11.12             2.10.12             2.9.12
      |     2.12.11             2.11.11             2.10.11             2.9.11
      |     2.12.10             2.11.10             2.10.10             2.9.10
      |     2.12.9              2.11.9              2.10.9              2.9.9
      |     2.12.8              2.11.8              2.10.8              2.9.8
      |     2.12.7              2.11.7              2.10.7              2.9.7
      |     2.12.6              2.11.6              2.10.6              2.9.6
      |     2.12.5              2.11.5              2.10.5              2.9.5
      |     2.12.4              2.11.4              2.10.4              2.9.4
      |     2.12.3              2.11.3              2.10.3              2.9.3
      |     2.12.2              2.11.2              2.10.2              2.9.2
      |     2.12.1              2.11.1              2.10.1              2.9.1
      |     2.12.0              2.11.0              2.10.0              2.9.0
      |
      |================================================================================
      |+ - local version
      |* - installed
      |> - currently in use
      |================================================================================
    """

  Scenario: Local Versions are displayed
    Given the UNIVERSAL scala Versions 2.12.0 thru 2.12.13
    And these Versions are available on the remote service
    And no Versions for scala of platform LINUX_X64 on the remote service
    And the scala Versions 2.12.14,2.12.13,2.12.12,2.11.14,2.10.14,2.9.14 are installed
    When a request is made to /candidates/scala/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      |   + 2.12.14           + 2.11.14
      |   * 2.12.13           + 2.10.14
      |   * 2.12.12           + 2.9.14
      |     2.12.11
      |     2.12.10
      |     2.12.9
      |     2.12.8
      |     2.12.7
      |     2.12.6
      |     2.12.5
      |     2.12.4
      |     2.12.3
      |     2.12.2
      |     2.12.1
      |     2.12.0
      |
      |================================================================================
      |+ - local version
      |* - installed
      |> - currently in use
      |================================================================================
    """

  Scenario: Version List expands to an arbitrary length
    Given the UNIVERSAL scala Versions 2.9.0 thru 2.9.30
    And the UNIVERSAL scala Versions 2.10.0 thru 2.10.30
    And the UNIVERSAL scala Versions 2.11.0 thru 2.11.30
    And the UNIVERSAL scala Versions 2.12.0 thru 2.12.30
    And these Versions are available on the remote service
    And no Versions for scala of platform LINUX_X64 on the remote service
    When a request is made to /candidates/scala/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      |     2.12.30             2.11.30             2.10.30             2.9.30
      |     2.12.29             2.11.29             2.10.29             2.9.29
      |     2.12.28             2.11.28             2.10.28             2.9.28
      |     2.12.27             2.11.27             2.10.27             2.9.27
      |     2.12.26             2.11.26             2.10.26             2.9.26
      |     2.12.25             2.11.25             2.10.25             2.9.25
      |     2.12.24             2.11.24             2.10.24             2.9.24
      |     2.12.23             2.11.23             2.10.23             2.9.23
      |     2.12.22             2.11.22             2.10.22             2.9.22
      |     2.12.21             2.11.21             2.10.21             2.9.21
      |     2.12.20             2.11.20             2.10.20             2.9.20
      |     2.12.19             2.11.19             2.10.19             2.9.19
      |     2.12.18             2.11.18             2.10.18             2.9.18
      |     2.12.17             2.11.17             2.10.17             2.9.17
      |     2.12.16             2.11.16             2.10.16             2.9.16
      |     2.12.15             2.11.15             2.10.15             2.9.15
      |     2.12.14             2.11.14             2.10.14             2.9.14
      |     2.12.13             2.11.13             2.10.13             2.9.13
      |     2.12.12             2.11.12             2.10.12             2.9.12
      |     2.12.11             2.11.11             2.10.11             2.9.11
      |     2.12.10             2.11.10             2.10.10             2.9.10
      |     2.12.9              2.11.9              2.10.9              2.9.9
      |     2.12.8              2.11.8              2.10.8              2.9.8
      |     2.12.7              2.11.7              2.10.7              2.9.7
      |     2.12.6              2.11.6              2.10.6              2.9.6
      |     2.12.5              2.11.5              2.10.5              2.9.5
      |     2.12.4              2.11.4              2.10.4              2.9.4
      |     2.12.3              2.11.3              2.10.3              2.9.3
      |     2.12.2              2.11.2              2.10.2              2.9.2
      |     2.12.1              2.11.1              2.10.1              2.9.1
      |     2.12.0              2.11.0              2.10.0              2.9.0
      |
      |================================================================================
      |+ - local version
      |* - installed
      |> - currently in use
      |================================================================================
    """