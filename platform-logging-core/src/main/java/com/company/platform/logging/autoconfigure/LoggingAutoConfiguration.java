package com.company.platform.logging.autoconfigure;

import com.company.platform.logging.config.LoggingProperties;
import com.company.platform.logging.correlation.CorrelationIdFilter;
import com.company.platform.logging.masking.RegexSensitiveDataMasker;
import com.company.platform.logging.masking.SensitiveDataMasker;
import com.company.platform.logging.metadata.LoggingMetadataInitializer;
import com.company.platform.logging.web.RequestLoggingFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Wires the platform logging components and only contributes beans the user has not
 * overridden. Each piece can be disabled independently via {@code platform.logging.*.enabled}.
 */
@AutoConfiguration
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "platform.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SensitiveDataMasker sensitiveDataMasker(LoggingProperties properties) {
        return new RegexSensitiveDataMasker(properties.getMasking());
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingMetadataInitializer loggingMetadataInitializer(LoggingProperties properties,
                                                                 Environment environment) {
        return new LoggingMetadataInitializer(properties.getMetadata(), environment);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass({OncePerRequestFilter.class, Filter.class})
    static class WebFilterConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "platform.logging.correlation", name = "enabled",
                havingValue = "true", matchIfMissing = true)
        public CorrelationIdFilter correlationIdFilter(LoggingProperties properties) {
            return new CorrelationIdFilter(properties.getCorrelation());
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "platform.logging.request-logging", name = "enabled",
                havingValue = "true", matchIfMissing = true)
        public RequestLoggingFilter requestLoggingFilter(LoggingProperties properties,
                                                         SensitiveDataMasker masker) {
            return new RequestLoggingFilter(properties.getRequestLogging(), masker);
        }
    }
}
