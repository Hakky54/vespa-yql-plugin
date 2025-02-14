import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("org.jetbrains.idea.maven")
    }

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

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    compileJava {
        dependsOn("gen.props")
        options.encoding = "UTF-8"
        // options.compilerArgs.addAll(listOf("--release", "17"))
    }
}