Feature: Candidate Version Validation by Platform

	Background:
      Given the Versions
        | candidate | version | vendor | platform      | url                                                 |
        | java      | 8u111   | open   | MAC_OSX       | http://dl/8u111-b14/jdk-8u111-macosx-x64.dmg        |
        | java      | 8u111   | open   | MAC_ARM64     | http://dl/8u111-b14/jdk-8u111-macos-silicon-x64.dmg |
        | java      | 8u111   | open   | LINUX_32      | http://dl/8u111-b14/jdk-8u111-linux-i386.tar.gz     |
        | java      | 8u111   | open   | LINUX_64      | http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz      |
        | java      | 8u111   | open   | LINUX_ARM32SF | http://dl/8u111-b14/jdk-8u111-linux-arm386sf.tar.gz |
        | java      | 8u111   | open   | LINUX_ARM32HF | http://dl/8u111-b14/jdk-8u111-linux-arm386hf.tar.gz |
        | java      | 8u111   | open   | LINUX_ARM64   | http://dl/8u111-b14/jdk-8u111-linux-arm64.tar.gz    |
        | java      | 8u111   | open   | WINDOWS_64    | http://dl/8u111-b14/jdk-8u111-windows-x64.exe       |
        | scala     | 2.12.0  | open   | UNIVERSAL     | http://dl/scala/2.12.0/scala-2.12.0.zip             |

	Scenario: Validation succeeds for a multi-platform binary on Linux 32 bit platform
		When I attempt validation at endpoint /validate/java/8u111/linuxx32
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 64 bit platform
		When I attempt validation at endpoint /validate/java/8u111/linuxx64
		Then a 200 status code is received
		And the response body is "valid"
		When I attempt validation at endpoint /validate/java/8u111/linux
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 32 bit ARM soft float platform
		When I attempt validation at endpoint /validate/java/8u111/linuxarm32sf
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 32 bit ARM hard float platform
		When I attempt validation at endpoint /validate/java/8u111/linuxarm32hf
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 64 bit ARM platform
		When I attempt validation at endpoint /validate/java/8u111/linuxarm64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Mac OSX X86 platform
		When I attempt validation at endpoint /validate/java/8u111/darwinx64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Mac OSX Silicon ARM platform
		When I attempt validation at endpoint /validate/java/8u111/darwinarm64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Cygwin platform
		When I attempt validation at endpoint /validate/java/8u111/windowsx64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation fails for a multi-platform binary on an unsupported platform
		When I attempt validation at endpoint /validate/java/8u111/freebdd
		Then a 200 status code is received
		And the response body is "invalid"

	Scenario: Validation fails for a multi-platform binary on an unknown platform
		When I attempt validation at endpoint /validate/java/8u111/exotic
		Then a 200 status code is received
		And the response body is "invalid"

	Scenario: Validation succeeds for a universal binary on a know platform
		When I attempt validation at endpoint /validate/scala/2.12.0/linuxx64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a universal binary on an unknown platform
		When I attempt validation at endpoint /validate/scala/2.12.0/exotic
		Then a 200 status code is received
		And the response body is "valid"