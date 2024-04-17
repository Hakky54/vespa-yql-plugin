import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "org.pehrs"
version = "1.0.3"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    // FIXME: The documentapi libs do not work with Intellij
    // The deps mess things up
    //implementation("com.yahoo.vespa:documentapi:8.324.16")
    //implementation("com.yahoo.vespa:documentapi-dependencies:8.324.16")

    implementation("com.yahoo.vespa:vespa-feed-client:8.324.16")

    // For apache http client SSL connectivity
    implementation("io.github.hakky54:sslcontext-kickstart:8.3.4")
    implementation("io.github.hakky54:sslcontext-kickstart-for-pem:8.3.4")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.0")

    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")
    implementation("org.jgrapht:jgrapht-ext:1.5.2")

    implementation("org.graphstream:gs-core:2.0")
    implementation("org.graphstream:gs-ui-swing:2.0")

}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

// Will this ALWAYS be run?
tasks.register("gen.props") {
    doFirst {
        File("src/main/resources/build-info.properties").printWriter().use { out ->
            System.out.println("Generating build-info.properties...")
            var ts = System.currentTimeMillis();
            val humanReadable = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(Date(ts))
            out.println("build-timestamp: " + humanReadable);
            out.println("built-by: " + System.getenv("USER"))
        }
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
var ideaVersion = System.getProperty("ideaVersion", "2023.1.5")
intellij {
    version.set(ideaVersion)
    System.out.println("Build for VERSION " + ideaVersion)
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java", "org.jetbrains.idea.maven"))

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    compileKotlin {
        dependsOn("gen.props")
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
