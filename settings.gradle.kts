rootProject.name = "rmc"

include("conversion")
include("integration-test")

dependencyResolutionManagement {
    versionCatalogs {
        create("gradlePluginLibs") {
            version("buildTimeTracker", "4.3.0")
            version("cyclonedx", "3.1.0")
            version("git-version", "3.1.0")
            version("license", "0.16.1")
            version("owasp", "12.2.2")
            version("sonarqube", "7.2.2.6593")

            plugin("buildTimeTracker", "com.asarkar.gradle.build-time-tracker").versionRef("buildTimeTracker")
            plugin("cyclonedx", "org.cyclonedx.bom").versionRef("cyclonedx")
            plugin("license", "com.github.hierynomus.license").versionRef("license")
            plugin("owasp-dependencyCheck", "org.owasp.dependencycheck").versionRef("owasp")
            plugin("palantir-git", "com.palantir.git-version").versionRef("git-version")
            plugin("sonarqube", "org.sonarqube").versionRef("sonarqube")
        }
        create("a12") {
            version("artifact-publish", "0.4.2")
            plugin("artifact-publish", "com.mgmtp.a12.bd.artifact-publish").versionRef("artifact-publish")

            version("wcf", "1.1.1")
            version("kernel", "31.1.0")

            library("kernel.md.facade", "com.mgmtp.a12.kernel", "kernel-md-facade").versionRef("kernel")
            library(
                "kernel-md-workspace-converter",
                "com.mgmtp.a12.kernel",
                "kernel-md-workspace-converter"
            ).versionRef("kernel")
            library("wcf-api", "com.mgmtp.a12.dataservices.wcf", "dataservices-wcf-api").versionRef("wcf")
            library("wcf-cli", "com.mgmtp.a12.dataservices.wcf", "dataservices-wcf-cli").versionRef("wcf")
        }

        create("libs") {
            version("jackson", "3.1.2")
            version("junit", "5.10.2")
            library("jackson-databind", "tools.jackson.core", "jackson-databind").versionRef("jackson")
            library("jackson-dataformat-yaml", "tools.jackson.dataformat", "jackson-dataformat-yaml").versionRef("jackson")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit")
        }
    }
}
