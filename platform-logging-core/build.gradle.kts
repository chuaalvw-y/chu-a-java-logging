plugins {
    `java-library`
    `maven-publish`
}

description = "Enterprise Spring Boot logging library: correlation IDs, sensitive-data masking, request logging, JSON stdout for Splunk."

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-autoconfigure")

    compileOnly("org.springframework:spring-web")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("net.logstash.logback:logstash-logback-encoder:7.4")
    compileOnly("io.micrometer:micrometer-tracing-bridge-otel:1.3.4")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("net.logstash.logback:logstash-logback-encoder:7.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("library") {
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
                name.set("Platform Logging Core")
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
