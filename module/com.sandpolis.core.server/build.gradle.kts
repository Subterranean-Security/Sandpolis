//============================================================================//
//                                                                            //
//                Copyright © 2015 - 2020 Subterranean Security               //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation at:                                //
//                                                                            //
//    https://mozilla.org/MPL/2.0                                             //
//                                                                            //
//=========================================================S A N D P O L I S==//

plugins {
	id("java-library")
	id("sandpolis-java")
	id("sandpolis-module")
	id("sandpolis-publish")
	id("com.sandpolis.gradle.codegen")
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.1")

	api(project(":module:com.sandpolis.core.clientserver"))
	api(project(":module:com.sandpolis.core.instance"))
	api(project(":module:com.sandpolis.core.net"))
	api(project(":module:com.sandpolis.core.serveragent"))
	
	// https://github.com/FasterXML/jackson-databind
	implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")

	// https://github.com/netty/netty
	implementation("io.netty:netty-codec:4.1.48.Final")
	implementation("io.netty:netty-common:4.1.48.Final")
	implementation("io.netty:netty-handler:4.1.48.Final")
	implementation("io.netty:netty-transport:4.1.48.Final")

	// https://github.com/javaee/jpa-spec
	implementation("javax.persistence:javax.persistence-api:2.2")

	// https://github.com/hibernate/hibernate-ogm
	implementation("org.hibernate.ogm:hibernate-ogm-mongodb:5.4.1.Final")

	// https://github.com/cilki/zipset
	implementation("com.github.cilki:zipset:1.2.1")

	// https://github.com/jchambers/java-otp
	//implementation("com.eatthepath:java-otp:0.2.0")

	implementation("javax.xml.bind:jaxb-api:2.3.0")
}

sourceSets {
	main {
		java {
			srcDirs("gen/main/java")
		}
	}
}
