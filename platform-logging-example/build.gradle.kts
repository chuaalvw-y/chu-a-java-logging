plugins {
    id("org.springframework.boot")
}

description = "Sample Spring Boot service consuming the platform-logging library."

val logstashEncoderVersion: String by project
val springdocOpenapiVersion: String by project

dependencies {
    implementation(project(":platform-logging-core"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocOpenapiVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

springBoot {
    mainClass.set("com.company.platform.logging.example.ExampleApplication")
}

// Examples are demos, not published.
tasks.named("javadoc") {
    enabled = false
}
