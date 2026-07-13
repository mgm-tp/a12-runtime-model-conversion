plugins {
    id("java")
}

// Configuration for the FAT JAR as dependency
val conversionFatJar: Configuration by configurations.creating

// Configuration for WCF CLI (no transitive dependencies needed for fat jar)
val wcfCli: Configuration by configurations.creating {
    isTransitive = false
}

dependencies {
    // FAT JAR from conversion module as dependency
    conversionFatJar(project(path = ":conversion", configuration = "shadow"))

    wcfCli(variantOf(a12.wcf.cli) { classifier("fatjar") })

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jackson.databind)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
        dependsOn("runWcfCliExec")
    }

    register<Exec>("runWcfCliExec") {
        group = "verification"
        description = "Executes wcf-cli with the conversion-fatjar.jar"

        dependsOn(":conversion:shadowJar")

        workingDir = projectDir

        // Register inputs
        inputs.files(configurations.getByName("wcfCli"))
        inputs.files(configurations.getByName("conversionFatJar"))
        inputs.dir("../conversion/src/test/resources/models/combinationmodel")

        // Register output directory
        outputs.dir(file("build/converted"))
            .withPropertyName("convertedOutput")

        // Get path to WCF CLI JAR via Configuration
        val wcfCliJarPath =
            configurations.getByName("wcfCli")
                .resolve()
                .single()
                .absolutePath

        // Get path to Conversion FAT JAR via Configuration
        val conversionJarPath =
            configurations.getByName("conversionFatJar")
                .resolve()
                .single()
                .absolutePath

        commandLine(
            "java",
            "-jar",
            wcfCliJarPath,
            file("../conversion/src/test/resources/models/combinationmodel").absolutePath,
            file("build/converted").absolutePath,
            "-c",
            conversionJarPath
        )
    }
}
