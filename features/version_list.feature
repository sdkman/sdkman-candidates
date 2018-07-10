Feature: Version List

  Background:
    Given the Candidate
      | candidate | name  | description        | default | websiteUrl                  | distribution |
      | scala     | Scala | The Scala Language | 2.12.6  | https://www.scala-lang.org/ | UNIVERSAL    |

  Scenario: A single column list of available uninstalled Versions are displayed
    Given the scala Versions 2.11.1 thru 2.11.8
    And the scala Versions 2.12.0 thru 2.12.6
    When a request is made to /candidates/scala/linux64/versions/list
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
    Given the scala Versions 2.9.0 thru 2.9.14
    And the scala Versions 2.10.0 thru 2.10.14
    And the scala Versions 2.11.0 thru 2.11.14
    And the scala Versions 2.12.0 thru 2.12.14
    When a request is made to /candidates/scala/linux64/versions/list
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
    Given the scala Versions 2.9.0 thru 2.9.14
    And the scala Versions 2.10.0 thru 2.10.14
    And the scala Versions 2.11.0 thru 2.11.14
    And the scala Versions 2.12.0 thru 2.12.14
    And the scala Version 2.12.14 is installed
    And the scala Version 2.12.14 is set as current
    When a request is made to /candidates/scala/linux64/versions/list
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

  Scenario: Installed versions are displayed
    Given the scala Versions 2.9.0 thru 2.9.14
    And the scala Versions 2.10.0 thru 2.10.14
    And the scala Versions 2.11.0 thru 2.11.14
    And the scala Versions 2.12.0 thru 2.12.14
    And the scala Versions 2.12.14,2.12.13,2.12.12,2.11.14,2.10.14,2.9.14 are installed
    When a request is made to /candidates/scala/linux64/versions/list
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

  Scenario: Local versions are displayed
    Given the scala Versions 2.12.0 thru 2.12.13
    And the scala Versions 2.12.14,2.12.13,2.12.12,2.11.14,2.10.14,2.9.14 are installed
    When a request is made to /candidates/scala/linux64/versions/list
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