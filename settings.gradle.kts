rootProject.name = "rmc"

include("conversion")
include("integration-test")

dependencyResolutionManagement {
    versionCatalogs {
        create("a12") {
            from(files("gradle/a12.versions.toml"))
        }
        create("thirdParty") {
            from(files("gradle/thirdParty.versions.toml"))
        }
    }
}
