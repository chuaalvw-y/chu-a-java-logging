// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

plugins {
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.springframework.boot") version "4.0.6" apply false
    id("com.diffplug.spotless") version "6.25.0"
}

val springBootVersion: String by project

allprojects {
    group = "com.chua.erp"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
    }

    extensions.configure<CheckstyleExtension> {
        toolVersion = "10.18.2"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isIgnoreFailures = false
        maxWarnings = 0
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target("src/**/*.java")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
            importOrder("java", "javax", "jakarta", "org", "com", "")
        }
        kotlinGradle {
            target("*.gradle.kts", "**/*.gradle.kts")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // -parameters helps @ConfigurationProperties constructor binding & SpEL.
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
