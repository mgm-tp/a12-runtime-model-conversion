import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.cyclonedx.gradle.CyclonedxDirectTask
import org.cyclonedx.model.ExternalReference

plugins {
    id("java")
    id("maven-publish")
    alias(thirdParty.plugins.shadow)
    alias(a12.plugins.artifact.publish)
    alias(thirdParty.plugins.cyclonedx)
}

val a12ReleaseLine: String by project

a12Publish {
    releaseLine = a12ReleaseLine
    artifactType = "community"
}

dependencies {
    implementation(thirdParty.jackson.databind)
    implementation(a12.wcf.api)
    implementation(a12.kernel.md.facade)
    implementation(a12.kernel.md.workspace.converter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(thirdParty.junit.jupiter)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    named<Jar>("jar") {
        archiveClassifier.set("")
        listOf("LICENSE", "NOTICE", "THIRD_PARTY_NOTICES").forEach { name ->
            from(rootProject.layout.projectDirectory.file(name)) {
                into("META-INF")
            }
        }
        from(rootProject.layout.projectDirectory) {
            include("licenses/**")
            exclude("licenses/license-header.txt")
            into("META-INF")
        }
    }
    named<CyclonedxDirectTask>("cyclonedxDirectBom") {
        externalReferences.set(
            listOf(
                ExternalReference().apply {
                    url = "https://git.geta12.com"
                    type = ExternalReference.Type.VCS
                }
            )
        )
        includeBuildSystem.set(false)
        xmlOutput.unsetConvention()
        jsonOutput.set(layout.buildDirectory.file("reports/sbom/cyclonedx.json"))
    }
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("fatjar")

        // EXCLUDE would drop duplicate service files before mergeServiceFiles() can merge them
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        mergeServiceFiles()

        listOf("LICENSE", "NOTICE").forEach { name ->
            from(rootProject.layout.projectDirectory.file(name)) {
                into("META-INF")
            }
        }
        from(project.layout.projectDirectory) {
            include("THIRD_PARTY_NOTICES")
            include("licenses/**")
            into("META-INF")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "a12Artifactory"
            url = uri("https://placeholder")
        }
    }
    publications {
        create<MavenPublication>("conversion") {
            artifactId = "conversion"
            from(components["java"])
            artifact(layout.buildDirectory.file("reports/sbom/cyclonedx.json")) {
                classifier = "cyclonedx"
            }
        }
        create<MavenPublication>("conversionFatjar") {
            artifactId = "conversion"
            artifact(tasks.named("shadowJar"))
        }
    }
    tasks.matching { it.name.startsWith("publish") }.configureEach {
        dependsOn(tasks.named("cyclonedxDirectBom"))
    }
}
