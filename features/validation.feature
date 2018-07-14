Feature: Candidate Version Validation by Platform

	Background:
      Given the Versions
        | candidate | version | platform   | url                                             |
        | java      | 8u111   | MAC_OSX    | http://dl/8u111-b14/jdk-8u111-macosx-x64.dmg    |
        | java      | 8u111   | LINUX_32   | http://dl/8u111-b14/jdk-8u111-linux-i386.tar.gz |
        | java      | 8u111   | LINUX_64   | http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz  |
        | java      | 8u111   | WINDOWS_64 | http://dl/8u111-b14/jdk-8u111-windows-x64.exe   |
        | scala     | 2.12.0  | UNIVERSAL  | http://dl/scala/2.12.0/scala-2.12.0.zip         |

	Scenario: Validation succeeds for a multi-platform binary on Linux 32 bit platform
		When I attempt validation at endpoint /validate/java/8u111/linux32
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 64 bit platform
		When I attempt validation at endpoint /validate/java/8u111/linux64
		Then a 200 status code is received
		And the response body is "valid"
		When I attempt validation at endpoint /validate/java/8u111/linux
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Mac OSX platform
		When I attempt validation at endpoint /validate/java/8u111/darwin
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Cygwin platform
		When I attempt validation at endpoint /validate/java/8u111/CYGWIN_NT-6.3
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on MinGW platform
		When I attempt validation at endpoint /validate/java/8u111/MINGW64_NT-10.0
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation fails for a multi-platform binary on an unsupported platform
		When I attempt validation at endpoint /validate/java/8u111/freebsd
		Then a 200 status code is received
		And the response body is "invalid"

	Scenario: Validation fails for a multi-platform binary on an unknown platform
		When I attempt validation at endpoint /validate/java/8u111/commodore64
		Then a 200 status code is received
		And the response body is "invalid"

	Scenario: Validation succeeds for a universal binary on a know platform
		When I attempt validation at endpoint /validate/scala/2.12.0/linux
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation fails for a universal binary on an unknown platform
		When I attempt validation at endpoint /validate/scala/2.12.0/commodore64
		Then a 200 status code is received
		And the response body is "invalid"