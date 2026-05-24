// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

plugins {
    `java-library`
    `maven-publish`
}

description = "Enterprise Spring Boot logging library: correlation IDs, sensitive-data masking, request logging, JSON stdout for Splunk."

val logstashEncoderVersion: String by project
val micrometerTracingVersion: String by project

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-autoconfigure")

    compileOnly("org.springframework:spring-web")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    compileOnly("io.micrometer:micrometer-tracing-bridge-otel:$micrometerTracingVersion")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("library") {
            // Group ID lives at the project level; override artifactId per the chua-erp- naming convention.
            artifactId = "chua-erp-platform-logging-core"
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set("Chua ERP — Platform Logging Core")
                description.set(project.description)
            }
        }
    }

    repositories {
        // Default in gradle.properties; override per environment with
        //   ./gradlew publish -PlocalRepoUrl=file:///some/other/path
        val localRepoUrl: String by project
        maven {
            name = "localRepo"
            url = uri(localRepoUrl)
        }
    }
}
