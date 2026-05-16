# platform-logging

Enterprise-grade reusable Spring Boot logging library. Provides correlation-ID propagation,
JSON structured logging for container stdout (Docker / Kubernetes / OpenShift / Splunk),
sensitive-data masking, request/response logging, and static deployment metadata in MDC —
all wired automatically through Spring Boot auto-configuration.

- Target: **Java 17+**, **Spring Boot 3.x**
- Base package: `com.company.platform.logging`
- Backend: **SLF4J + Logback** (the Spring Boot default)

---

## Modules

| Module | Purpose |
| --- | --- |
| `platform-logging-core` | The reusable library — auto-config, properties, filters, masker, Logback fragments |
| `platform-logging-example` | A sample Spring Boot service that consumes the library |

```
platform-logging-parent/
├── platform-logging-core/
│   └── src/main/java/com/company/platform/logging/
│       ├── api/PlatformLogger.java                 (optional convenience facade)
│       ├── autoconfigure/LoggingAutoConfiguration.java
│       ├── config/LoggingProperties.java           (@ConfigurationProperties)
│       ├── correlation/CorrelationContext.java
│       ├── correlation/CorrelationIdFilter.java    (OncePerRequestFilter)
│       ├── logback/MaskingPatternLayout.java
│       ├── masking/SensitiveDataMasker.java        (interface)
│       ├── masking/RegexSensitiveDataMasker.java
│       ├── metadata/LoggingMetadataInitializer.java
│       └── web/RequestLoggingFilter.java
└── platform-logging-example/
```

---

## Getting started

### Maven

```xml
<dependency>
    <groupId>com.company.platform</groupId>
    <artifactId>platform-logging-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Required if you use the bundled JSON appender (platform-logging-json.xml) -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### Gradle

```kotlin
implementation("com.company.platform:platform-logging-core:1.0.0-SNAPSHOT")
implementation("net.logstash.logback:logstash-logback-encoder:7.4")
```

No `@Enable…` annotation is needed. Spring Boot picks up
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
and registers `LoggingAutoConfiguration` automatically.

---

## What you get out of the box

| Concern | Component | Default |
| --- | --- | --- |
| Correlation / trace / request IDs | `CorrelationIdFilter` + MDC key `correlationId` | Reads `X-Correlation-Id`; generates UUID v4 if missing |
| Request/response logging | `RequestLoggingFilter` | One log line per request: method, path, status, duration |
| Sensitive-data masking | `RegexSensitiveDataMasker` | Masks common field names + sensitive headers |
| App/env/version/host in MDC | `LoggingMetadataInitializer` | Populated at startup from `spring.application.name`, `spring.profiles.active`, hostname |
| JSON structured stdout | `platform-logging-json.xml` (Logback include) | UTC timestamp, level, logger, MDC, message, stack trace |
| Human-readable console | `platform-logging-console.xml` (Logback include) | Includes correlationId in the prefix |

---

## Configuration

All settings live under `platform.logging.*`. Defaults are sensible — override only what you need.

```yaml
platform:
  logging:
    enabled: true

    metadata:
      application-name: ${spring.application.name}
      environment: ${spring.profiles.active}
      version: ${APP_VERSION:0.0.0-dev}

    correlation:
      enabled: true
      header-name: X-Correlation-Id
      mdc-key: correlationId
      generate-if-missing: true
      include-in-response: true

    request-logging:
      enabled: true
      include-query-string: true
      include-headers: false           # set true with caution; values are masked
      include-payload: false           # set true with caution; values are masked & truncated
      max-payload-length: 2048
      excluded-paths:
        - /actuator/**
        - /health
        - /metrics

    masking:
      enabled: true
      mask: "****"
      sensitive-fields:
        - password
        - token
        - ssn
        - taxId
        - accountNumber
        - cardNumber
        - cvv
        - authorization
      sensitive-headers:
        - Authorization
        - Cookie
        - X-Api-Key
      additional-patterns: []          # raw regex; group(1) is masked if present
```

### Disabling individual pieces

Every component has its own toggle so teams can opt out without forking:

- `platform.logging.enabled=false` — disable the entire library
- `platform.logging.correlation.enabled=false`
- `platform.logging.request-logging.enabled=false`
- `platform.logging.masking.enabled=false`

---

## Logback wiring

Include one or both fragments from your `logback-spring.xml`:

```xml
<configuration>
    <include resource="com/company/platform/logging/logback/platform-logging-console.xml"/>
    <include resource="com/company/platform/logging/logback/platform-logging-json.xml"/>

    <springProfile name="local | dev">
        <root level="INFO"><appender-ref ref="PLATFORM_CONSOLE"/></root>
    </springProfile>
    <springProfile name="!local &amp; !dev">
        <root level="INFO"><appender-ref ref="PLATFORM_JSON"/></root>
    </springProfile>
</configuration>
```

The example service in `platform-logging-example/` does exactly this.

---

## Stdout vs Splunk ingestion

The library targets **stdout-based** log collection. It does **not** ship its own HTTP Event
Collector client — that responsibility belongs to your platform infrastructure. Two patterns
are supported, and you choose between them at deployment time, not at build time:

### Pattern 1 — stdout → forwarder → Splunk (recommended for k8s / OpenShift)

The application writes JSON to stdout. A node-level forwarder (Splunk Universal Forwarder,
Fluent Bit, Vector, etc.) tails container stdout and ships events to Splunk indexers.

```
[ JVM ]  ──stdout──►  [ container runtime ]  ──tail──►  [ forwarder ]  ──TCP/HEC──►  [ Splunk ]
```

Operator setup in `inputs.conf`:

```ini
[monitor:///var/log/containers/*.log]
sourcetype = _json
index = platform-apps
```

Pros: the application never blocks on network I/O, backpressure is handled by the forwarder,
log shipping survives application crashes, and no Splunk credentials live in the application.
**This is the recommended pattern.**

### Pattern 2 — direct to Splunk HTTP Event Collector (HEC)

When no forwarder is available (e.g. legacy VMs, ad-hoc services), the application can post
events directly to HEC. The library does not provide an appender for this out of the box; if
you need it, add a Logback HTTP appender and point it at:

```
POST https://<splunk-host>:8088/services/collector
Authorization: Splunk <HEC_TOKEN>
Content-Type: application/json

{ "event": { ...json from our encoder... }, "sourcetype": "_json", "index": "platform-apps" }
```

Surface the HEC token via environment variables or a secrets manager — never the application
properties file. A placeholder integration test exists at
`platform-logging-core/src/test/java/com/company/platform/logging/splunk/SplunkHecForwarderIntegrationTest.java`.

---

## OpenTelemetry / distributed tracing

When Micrometer Tracing (Spring Boot 3.x) or the OpenTelemetry Java agent is on the classpath,
trace and span IDs land in MDC as `traceId` / `spanId` automatically — no library
configuration needed. The bundled JSON encoder forwards the full MDC, so these flow through to
Splunk and any other backend. Use `correlationId` for user-facing request correlation and
`traceId` for distributed-tracing tools; they coexist cleanly.

---

## Extension points

| To customize… | Provide a bean of type… | Notes |
| --- | --- | --- |
| Masking strategy | `SensitiveDataMasker` | Auto-config backs off when present |
| Correlation behaviour | `CorrelationIdFilter` | Subclass or replace; auto-config backs off |
| Request logging format | `RequestLoggingFilter` | Replace to emit a different shape |
| JSON envelope | A custom Logback encoder | Override the included XML fragment |

---

## Example usage

```java
@RestController
@RequestMapping("/orders")
class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    // Use SLF4J directly — correlationId, app, env, host are already in MDC.
    @GetMapping("/{id}")
    Order get(@PathVariable String id) {
        log.info("Fetching order id={}", id);
        return service.find(id);
    }
}
```

For call sites that want masking applied at the source:

```java
PlatformLogger plog = PlatformLogger.forClass(OrderService.class, masker);
plog.infoMasked("Received payload: " + rawJson);
```

A full runnable service lives in `platform-logging-example/`.

---

## Building and testing

```bash
mvn -q clean verify
```

Unit tests cover:

- `RegexSensitiveDataMaskerTest` — JSON / form / header / case-insensitive / null behaviour
- `CorrelationIdFilterTest` — propagation, generation, MDC cleanup (including on exceptions)
- `RequestLoggingFilterTest` — MockMvc tests for emission, masking, header masking, path exclusion
- `LoggingAutoConfigurationTest` — Spring `ApplicationContextRunner` smoke tests
- `OrderControllerIntegrationTest` — end-to-end MockMvc test in the example service

`SplunkHecForwarderIntegrationTest` is a `@Disabled` placeholder requiring a live HEC endpoint.

---

## License

See [LICENSE](LICENSE).
