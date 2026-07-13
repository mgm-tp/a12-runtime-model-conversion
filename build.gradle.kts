plugins {
    alias(gradlePluginLibs.plugins.palantir.git)
    alias(gradlePluginLibs.plugins.sonarqube)
    alias(gradlePluginLibs.plugins.owasp.dependencyCheck)
    alias(gradlePluginLibs.plugins.buildTimeTracker)
    id("com.diffplug.spotless") version "8.3.0"
}

spotless {
    java {
        licenseHeaderFile("licenses/license-header.txt")
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
val buildNr: String? = System.getenv("BUILD_NUMBER")

allprojects {
    version =
        if (isReleaseBuild) {
            gitVersion()
        } else {
            calculateNightlyVersion()
        }

    tasks.withType<JavaCompile> {
        options.release.set(21)
    }
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
