package com.company.platform.logging.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly typed configuration for the platform logging library.
 *
 * <p>All values can be overridden in {@code application.yml} under {@code platform.logging}.
 */
@ConfigurationProperties(prefix = "platform.logging")
public class LoggingProperties {

    /** Master switch. When false the library contributes no beans. */
    private boolean enabled = true;

    /** Application metadata stamped into every log event via MDC. */
    private final Metadata metadata = new Metadata();

    /** Correlation / trace ID propagation settings. */
    private final Correlation correlation = new Correlation();

    /** Request / response logging filter settings. */
    private final RequestLogging requestLogging = new RequestLogging();

    /** Sensitive-data masking settings. */
    private final Masking masking = new Masking();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Correlation getCorrelation() {
        return correlation;
    }

    public RequestLogging getRequestLogging() {
        return requestLogging;
    }

    public Masking getMasking() {
        return masking;
    }

    /** Static metadata stamped into MDC at startup. */
    public static class Metadata {
        private String applicationName;
        private String environment;
        private String version;
        private String host;

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    /** Correlation ID propagation. */
    public static class Correlation {
        private boolean enabled = true;
        private String headerName = "X-Correlation-Id";
        private String mdcKey = "correlationId";
        private boolean generateIfMissing = true;
        private boolean includeInResponse = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getMdcKey() {
            return mdcKey;
        }

        public void setMdcKey(String mdcKey) {
            this.mdcKey = mdcKey;
        }

        public boolean isGenerateIfMissing() {
            return generateIfMissing;
        }

        public void setGenerateIfMissing(boolean generateIfMissing) {
            this.generateIfMissing = generateIfMissing;
        }

        public boolean isIncludeInResponse() {
            return includeInResponse;
        }

        public void setIncludeInResponse(boolean includeInResponse) {
            this.includeInResponse = includeInResponse;
        }
    }

    /** Request / response logging. */
    public static class RequestLogging {
        private boolean enabled = true;
        private boolean includeQueryString = true;
        private boolean includeHeaders = false;
        private boolean includePayload = false;
        private int maxPayloadLength = 2048;
        private List<String> excludedPaths = new ArrayList<>(List.of(
                "/actuator/**",
                "/health",
                "/metrics"
        ));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeQueryString() {
            return includeQueryString;
        }

        public void setIncludeQueryString(boolean includeQueryString) {
            this.includeQueryString = includeQueryString;
        }

        public boolean isIncludeHeaders() {
            return includeHeaders;
        }

        public void setIncludeHeaders(boolean includeHeaders) {
            this.includeHeaders = includeHeaders;
        }

        public boolean isIncludePayload() {
            return includePayload;
        }

        public void setIncludePayload(boolean includePayload) {
            this.includePayload = includePayload;
        }

        public int getMaxPayloadLength() {
            return maxPayloadLength;
        }

        public void setMaxPayloadLength(int maxPayloadLength) {
            this.maxPayloadLength = maxPayloadLength;
        }

        public List<String> getExcludedPaths() {
            return excludedPaths;
        }

        public void setExcludedPaths(List<String> excludedPaths) {
            this.excludedPaths = excludedPaths;
        }
    }

    /** Sensitive-data masking. */
    public static class Masking {
        private boolean enabled = true;
        private String mask = "****";

        /** Field / JSON-key names whose values will be replaced wholesale. */
        private Set<String> sensitiveFields = new java.util.LinkedHashSet<>(Set.of(
                "password",
                "passwd",
                "secret",
                "token",
                "accessToken",
                "refreshToken",
                "apiKey",
                "authorization",
                "ssn",
                "socialSecurityNumber",
                "taxId",
                "accountNumber",
                "cardNumber",
                "cvv"
        ));

        /** HTTP headers whose values will be replaced wholesale. */
        private Set<String> sensitiveHeaders = new java.util.LinkedHashSet<>(Set.of(
                "Authorization",
                "Proxy-Authorization",
                "Cookie",
                "Set-Cookie",
                "X-Api-Key"
        ));

        /** Additional free-form regex patterns applied to log messages. */
        private List<String> additionalPatterns = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMask() {
            return mask;
        }

        public void setMask(String mask) {
            this.mask = mask;
        }

        public Set<String> getSensitiveFields() {
            return sensitiveFields;
        }

        public void setSensitiveFields(Set<String> sensitiveFields) {
            this.sensitiveFields = sensitiveFields;
        }

        public Set<String> getSensitiveHeaders() {
            return sensitiveHeaders;
        }

        public void setSensitiveHeaders(Set<String> sensitiveHeaders) {
            this.sensitiveHeaders = sensitiveHeaders;
        }

        public List<String> getAdditionalPatterns() {
            return additionalPatterns;
        }

        public void setAdditionalPatterns(List<String> additionalPatterns) {
            this.additionalPatterns = additionalPatterns;
        }
    }
}
