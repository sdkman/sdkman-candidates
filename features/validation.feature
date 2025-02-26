Feature: Candidate Version Validation by Platform

	Scenario: Validation succeeds for a multi-platform binary on Linux 32 bit platform
		Given the Version on the remote service
			| candidate | version | vendor | platform | url                                             |
			| java      | 8u111   | open   | LINUX_32 | http://dl/8u111-b14/jdk-8u111-linux-i386.tar.gz |

		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/linuxx32
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 64 bit platform
		Given the Version on the remote service
			| candidate | version | vendor | platform | url                                          |
			| java      | 8u111   | open   | LINUX_64 | http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz |

		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/linuxx64
		Then a 200 status code is received
		And the response body is "valid"
		When I attempt validation at endpoint /validate/java/8u111-open/linux
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 32 bit ARM soft float platform
		Given the Version on the remote service
			| candidate | version | vendor | platform      | url                                                 |
			| java      | 8u111   | open   | LINUX_ARM32SF | http://dl/8u111-b14/jdk-8u111-linux-arm386sf.tar.gz |
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/linuxarm32sf
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 32 bit ARM hard float platform
		Given the Version on the remote service
			| candidate | version | vendor | platform      | url                                                 |
			| java      | 8u111   | open   | LINUX_ARM32HF | http://dl/8u111-b14/jdk-8u111-linux-arm386hf.tar.gz |
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/linuxarm32hf
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Linux 64 bit ARM platform
		Given the Version on the remote service
			| candidate | version | vendor | platform    | url                                              |
			| java      | 8u111   | open   | LINUX_ARM64 | http://dl/8u111-b14/jdk-8u111-linux-arm64.tar.gz |
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/linuxarm64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Mac OSX X86 platform
		Given the Version on the remote service
			| candidate | version | vendor | platform | url                                          |
			| java      | 8u111   | open   | MAC_OSX  | http://dl/8u111-b14/jdk-8u111-macosx-x64.dmg |
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/darwinx64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Mac OSX M1 ARM platform
		Given the Version on the remote service
			| candidate | version | vendor | platform  | url                                           |
			| java      | 8u111   | open   | MAC_ARM64 | http://dl/8u111-b14/jdk-8u111-macos-m1-x64.dmg |
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/darwinarm64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a multi-platform binary on Cygwin platform
		Given the Version on the remote service
			| candidate | version | vendor | platform   | url                                           |
			| java      | 8u111   | open   | WINDOWS_64 | http://dl/8u111-b14/jdk-8u111-windows-x64.exe |
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/windowsx64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation fails for a multi-platform binary on an unsupported platform
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/freebdd
		Then a 200 status code is received
		And the response body is "invalid"

	Scenario: Validation fails for a multi-platform binary on an unknown platform
		And no Version for java 8u111 open of platform UNIVERSAL on the remote service
		And no Version for java 8u111 open of platform EXOTIC on the remote service
		When I attempt validation at endpoint /validate/java/8u111-open/exotic
		Then a 200 status code is received
		And the response body is "invalid"

	Scenario: Validation succeeds for a universal binary on a know platform
		Given the Version on the remote service
			| candidate | version | vendor | platform  | url                                     |
			| scala     | 2.12.0  |        | UNIVERSAL | http://dl/scala/2.12.0/scala-2.12.0.zip |
		And no Version for scala 2.12.0  of platform LINUX_64 on the remote service
		When I attempt validation at endpoint /validate/scala/2.12.0/linuxx64
		Then a 200 status code is received
		And the response body is "valid"

	Scenario: Validation succeeds for a universal binary on an unknown platform
		Given the Version on the remote service
			| candidate | version | vendor | platform  | url                                     |
			| scala     | 2.12.0  |        | UNIVERSAL | http://dl/scala/2.12.0/scala-2.12.0.zip |
		When I attempt validation at endpoint /validate/scala/2.12.0/exotic
		Then a 200 status code is received
		And the response body is "valid"