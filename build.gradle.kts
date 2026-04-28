import org.gradle.api.JavaVersion.VERSION_25

group = "de.rettichlp.therettingtonconcierge"
version = project.findProperty("trcVersion") as? String ?: "0.0.0"
println("> The Rettington Concierge Version: $version")

java {
    sourceCompatibility = VERSION_25
    targetCompatibility = VERSION_25

    withJavadocJar()
    withSourcesJar()
}

plugins {
    `java-library`
    `maven-publish`

    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    api("net.kyori:adventure-text-serializer-ansi:5.0.1")

    api("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    // https://mvnrepository.com/artifact/org.atteo.classindex/classindex
    api("org.atteo.classindex:classindex:3.13")
    annotationProcessor("org.atteo.classindex:classindex:3.13")

    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    // https://mvnrepository.com/artifact/com.google.inject/guice
    implementation("com.google.inject:guice:7.0.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.14.0")

    // https://mvnrepository.com/artifact/org.springframework/spring-webflux
    implementation("org.springframework:spring-webflux:7.0.7")

    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-mongodb
    implementation("org.springframework.data:spring-data-mongodb:5.0.5")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.21.2")

    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

runPaper.disablePluginJarDetection()

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        isFailOnError = false
    }

    jar {
        dependsOn("shadowJar")
    }
}

publishing {
    repositories {
        maven {
            name = "rettichlpRepositoryPublisher"
            url = uri("https://repo.rettichlp.de/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.rettichlp"
            artifactId = "therettingtonconcierge"
            version = project.version.toString()
            from(components["java"])
        }
    }
}
