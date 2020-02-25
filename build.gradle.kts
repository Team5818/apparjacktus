import com.techshroom.inciseblue.commonLib

plugins {
    `java-library`
    id("com.techshroom.incise-blue") version "0.5.7"
    id("net.researchgate.release") version "2.8.1"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
    signing
}

inciseBlue {
    ide()
    license()
    util {
        javaVersion = JavaVersion.VERSION_11
    }
}

repositories {
    maven {
        name = "WPI Maven"
        url = uri("https://frcmaven.wpi.edu/artifactory/release")
    }
}

dependencies {
    api("com.techshroom:greenish-jungle:0.0.3")
	api("org.slf4j:slf4j-api:1.7.25")
	commonLib("ch.qos.logback", "logback", "1.2.3") {
        api(lib("classic"))
        api(lib("core"))
	}
    val wpiVersion = "2020.3.2"
    api("edu.wpi.first.wpilibj:wpilibj-java:$wpiVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpiVersion")
    implementation("edu.wpi.first.hal:hal-java:$wpiVersion")
}

release {
    tagTemplate = "v\${version}"
    buildTasks = listOf<String>()
}

java.withJavadocJar()
java.withSourcesJar()

publishing {
    publications {
        register<MavenPublication>("library") {
            pom {
                name.set("apparjacktus")
            }
            groupId = "org.rivierarobotics.apparjacktus"
            artifactId = "apparjacktus"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER") ?: findProperty("bintray.user")?.toString()
    key = System.getenv("BINTRAY_KEY") ?: findProperty("bintray.password")?.toString()
    setPublications("library")
    with(pkg) {
        repo = "maven-release"
        name = "apparjacktus"
        userOrg = "team5818"
        vcsUrl = "https://github.com/Team5818/apparjacktus.git"
        publish = true
        with(version) {
            name = project.version.toString()
        }
    }
}

project.configure<SigningExtension> {
    // Only sign if it's possible.
    if (this.signatories.getDefaultSignatory(project) != null) {
        sign(project.publishing.publications.getByName("library"))
    }
}
