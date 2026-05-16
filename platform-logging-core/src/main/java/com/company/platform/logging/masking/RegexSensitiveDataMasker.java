package com.company.platform.logging.masking;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.company.platform.logging.config.LoggingProperties;

/**
 * Regex-based masker. Compiles patterns once at construction time and is fully thread-safe.
 *
 * <p>Three layers of masking are applied:
 * <ol>
 *   <li>JSON-style key/value pairs — {@code "password":"hunter2"} → {@code "password":"****"}</li>
 *   <li>Form / query-string pairs — {@code password=hunter2} → {@code password=****}</li>
 *   <li>Any additional user-supplied regex patterns (group 1 is masked if present, otherwise the entire match)</li>
 * </ol>
 */
public class RegexSensitiveDataMasker implements SensitiveDataMasker {

    private final String mask;
    private final Set<String> sensitiveHeadersLower;
    private final List<Pattern> jsonPatterns;
    private final List<Pattern> kvPatterns;
    private final List<Pattern> additionalPatterns;

    public RegexSensitiveDataMasker(LoggingProperties.Masking config) {
        this.mask = config.getMask();
        this.sensitiveHeadersLower = new LinkedHashSet<>();
        for (String h : config.getSensitiveHeaders()) {
            this.sensitiveHeadersLower.add(h.toLowerCase(Locale.ROOT));
        }
        this.jsonPatterns = new ArrayList<>();
        this.kvPatterns = new ArrayList<>();
        for (String field : config.getSensitiveFields()) {
            String quoted = Pattern.quote(field);
            // "field" : "value"  — value captured in group 1
            this.jsonPatterns.add(Pattern.compile(
                    "(?i)(\"" + quoted + "\"\\s*:\\s*\")([^\"]*)(\")"));
            // "field" : 1234     — numeric/boolean/null literal
            this.jsonPatterns.add(Pattern.compile(
                    "(?i)(\"" + quoted + "\"\\s*:\\s*)(-?\\d+(?:\\.\\d+)?|true|false|null)"));
            // field=value form/querystring pair
            this.kvPatterns.add(Pattern.compile(
                    "(?i)(\\b" + quoted + "\\s*[=:]\\s*)([^&\\s,;]+)"));
        }
        this.additionalPatterns = new ArrayList<>();
        for (String pattern : config.getAdditionalPatterns()) {
            this.additionalPatterns.add(Pattern.compile(pattern));
        }
    }

    @Override
    public String mask(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = input;
        for (Pattern p : jsonPatterns) {
            // For string values: keep prefix ($1) and closing quote ($3). For literals: keep prefix ($1) only.
            result = p.matcher(result).replaceAll(matchResult -> {
                if (matchResult.groupCount() >= 3) {
                    return java.util.regex.Matcher.quoteReplacement(
                            matchResult.group(1) + mask + matchResult.group(3));
                }
                return java.util.regex.Matcher.quoteReplacement(matchResult.group(1) + mask);
            });
        }
        for (Pattern p : kvPatterns) {
            result = p.matcher(result).replaceAll(matchResult ->
                    java.util.regex.Matcher.quoteReplacement(matchResult.group(1) + mask));
        }
        for (Pattern p : additionalPatterns) {
            result = p.matcher(result).replaceAll(matchResult -> {
                if (matchResult.groupCount() >= 1 && matchResult.group(1) != null) {
                    String full = matchResult.group();
                    String captured = matchResult.group(1);
                    return java.util.regex.Matcher.quoteReplacement(
                            full.replace(captured, mask));
                }
                return java.util.regex.Matcher.quoteReplacement(mask);
            });
        }
        return result;
    }

    @Override
    public String maskHeader(String headerName, String value) {
        if (headerName == null || value == null) {
            return value;
        }
        if (sensitiveHeadersLower.contains(headerName.toLowerCase(Locale.ROOT))) {
            return mask;
        }
        return value;
    }
}
