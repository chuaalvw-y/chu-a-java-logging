plugins {
    application
    id("org.springframework.boot")
}

description = "Sample Spring Boot service consuming platform-logging-core."

val logstashEncoderVersion = "7.4"

dependencies {
    implementation(project(":platform-logging-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.company.platform.logging.example.ExampleApplication")
}
