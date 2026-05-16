package com.company.platform.logging.logback;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback {@link PatternLayout} that applies masking patterns to the formatted message.
 *
 * <p>Configured directly in {@code logback-spring.xml} for users who cannot route every
 * log call through {@link com.company.platform.logging.api.PlatformLogger}:
 *
 * <pre>{@code
 * <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
 *   <layout class="com.company.platform.logging.logback.MaskingPatternLayout">
 *     <pattern>%d %-5level [%X{correlationId:-}] %logger - %msg%n</pattern>
 *     <mask>(?i)(password\s*[:=]\s*)\S+</mask>
 *     <mask>(?i)(token\s*[:=]\s*)\S+</mask>
 *   </layout>
 * </encoder>
 * }</pre>
 */
public class MaskingPatternLayout extends PatternLayout {

    private static final String DEFAULT_REPLACEMENT = "****";

    private final List<Pattern> patterns = new ArrayList<>();
    private String replacement = DEFAULT_REPLACEMENT;

    public void addMask(String pattern) {
        this.patterns.add(Pattern.compile(pattern));
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String formatted = super.doLayout(event);
        if (patterns.isEmpty()) {
            return formatted;
        }
        String result = formatted;
        for (Pattern pattern : patterns) {
            result = pattern.matcher(result).replaceAll(matchResult -> {
                if (matchResult.groupCount() >= 1 && matchResult.group(1) != null) {
                    return java.util.regex.Matcher.quoteReplacement(matchResult.group(1) + replacement);
                }
                return java.util.regex.Matcher.quoteReplacement(replacement);
            });
        }
        return result;
    }
}
