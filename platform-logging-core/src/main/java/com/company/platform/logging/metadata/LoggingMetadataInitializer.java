package com.company.platform.logging.metadata;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.qos.logback.classic.LoggerContext;
import com.company.platform.logging.config.LoggingProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Stamps static deployment metadata into the Logback {@link LoggerContext} so every appender
 * can reference {@code ${app}}, {@code ${env}}, {@code ${version}}, {@code ${host}} regardless
 * of which thread emits the event.
 *
 * <p>Values are resolved with this precedence:
 * <ol>
 *     <li>{@code platform.logging.metadata.*}</li>
 *     <li>Standard Spring properties (e.g. {@code spring.application.name})</li>
 *     <li>Sensible runtime defaults (hostname lookup, etc.)</li>
 * </ol>
 *
 * <p>Context properties are deliberately chosen over MDC: MDC is thread-local and would only
 * apply on the bootstrap thread, while context properties apply globally for the application
 * lifetime, which matches the meaning of "static deployment metadata".
 */
public class LoggingMetadataInitializer {

    static final String KEY_APP = "app";
    static final String KEY_ENV = "env";
    static final String KEY_VERSION = "version";
    static final String KEY_HOST = "host";

    private final LoggingProperties.Metadata metadata;
    private final Environment environment;

    public LoggingMetadataInitializer(LoggingProperties.Metadata metadata, Environment environment) {
        this.metadata = metadata;
        this.environment = environment;
    }

    @PostConstruct
    public void initialize() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof LoggerContext context)) {
            // Non-Logback backend; nothing to do.
            return;
        }
        putIfPresent(context, KEY_APP, firstNonBlank(
                metadata.getApplicationName(),
                environment.getProperty("spring.application.name")));
        putIfPresent(context, KEY_ENV, firstNonBlank(
                metadata.getEnvironment(),
                environment.getProperty("spring.profiles.active"),
                environment.getProperty("ENVIRONMENT")));
        putIfPresent(context, KEY_VERSION, firstNonBlank(
                metadata.getVersion(),
                environment.getProperty("info.app.version"),
                environment.getProperty("application.version")));
        putIfPresent(context, KEY_HOST, firstNonBlank(metadata.getHost(), resolveHost()));
    }

    private static void putIfPresent(LoggerContext context, String key, String value) {
        if (value != null && !value.isBlank()) {
            context.putProperty(key, value);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
