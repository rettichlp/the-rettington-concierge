import org.gradle.api.JavaVersion.VERSION_25
import xyz.jpenilla.runpaper.task.RunServer

group = "de.rettichlp.therettingtonconcierge"
version = project.findProperty("trcVersion") as? String ?: "0.0.0"
println("> The-Rettington-Concierge-Version: $version")

java {
    sourceCompatibility = VERSION_25
    targetCompatibility = VERSION_25

    withJavadocJar()
    withSourcesJar()
}

plugins {
    `java-library`
    `maven-publish`

    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.3.1"
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    // https://mvnrepository.com/artifact/org.atteo.classindex/classindex
    api("org.atteo.classindex", "classindex", "3.13")
    annotationProcessor("org.atteo.classindex", "classindex", "3.13")
    testAnnotationProcessor("org.atteo.classindex", "classindex", "3.13")

    api("io.papermc.paper", "paper-api", "1.21.11-R0.1-SNAPSHOT")
    api("net.kyori", "adventure-text-serializer-ansi", "4.26.1")
    testCompileOnly("io.papermc.paper", "paper-api", "1.21.11-R0.1-SNAPSHOT")

    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok", "lombok", "1.18.42")
    annotationProcessor("org.projectlombok", "lombok", "1.18.42")
    testCompileOnly("org.projectlombok", "lombok", "1.18.42")
    testAnnotationProcessor("org.projectlombok", "lombok", "1.18.42")

    // https://mvnrepository.com/artifact/com.google.inject/guice
    implementation("com.google.inject", "guice", "7.0.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson", "gson", "2.13.2")

    // https://mvnrepository.com/artifact/org.springframework/spring-webflux
    implementation("org.springframework", "spring-webflux", "7.0.4")

    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-mongodb
    implementation("org.springframework.data", "spring-data-mongodb", "5.0.3")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml
    implementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", "2.21.0")

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

    register<Jar>("createTestRunPaperJar") {
        mustRunAfter(shadowJar)

        // Include everything from the shadowJar
        from({ zipTree(shadowJar.get().archiveFile.get().asFile) })

        // Add test sources and resources
        from(sourceSets.test.get().output)
        from(sourceSets.test.get().resources)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        doLast {
            println("> Test Run-Paper: Build custom test run jar")
        }

        group = "Custom Build Tasks"
    }

    // Task to copy the final JAR to run/plugins
    register<Copy>("copyTestRunPaperJar") {
        dependsOn("createTestRunPaperJar")

        from(named<Jar>("createTestRunPaperJar").flatMap { it.archiveFile })
        into(layout.projectDirectory.dir("run/plugins")) // Copy to run/plugins directory

        doLast {
            println("> Test Run-Paper: Copied custom-test-run-paper.jar to run/plugins/")
        }

        group = "Custom Build Tasks"
    }

    withType<RunServer> {
        dependsOn(shadowJar)
        dependsOn("createTestRunPaperJar")
        dependsOn("copyTestRunPaperJar")
    }

    test {
        useJUnitPlatform()
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
