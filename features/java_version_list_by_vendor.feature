Feature: Java Version List by Vendor

  Background:
    Given the Candidate
      | candidate | name  | description        | default      | websiteUrl                  | distribution       |
      | java      | Java  | The Java  Language | 11.0.3-open  | https://adoptopenjdk.net/   | PLATFORM_SPECIFIC  |

  Scenario: List all Java Versions
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 12.0.1.j9-adpt   | adpt   | LINUX_64   | http://adopt.example.org/jdk-12.0.1.j9.tar.gz  |
      | java      | 12.0.1.hs-adpt   | adpt   | LINUX_64   | http://adopt.example.org/jdk-12.0.1.hs.tar.gz  |
      | java      | 11.0.3.j9-adpt   | adpt   | LINUX_64   | http://adopt.example.org/jdk-11.0.3.j9.tar.gz  |
      | java      | 11.0.3.hs-adpt   | adpt   | LINUX_64   | http://adopt.example.org/jdk-11.0.3.hs.tar.gz  |
      | java      | 8.0.212.j9-adpt  | adpt   | LINUX_64   | http://adopt.example.org/jdk-8.0.212.j9.tar.gz |
      | java      | 8.0.212.hs-adpt  | adpt   | LINUX_64   | http://adopt.example.org/jdk-8.0.212.hs.tar.gz |
      | java      | 11.0.3-amzn      | amzn   | LINUX_64   | http://amzn.example.org/jdk-11.0.3.j9.tar.gz   |
      | java      | 8.0.212-amzn     | amzn   | LINUX_64   | http://amzn.example.org/jdk-8.0.212.tar.gz     |
      | java      | 19.0.0-grl       | grl    | LINUX_64   | http://graal.example.org/graal-19.0.0.tar.gz   |
      | java      | 1.0.0-rc-16-grl  | grl    | LINUX_64   | http://graal.example.org/grl-1.0.0-rc16.tar.gz |
      | java      | 13.ea.20-open    | open   | LINUX_64   | http://open.example.org/jdk-13.ea.20.tar.gz    |
      | java      | 12.0.1-open      | open   | LINUX_64   | http://open.example.org/jdk-12.0.1.tar.gz      |
      | java      | 11.0.3-open      | open   | LINUX_64   | http://open.example.org/jdk-11.0.3.tar.gz      |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |
      | java      | 9.0.4-open       | open   | LINUX_64   | http://open.example.org/jdk-9.0.4.tar.gz       |
      | java      | 12.0.1-zulu      | zulu   | LINUX_64   | http://zulu.example.org/jdk-12.0.1.tar.gz      |
      | java      | 11.0.3-zulu      | zulu   | LINUX_64   | http://zulu.example.org/jdk-11.0.3.tar.gz      |
      | java      | 10.0.2-zulu      | zulu   | LINUX_64   | http://zulu.example.org/jdk-10.0.2.tar.gz      |
      | java      | 9.0.4-zulu       | zulu   | LINUX_64   | http://zulu.example.org/jdk-9.0.4.tar.gz       |
      | java      | 8.0.212-zulu     | zulu   | LINUX_64   | http://zulu.example.org/jdk-8.0.212.tar.gz     |
      | java      | 7.0.222-zulu     | zulu   | LINUX_64   | http://zulu.example.org/jdk-7.0.222.tar.gz     |
      | java      | 6.0.119-zulu     | zulu   | LINUX_64   | http://zulu.example.org/jdk-6.0.119.tar.gz     |

    And the current Version is 11.0.3.j9-adpt
    And the installed Versions 8.0.202-zulu,11.0.3.j9-adpt,12.0.1-zulu,13.ea.20-open,11.0.3-local
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | AdoptOpenJDK  |     | 12.0.1.j9    | adpt    |            | 12.0.1.j9-adpt
    |               |     | 12.0.1.hs    | adpt    |            | 12.0.1.hs-adpt
    |               | >>> | 11.0.3.j9    | adpt    | installed  | 11.0.3.j9-adpt
    |               |     | 11.0.3.hs    | adpt    |            | 11.0.3.hs-adpt
    |               |     | 8.0.212.j9   | adpt    |            | 8.0.212.j9-adpt
    |               |     | 8.0.212.hs   | adpt    |            | 8.0.212.hs-adpt
    | Amazon        |     | 11.0.3       | amzn    |            | 11.0.3-amzn
    |               |     | 8.0.212      | amzn    |            | 8.0.212-amzn
    | Azul Zulu     |     | 12.0.1       | zulu    | installed  | 12.0.1-zulu
    |               |     | 11.0.3       | zulu    |            | 11.0.3-zulu
    |               |     | 10.0.2       | zulu    |            | 10.0.2-zulu
    |               |     | 9.0.4        | zulu    |            | 9.0.4-zulu
    |               |     | 8.0.212      | zulu    |            | 8.0.212-zulu
    |               |     | 8.0.202      | zulu    | local only | 8.0.202-zulu
    |               |     | 7.0.222      | zulu    |            | 7.0.222-zulu
    |               |     | 6.0.119      | zulu    |            | 6.0.119-zulu
    | GraalVM       |     | 19.0.0       | grl     |            | 19.0.0-grl
    |               |     | 1.0.0        | grl     |            | 1.0.0-rc-16-grl
    | Java.net      |     | 13.ea.20     | open    | installed  | 13.ea.20-open
    |               |     | 12.0.1       | open    |            | 12.0.1-open
    |               |     | 11.0.3       | open    |            | 11.0.3-open
    |               |     | 10.0.2       | open    |            | 10.0.2-open
    |               |     | 9.0.4        | open    |            | 9.0.4-open
    | Unclassified  |     | 11.0.3       | none    | local only | 11.0.3-local
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: An orphaned local version is displayed
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |

    And the installed Versions 10.0.2-open,10.0.1-open
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 10.0.2       | open    | installed  | 10.0.2-open
    |               |     | 10.0.1       | open    | local only | 10.0.1-open
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: An unclassified local version is displayed
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |

    And the installed Versions 10.0.2-open,10.0.1-local
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 10.0.2       | open    | installed  | 10.0.2-open
    | Unclassified  |     | 10.0.1       | none    | local only | 10.0.1-local
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: An unknown versions is omitted
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |

    And the installed Versions 10.0.2-open,10.0.1-oracle
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 10.0.2       | open    | installed  | 10.0.2-open
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: No local or orphaned versions are displayed
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |

    And the installed Versions 10.0.2-open
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 10.0.2       | open    | installed  | 10.0.2-open
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: Only local versions are displayed
    And the installed Versions 10.0.2-local
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Unclassified  |     | 10.0.2       | none    | local only | 10.0.2-local
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """

  Scenario: No local or remote versions are displayed
    When a request is made to /candidates/java/linux64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    |================================================================================
    |Use the Identifier for installation:
    |
    |    $ sdk install java 11.0.3.hs-adpt
    |================================================================================
    """
