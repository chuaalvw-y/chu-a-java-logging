plugins {
    application
}

description = "Sample Spring Boot service consuming platform-logging-core."

dependencies {
    implementation(project(":platform-logging-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.company.platform.logging.example.ExampleApplication")
}
