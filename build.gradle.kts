import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    `java-library`
    `maven-publish`
    id("net.researchgate.release") version "2.8.1"
    id("org.cadixdev.licenser") version "0.5.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven {
        name = "WPI"
        url = uri("https://frcmaven.wpi.edu/artifactory/release")
    }
}

dependencies {
    api("com.techshroom:greenish-jungle:0.0.3")
	api("org.slf4j:slf4j-api:1.7.25")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    val wpiVersion = "2020.3.2"
    api("edu.wpi.first.wpilibj:wpilibj-java:$wpiVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpiVersion")
    implementation("edu.wpi.first.hal:hal-java:$wpiVersion")
}

configure<LicenseExtension> {
    header = rootProject.file("HEADER.txt")
    (this as ExtensionAware).extra.apply {
        set("name", rootProject.name)
        for (key in listOf("organization", "url")) {
            set(key, rootProject.property(key))
        }
    }
}

release {
    tagTemplate = "v\${version}"
    buildTasks = listOf<String>("build")
}

publishing {
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = "https://maven.octyl.net/repository/team5818-releases"
            val snapshotsRepoUrl = "https://maven.octyl.net/repository/team5818-snapshots"
            name = "octylNet"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials(PasswordCredentials::class)
        }
    }
}
