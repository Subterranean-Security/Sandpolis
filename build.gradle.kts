//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//

buildscript {
	repositories {
		mavenCentral()
		jcenter()
	}
}

plugins {
	id("com.diffplug.spotless") version "5.10.0"
}

spotless {
	cpp {
		target("**/*.cc", "**/*.hh")

		// Exclude build directory
		targetExclude("**/build/**", "**/gen/main/cpp/**")

		eclipseCdt()
		endWithNewline()
		indentWithTabs()

		licenseHeaderFile(file("gradle/resources/header_cpp.txt"), "(\\#include|\\#ifndef)")
	}
	kotlin {
		target("**/*.kt")

		// Exclude build directory
		targetExclude("**/build/**")

		licenseHeaderFile(file("gradle/resources/header_gradle.txt"), "(plugins|import|package)")
	}
	kotlinGradle {
		target("**/*.kts")

		// Exclude build directory
		targetExclude("**/build/**")

		licenseHeaderFile(file("gradle/resources/header_gradle.txt"), "(plugins|import|buildscript|rootProject)")
	}
	java {
		target("**/*.java")

		// Exclude build directory and generated sources
		targetExclude("**/build/**", "**/gen/main/java/**", "**/src/main/java/com/sandpolis/core/instance/converter/**")

		eclipse().configFile("gradle/resources/EclipseConventions.xml")
		trimTrailingWhitespace()
		endWithNewline()

		licenseHeaderFile(file("gradle/resources/header_java.txt"), "package")
	}
	format("javaModules") {
		target("**/module-info.java")

		// Exclude build directory
		targetExclude("**/build/**")

		trimTrailingWhitespace()
		endWithNewline()

		licenseHeaderFile(file("gradle/resources/header_java.txt"), "(module|open module)")
	}
	format("proto") {
		target("**/*.proto")

		// Exclude build directory
		targetExclude("**/build/**")

		trimTrailingWhitespace()
		endWithNewline()
		indentWithSpaces()

		licenseHeaderFile(file("gradle/resources/header_java.txt"), "syntax")
	}
	format("swift") {
		target("**/*.swift")

		// Exclude build directory
		targetExclude("**/build/**")

		trimTrailingWhitespace()
		endWithNewline()
		indentWithTabs()

		licenseHeaderFile(file("gradle/resources/header_swift.txt"), "import")
	}
	format("css") {
		target("**/*.css")

		// Exclude build directory
		targetExclude("**/build/**")

		eclipseWtp(com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.CSS)
	}
	format("json") {
		target("**/*.json")

		// Exclude build directory and iOS projects
		targetExclude("**/build/**", "**/com.sandpolis.client.lockstone/**")

		eclipseWtp(com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.JSON)
	}
}

// Uncheckout buildSrc submodules and use root instead
subprojects {
	// TODO
}
