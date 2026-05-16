package com.company.platform.logging.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.company.platform.logging.config.LoggingProperties;
import com.company.platform.logging.masking.SensitiveDataMasker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Logs one structured line per HTTP request including method, path, status, duration, and
 * optionally headers / payloads. Sensitive values are routed through the configured
 * {@link SensitiveDataMasker} before reaching any appender.
 */
public class RequestLoggingFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final LoggingProperties.RequestLogging config;
    private final SensitiveDataMasker masker;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public RequestLoggingFilter(LoggingProperties.RequestLogging config, SensitiveDataMasker masker) {
        this.config = config;
        this.masker = masker;
    }

    @Override
    public int getOrder() {
        // After CorrelationIdFilter so its MDC is visible, but still early in the chain.
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> excluded = config.getExcludedPaths();
        if (excluded == null || excluded.isEmpty()) {
            return false;
        }
        for (String pattern : excluded) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean capturePayload = config.isIncludePayload();
        HttpServletRequest requestToUse = capturePayload ? new ContentCachingRequestWrapper(request) : request;
        HttpServletResponse responseToUse = capturePayload ? new ContentCachingResponseWrapper(response) : response;

        long start = System.nanoTime();
        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            emit(requestToUse, responseToUse, durationMs);
            if (responseToUse instanceof ContentCachingResponseWrapper wrapper) {
                wrapper.copyBodyToResponse();
            }
        }
    }

    private void emit(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", "http.request");
        fields.put("method", request.getMethod());
        fields.put("path", request.getRequestURI());
        if (config.isIncludeQueryString() && request.getQueryString() != null) {
            fields.put("query", masker.mask(request.getQueryString()));
        }
        fields.put("status", response.getStatus());
        fields.put("durationMs", durationMs);
        fields.put("remote", request.getRemoteAddr());

        if (config.isIncludeHeaders()) {
            fields.put("headers", collectHeaders(request));
        }
        if (config.isIncludePayload() && request instanceof ContentCachingRequestWrapper cached) {
            fields.put("requestBody", capturePayload(cached.getContentAsByteArray(), cached.getCharacterEncoding()));
        }
        if (config.isIncludePayload() && response instanceof ContentCachingResponseWrapper cached) {
            fields.put("responseBody", capturePayload(cached.getContentAsByteArray(), cached.getCharacterEncoding()));
        }

        log.info("http.request {}", fields);
    }

    private Map<String, String> collectHeaders(HttpServletRequest request) {
        Map<String, String> out = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            out.put(name, masker.maskHeader(name, request.getHeader(name)));
        }
        return out;
    }

    private String capturePayload(byte[] buf, String charset) {
        if (buf == null || buf.length == 0) {
            return "";
        }
        int length = Math.min(buf.length, config.getMaxPayloadLength());
        String encoding = (charset != null) ? charset : StandardCharsets.UTF_8.name();
        try {
            String raw = new String(buf, 0, length, encoding);
            String masked = masker.mask(raw);
            if (buf.length > length) {
                return masked + "...[truncated " + (buf.length - length) + " bytes]";
            }
            return masked;
        } catch (java.io.UnsupportedEncodingException e) {
            return "[unreadable payload: " + e.getMessage() + "]";
        }
    }
}
