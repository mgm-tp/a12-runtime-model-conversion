plugins {

    alias(thirdParty.plugins.palantir.git)
    alias(thirdParty.plugins.sonarqube)
    alias(thirdParty.plugins.owasp.dependencyCheck)
    alias(thirdParty.plugins.buildTimeTracker)
    alias(thirdParty.plugins.spotless)
    alias(a12.plugins.integration)
    alias(a12.plugins.third.party.notices)
}

val a12ReleaseLine: String by project

spotless {
    java {
        licenseHeaderFile("copyright/license-header.txt")
        googleJavaFormat()
        target("conversion/src/**/*.java", "integration-test/src/**/*.java")
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
    }
}

val isReleaseBuild = System.getenv("TAG_NAME") != null
val isMasterBuild = System.getenv("BRANCH_NAME") == "master"
val isSupportBuild = System.getenv("BRANCH_NAME")?.startsWith("support/") ?: false
val gitVersion: groovy.lang.Closure<String> by extra
val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val buildNr: String? = System.getenv("BUILD_NUMBER")
// The BD integration build invokes one of these tasks, so their presence on the command line marks an integration run
val integrationTaskNames = listOf("prepareForIntegration", "buildAndTestForIntegration", "publishForIntegration")
val isIntegrationBuild =
    gradle.startParameter.taskNames.any { requested ->
        requested.substringAfterLast(':') in integrationTaskNames
    }

allprojects {
    version =
        if (isReleaseBuild) {
            gitVersion()
        } else if (isIntegrationBuild) {
            calculateIntegrationVersion()
        } else {
            calculateNightlyVersion()
        }

    tasks.withType<JavaCompile> {
        options.release.set(21)
    }
}

fun calculateIntegrationVersion(): String {
    // Integration builds are versioned "<version>-build.<buildNumber>.integration"
    val buildNumber = buildNr ?: "0"
    return "${nextMinorVersion()}.0-build.$buildNumber.integration"
}

fun calculateNightlyVersion(): String {
    val additionalSuffix = if (isMasterBuild || isSupportBuild) buildNr else "0"
    return if (isSupportBuild) {
        "${nextMinorVersion()}.0-build.$additionalSuffix"
    } else {
        "${nextMajorVersion()}.0.0-build.$additionalSuffix"
    }
}

fun nextMajorVersion(): Number {
    val gitVersion = gitVersion()
    val majorVersion = if (gitVersion.indexOf(".") > 0) gitVersion.substring(0, gitVersion.indexOf(".")).toInt() else 0
    if (gitVersion.matches("\\d+\\.0\\.0-(rc|pre).*".toRegex())) {
        return majorVersion
    } else if (gitVersion.matches("(\\d+\\.)+\\d.*".toRegex())) {
        return majorVersion + 1
    }
    return 0
}

fun nextMinorVersion(): String {
    val gitVersion = gitVersion()
    val versionParts = gitVersion.split(".")
    if (versionParts.size < 2) throw RuntimeException("Cannot determine minor version from $gitVersion")
    val major = versionParts[0].toInt()
    val minor = versionParts[1].toInt()
    return "$major.${minor + 1}"
}

integration {
    libsVersionsFile = layout.projectDirectory.file("gradle/a12.versions.toml")
    buildTasks =
        listOf(
            "spotlessCheck",
            "assemble",
            ":conversion:check",
            ":integration-test:check"
        )
    additionalComponents =
        mapOf(
            "kernel" to
                mapOf(
                    "npm" to
                        listOf<String>(),
                    "toml" to listOf("kernel")
                ),
            "wcf" to
                mapOf(
                    "npm" to
                        listOf<String>(),
                    "toml" to listOf("wcf")
                )
        )
    publishTasks = listOf(":conversion:publish")
}

thirdPartyNotices {
    releaseLine.set(a12ReleaseLine)

    notices {
        register("main") {
            componentName.set("Runtime Model Conversion (RMC)")

            // Aggregates the direct dependencies of every subproject; mirrors the hand-written root THIRD_PARTY_NOTICES.
            projectPaths.set(rootProject.subprojects.map { it.path })
            failOnMissingConfiguration.set(false)

            failOnMissingMetadata.set(true)
        }

        register("conversionFatJar") {
            componentName.set("Runtime Model Conversion (RMC) Fat JAR")
            outputFile.set(rootProject.file("conversion/THIRD_PARTY_NOTICES"))
            projectPaths.set(listOf(":conversion"))

            failOnMissingMetadata.set(true)

            // fat JAR bundles its whole dependency tree
            includeTransitiveDependencies.set(true)
        }
    }
}

dependencyCheck {
    // https://nvd.nist.gov/developers/request-an-api-key
    nvd.apiKey = (project.findProperty("nvd_apiKey") ?: System.getenv("nvd_apiKey")) as String?
    formats = listOf("XML", "JSON", "HTML")
    suppressionFile = rootProject.file("audit-suppressions.xml").toString()
    analyzers {
        ossIndex.enabled = false
        retirejs.enabled = false
        assemblyEnabled = false
    }
    failBuildOnCVSS = 7f
}

sonarqube {
    properties {
        property("sonar.projectKey", "rmc")
        property("sonar.projectName", "Runtime Model Conversion (RMC)")
        property("sonar.scm.provider", "git")

        property(
            "sonar.dependencyCheck.reportPath",
            "${dependencyCheck.outputDirectory}/dependency-check-report.xml"
        )
        property(
            "sonar.dependencyCheck.htmlReportPath",
            "${dependencyCheck.outputDirectory}/dependency-check-report.html"
        )
        property(
            "sonar.dependencyCheck.jsonReportPath",
            "${dependencyCheck.outputDirectory}/dependency-check-report.json"
        )
    }
}

tasks {
    register<Exec>("unchangedSourceCheck") {
        onlyIf("run if version contains dirty") {
            gitVersion().contains("dirty")
        }
        doFirst {
            logger.lifecycle("Git Status:")
        }
        commandLine("git", "status", "-s")
        doLast {
            throw BuildCancelledException("Dirty version detected: files under version control must not change during build.")
        }
    }
}
