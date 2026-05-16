plugins {
    `java-library`
}

description = "Core auto-configurable logging components: correlation, masking, request logging."

val logstashEncoderVersion = "7.4"
val micrometerTracingVersion = "1.3.4"

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-autoconfigure")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("org.springframework:spring-web")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    compileOnly("io.micrometer:micrometer-tracing-bridge-otel:$micrometerTracingVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
