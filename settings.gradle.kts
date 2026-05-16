pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "platform-logging"

include(
    ":platform-logging-core",
    ":platform-logging-example",
)
