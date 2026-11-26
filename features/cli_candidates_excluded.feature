Feature: CLI Candidates Excluded

  Background:
    Given the Candidates
      | candidate | name      | description             | default | websiteUrl                  |
      | scala     | Scala     | The Scala Language      | 2.12.0  | http://www.scala-lang.org/  |
      | sdkcli    | Sdkcli    | The SDKMAN Bash CLI     | 5.24.0  | https://sdkman.io           |
      | groovy    | Groovy    | The Groovy Language     | 2.4.7   | http://www.groovy-lang.org/ |
      | sdkrs     | Sdkrs     | The SDKMAN Native CLI   | 0.8.0   | https://sdkman.io           |
      | micronaut | Micronaut | The Micronaut Framework |         | http://micronaut.io         |

  Scenario: The CLI candidates are absent from the candidate names
    When a request is made to /candidates/all
    Then a 200 status code is received
    And the response body is "scala,groovy,micronaut"

  Scenario: The CLI candidates have no section in the candidate list
    When a request is made to /candidates/list
    Then a 200 status code is received
    And the rendered text is:
    """
      |================================================================================
      |Available Candidates
      |================================================================================
      |q-quit                                  /-search down
      |j-down                                  ?-search up
      |k-up                                    h-help
      |
      |--------------------------------------------------------------------------------
      |Scala (2.12.0)                                        http://www.scala-lang.org/
      |
      |The Scala Language
      |
      |                                                             $ sdk install scala
      |--------------------------------------------------------------------------------
      |Groovy (2.4.7)                                       http://www.groovy-lang.org/
      |
      |The Groovy Language
      |
      |                                                            $ sdk install groovy
      |--------------------------------------------------------------------------------
      |Micronaut (Coming soon!)                                     http://micronaut.io
      |
      |The Micronaut Framework
      |
      |                                                         $ sdk install micronaut
      |--------------------------------------------------------------------------------
      |
    """

  Scenario: No Default Version is provided for a CLI candidate
    When a request is made to /default/sdkcli
    Then a 400 status code is received
    And the response body is ""
    When a request is made to /default/sdkrs
    Then a 400 status code is received
    And the response body is ""

  Scenario: No Versions are listed for a CLI candidate
    When a request is made to /candidates/sdkcli/linuxx64/versions/list
    Then a 404 status code is received
    And the State API received no requests for sdkcli
    When a request is made to /candidates/sdkrs/linuxx64/versions/list
    Then a 404 status code is received
    And the State API received no requests for sdkrs

  Scenario: Validation fails by name for a CLI candidate
    Given the Version on the remote service
      | candidate | version | vendor | platform  | url                                |
      | sdkcli    | 5.24.0  |        | UNIVERSAL | http://dl/sdkcli/5.24.0/sdkman.zip |
    And the Version on the remote service
      | candidate | version | vendor | platform  | url                                |
      | sdkrs     | 0.8.0   |        | LINUX_X64 | http://dl/sdkrs/0.8.0/sdkrs.tar.gz |
    When I attempt validation at endpoint /validate/sdkcli/5.24.0/linuxx64
    Then a 200 status code is received
    And the response body is "invalid"
    And the State API received no requests for sdkcli
    When I attempt validation at endpoint /validate/sdkrs/0.8.0/linuxx64
    Then a 200 status code is received
    And the response body is "invalid"
    And the State API received no requests for sdkrs
