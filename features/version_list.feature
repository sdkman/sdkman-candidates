Feature: Version List

  Scenario: A list of available uninstalled Versions are displayed
    Given the Candidate
      | candidate | name  | description        | default | websiteUrl                  | distribution   |
      | scala     | Scala | The Scala Language | 2.12.6  | https://www.scala-lang.org/ | UNIVERSAL      |
    And the Versions
      | candidate | version | platform  | url                                                           |
      | scala     | 2.11.0  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.0/scala-2.11.0.zip |
      | scala     | 2.11.1  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.1/scala-2.11.1.zip |
      | scala     | 2.11.2  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.2/scala-2.11.2.zip |
      | scala     | 2.11.3  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.3/scala-2.11.3.zip |
      | scala     | 2.11.4  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.4/scala-2.11.4.zip |
      | scala     | 2.11.5  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.5/scala-2.11.5.zip |
      | scala     | 2.11.6  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.6/scala-2.11.6.zip |
      | scala     | 2.11.7  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.7/scala-2.11.7.zip |
      | scala     | 2.11.8  | UNIVERSAL | https://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.zip |
      | scala     | 2.12.0  | UNIVERSAL | https://downloads.lightbend.com/scala/2.12.0/scala-2.12.0.zip |
      | scala     | 2.12.1  | UNIVERSAL | https://downloads.lightbend.com/scala/2.12.1/scala-2.12.1.zip |
      | scala     | 2.12.2  | UNIVERSAL | https://downloads.lightbend.com/scala/2.12.2/scala-2.12.2.zip |
      | scala     | 2.12.3  | UNIVERSAL | https://downloads.lightbend.com/scala/2.12.3/scala-2.12.3.zip |
      | scala     | 2.12.4  | UNIVERSAL | https://downloads.lightbend.com/scala/2.12.4/scala-2.12.4.zip |
      | scala     | 2.12.5  | UNIVERSAL | https://downloads.lightbend.com/scala/2.12.5/scala-2.12.5.zip |
      | scala     | 2.12.6  | UNIVERSAL | https://downloads.lightbend.com/scala/2.12.6/scala-2.12.6.zip |
    When a request is made to /candidates/scala/linux64/versions/list?current=2.12.6&installed=2.11.8,2.12.6
    Then a 200 status code is received
    And the response body is
    """
      |================================================================================
      |Available Scala Versions
      |================================================================================
      | > * 2.12.6               2.11.0
      |     2.12.5
      |     2.12.4
      |     2.12.3
      |     2.12.2
      |     2.12.1
      |     2.12.0
      |   * 2.11.8
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