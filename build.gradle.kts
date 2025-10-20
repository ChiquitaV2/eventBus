plugins {
	kotlin("jvm") version "2.2.0"
	`maven-publish`
}

group = "wtf.owen"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(21)
}

publishing {
	repositories {
		maven {
			name = "quantumRespository"
			url = uri("https://maven.quantumdev.org/snapshots/")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
	publications {
		create<MavenPublication>("maven") {
			groupId = "wtf.owen"
			artifactId = "eventbus"
			version = version
			from(components["java"])
		}
	}
}