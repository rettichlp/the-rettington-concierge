println("> The Rettington Concierge Version: $version")

plugins {
    id("java-library")
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    api("com.velocitypowered:velocity-api:4.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:4.1.1")

    // https://mvnrepository.com/artifact/org.atteo.classindex/classindex
    api("org.atteo.classindex:classindex:3.13")
    annotationProcessor("org.atteo.classindex:classindex:3.13")

    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    // https://mvnrepository.com/artifact/com.google.inject/guice
    implementation("com.google.inject:guice:7.0.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.14.0")

    // Source: https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.22.2")

    // https://mvnrepository.com/artifact/org.springframework/spring-webflux
    implementation("org.springframework:spring-webflux:7.0.9")

    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-mongodb
    implementation("org.springframework.data:spring-data-mongodb:5.1.1")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
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
