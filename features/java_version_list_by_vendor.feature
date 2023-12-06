Feature: Java Version List by Vendor

  Background:
    Given the Candidate
      | candidate | name  | description        | default      | websiteUrl           | distribution       |
      | java      | Java  | The Java Language  | 17.0.0-tem   | https://adoptium.net | PLATFORM_SPECIFIC  |

  Scenario: List all Java Versions
    Given the Versions
      | candidate | version          | vendor  | platform   | url                                                |
      | java      | 12.0.1.j9-adpt   | adpt    | LINUX_64   | http://adopt.example.org/jdk-12.0.1.j9.tar.gz      |
      | java      | 12.0.1.hs-adpt   | adpt    | LINUX_64   | http://adopt.example.org/jdk-12.0.1.hs.tar.gz      |
      | java      | 11.0.3.j9-adpt   | adpt    | LINUX_64   | http://adopt.example.org/jdk-11.0.3.j9.tar.gz      |
      | java      | 11.0.3.hs-adpt   | adpt    | LINUX_64   | http://adopt.example.org/jdk-11.0.3.hs.tar.gz      |
      | java      | 8.0.212.j9-adpt  | adpt    | LINUX_64   | http://adopt.example.org/jdk-8.0.212.j9.tar.gz     |
      | java      | 8.0.212.hs-adpt  | adpt    | LINUX_64   | http://adopt.example.org/jdk-8.0.212.hs.tar.gz     |
      | java      | 11.0.3-albba     | albba   | LINUX_64   | http://albba.example.org/jdk-11.0.3.j9.tar.gz      |
      | java      | 8.0.212-albba    | albba   | LINUX_64   | http://albba.example.org/jdk-8.0.212.tar.gz        |
      | java      | 11.0.3-amzn      | amzn    | LINUX_64   | http://amzn.example.org/jdk-11.0.3.j9.tar.gz       |
      | java      | 8.0.212-amzn     | amzn    | LINUX_64   | http://amzn.example.org/jdk-8.0.212.tar.gz         |
      | java      | 17.0.9-bisheng   | bisheng | LINUX_64   | http://bisheng.example.org/jdk-17.0.9.tar.gz       |
      | java      | 11.0.21-bisheng  | bisheng | LINUX_64   | http://bisheng.example.org/jdk-11.0.21.tar.gz      |
      | java      | 8.0.392-bisheng  | bisheng | LINUX_64   | http://bisheng.example.org/jdk-8.0.392.tar.gz      |
      | java      | 19.0.0-gln       | gln     | LINUX_64   | http://graal.example.org/graal-19.0.0.tar.gz       |
      | java      | 17.0.7-graal     | graal   | LINUX_64   | http://graal.example.org/graal-17.0.7.tar.gz       |
      | java      | 17.0.7-graalce   | graalce | LINUX_64   | http://graal.example.org/graal-ce-17.0.7.tar.gz    |
      | java      | 11.0.8-jbr       | jbr     | LINUX_64   | http://jbr.example.org/jbr-11.0.8.tar.gz           |
      | java      | 17.0.7-jbr       | jbr     | LINUX_64   | http://jbr.example.org/jbr-17.0.7.tar.gz           |
      | java      | 20.1.0.1-mandrel | mandrel | LINUX_64   | http://mandrel.example.org/mandrel-20.1.0.1.tar.gz |
      | java      | 11.0.9-ms        | ms      | LINUX_64   | http://ms.example.org/ms-11.0.9.tar.gz             |
      | java      | 19.0.0-nik       | nik     | LINUX_64   | http://nik.example.org/nik-19.0.0.tar.gz           |
      | java      | 13.ea.20-open    | open    | LINUX_64   | http://open.example.org/jdk-13.ea.20.tar.gz        |
      | java      | 12.0.1-open      | open    | LINUX_64   | http://open.example.org/jdk-12.0.1.tar.gz          |
      | java      | 11.0.3-open      | open    | LINUX_64   | http://open.example.org/jdk-11.0.3.tar.gz          |
      | java      | 10.0.2-open      | open    | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz          |
      | java      | 9.0.4-open       | open    | LINUX_64   | http://open.example.org/jdk-9.0.4.tar.gz           |
      | java      | 11.0.9-oracle    | oracle  | LINUX_64   | http://oracle.example.org/oracle-11.0.9.tar.gz     |
      | java      | 8.0.212-sem      | sem     | LINUX_64   | http://sem.example.org/sem-8.0.212.tar.gz          |
      | java      | 8.0.212-tem      | tem     | LINUX_64   | http://tem.example.org/tem-8.0.212.tar.gz          |
      | java      | 17.0.5-kona      | kona    | LINUX_64   | http://kona.example.org/jdk-17.0.5.tar.gz          |
      | java      | 11.0.17-kona     | kona    | LINUX_64   | http://kona.example.org/jdk-11.0.17.tar.gz         |
      | java      | 8.0.352-kona     | kona    | LINUX_64   | http://kona.example.org/jdk-8.0.352.tar.gz         |
      | java      | 11.0.9-trava     | trava   | LINUX_64   | http://trava.example.org/trava-11.0.9.tar.gz       |
      | java      | 12.0.1-zulu      | zulu    | LINUX_64   | http://zulu.example.org/jdk-12.0.1.tar.gz          |
      | java      | 11.0.3-zulu      | zulu    | LINUX_64   | http://zulu.example.org/jdk-11.0.3.tar.gz          |
      | java      | 8.0.212-zulu     | zulu    | LINUX_64   | http://zulu.example.org/jdk-8.0.212.tar.gz         |

    And the current Version is 11.0.3.j9-adpt
    And the installed Versions 8.0.202-zulu,11.0.3.j9-adpt,12.0.1-zulu,13.ea.20-open,11.0.3-local
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | AdoptOpenJDK  |     | 12.0.1.j9    | adpt    |            | 12.0.1.j9-adpt
    |               |     | 12.0.1.hs    | adpt    |            | 12.0.1.hs-adpt
    |               | >>> | 11.0.3.j9    | adpt    | installed  | 11.0.3.j9-adpt
    |               |     | 11.0.3.hs    | adpt    |            | 11.0.3.hs-adpt
    |               |     | 8.0.212.j9   | adpt    |            | 8.0.212.j9-adpt
    |               |     | 8.0.212.hs   | adpt    |            | 8.0.212.hs-adpt
    | Corretto      |     | 11.0.3       | amzn    |            | 11.0.3-amzn
    |               |     | 8.0.212      | amzn    |            | 8.0.212-amzn
    | Dragonwell    |     | 11.0.3       | albba   |            | 11.0.3-albba
    |               |     | 8.0.212      | albba   |            | 8.0.212-albba
    | Gluon         |     | 19.0.0       | gln     |            | 19.0.0-gln
    | GraalVM CE    |     | 17.0.7       | graalce |            | 17.0.7-graalce
    | GraalVM Oracle|     | 17.0.7       | graal   |            | 17.0.7-graal
    | Huawei        |     | 17.0.9       | bisheng |            | 17.0.9-bisheng
    |               |     | 11.0.21      | bisheng |            | 11.0.21-bisheng
    |               |     | 8.0.392      | bisheng |            | 8.0.392-bisheng
    | Java.net      |     | 13.ea.20     | open    | installed  | 13.ea.20-open
    |               |     | 12.0.1       | open    |            | 12.0.1-open
    |               |     | 11.0.3       | open    |            | 11.0.3-open
    |               |     | 10.0.2       | open    |            | 10.0.2-open
    |               |     | 9.0.4        | open    |            | 9.0.4-open
    | JetBrains     |     | 17.0.7       | jbr     |            | 17.0.7-jbr
    |               |     | 11.0.8       | jbr     |            | 11.0.8-jbr
    | Liberica NIK  |     | 19.0.0       | nik     |            | 19.0.0-nik
    | Mandrel       |     | 20.1.0.1     | mandrel |            | 20.1.0.1-mandrel
    | Microsoft     |     | 11.0.9       | ms      |            | 11.0.9-ms
    | Oracle        |     | 11.0.9       | oracle  |            | 11.0.9-oracle
    | Semeru        |     | 8.0.212      | sem     |            | 8.0.212-sem
    | Temurin       |     | 8.0.212      | tem     |            | 8.0.212-tem
    | Tencent       |     | 17.0.5       | kona    |            | 17.0.5-kona
    |               |     | 11.0.17      | kona    |            | 11.0.17-kona
    |               |     | 8.0.352      | kona    |            | 8.0.352-kona
    | Trava         |     | 11.0.9       | trava   |            | 11.0.9-trava
    | Zulu          |     | 12.0.1       | zulu    | installed  | 12.0.1-zulu
    |               |     | 11.0.3       | zulu    |            | 11.0.3-zulu
    |               |     | 8.0.212      | zulu    |            | 8.0.212-zulu
    |               |     | 8.0.202      | zulu    | local only | 8.0.202-zulu
    | Unclassified  |     | 11.0.3       | none    | local only | 11.0.3-local
    |================================================================================
    |Omit Identifier to install default version 17.0.0-tem:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 17.0.0-tem
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: An orphaned local version is displayed
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |

    And the installed Versions 10.0.2-open,10.0.1-open
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 10.0.2       | open    | installed  | 10.0.2-open
    |               |     | 10.0.1       | open    | local only | 10.0.1-open
    |================================================================================
    |Omit Identifier to install default version 17.0.0-tem:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 17.0.0-tem
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: An unclassified local version is displayed
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |

    And the installed Versions 10.0.2-open,10.0.1-local,8.0.212-vendor,8.0.212-xyz
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 10.0.2       | open    | installed  | 10.0.2-open
    | Unclassified  |     | 10.0.1       | none    | local only | 10.0.1-local
    |               |     | 8.0.212      | none    | local only | 8.0.212-xyz
    |               |     | 8.0.212      | none    | local only | 8.0.212-vendor
    |================================================================================
    |Omit Identifier to install default version 17.0.0-tem:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 17.0.0-tem
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: No local or orphaned versions are displayed
    Given the Versions
      | candidate | version          | vendor | platform   | url                                            |
      | java      | 10.0.2-open      | open   | LINUX_64   | http://open.example.org/jdk-10.0.2.tar.gz      |

    And the installed Versions 10.0.2-open
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Java.net      |     | 10.0.2       | open    | installed  | 10.0.2-open
    |================================================================================
    |Omit Identifier to install default version 17.0.0-tem:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 17.0.0-tem
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: Only local versions are displayed
    And the installed Versions 10.0.2-vendor
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    | Unclassified  |     | 10.0.2       | none    | local only | 10.0.2-vendor
    |================================================================================
    |Omit Identifier to install default version 17.0.0-tem:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 17.0.0-tem
    |Hit Q to exit this list view
    |================================================================================
    """

  Scenario: No local or remote versions are displayed
    When a request is made to /candidates/java/linuxx64/versions/list
    Then a 200 status code is received
    And the response body is
    """
    |================================================================================
    |Available Java Versions for Linux 64bit
    |================================================================================
    | Vendor        | Use | Version      | Dist    | Status     | Identifier
    |--------------------------------------------------------------------------------
    |No versions available for your platform at this time.
    |================================================================================
    |Omit Identifier to install default version 17.0.0-tem:
    |    $ sdk install java
    |Use TAB completion to discover available versions
    |    $ sdk install java [TAB]
    |Or install a specific version by Identifier:
    |    $ sdk install java 17.0.0-tem
    |Hit Q to exit this list view
    |================================================================================
    """
