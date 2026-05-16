package com.company.platform.logging.correlation;

import java.io.IOException;

import com.company.platform.logging.config.LoggingProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads or generates a correlation ID per request, binds it to MDC, and optionally echoes
 * it back on the response. Runs at the highest precedence so downstream filters and
 * application code observe a populated MDC.
 */
public class CorrelationIdFilter extends OncePerRequestFilter implements Ordered {

    private final LoggingProperties.Correlation config;

    public CorrelationIdFilter(LoggingProperties.Correlation config) {
        this.config = config;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String headerName = config.getHeaderName();
        String mdcKey = config.getMdcKey();
        String correlationId = request.getHeader(headerName);

        if ((correlationId == null || correlationId.isBlank()) && config.isGenerateIfMissing()) {
            correlationId = CorrelationContext.generate();
        }

        try {
            CorrelationContext.set(mdcKey, correlationId);
            if (config.isIncludeInResponse() && correlationId != null) {
                response.setHeader(headerName, correlationId);
            }
            filterChain.doFilter(request, response);
        } finally {
            CorrelationContext.clear(mdcKey);
        }
    }
}
