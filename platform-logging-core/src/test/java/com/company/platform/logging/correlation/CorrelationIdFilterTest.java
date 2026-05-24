// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.correlation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.company.platform.logging.config.LoggingProperties;

class CorrelationIdFilterTest {

    private final LoggingProperties.Correlation config = new LoggingProperties.Correlation();
    private final CorrelationIdFilter filter = new CorrelationIdFilter(config);

    @Test
    void propagatesIncomingHeaderToMdcAndResponse() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(config.getHeaderName(), "abc-123");
        MockHttpServletResponse res = new MockHttpServletResponse();

        AtomicReference<String> insideChain = new AtomicReference<>();
        FilterChain chain = (r, s) -> insideChain.set(MDC.get(config.getMdcKey()));

        filter.doFilter(req, res, chain);

        assertThat(insideChain.get()).isEqualTo("abc-123");
        assertThat(res.getHeader(config.getHeaderName())).isEqualTo("abc-123");
    }

    @Test
    void generatesIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertThat(res.getHeader(config.getHeaderName())).isNotBlank();
    }

    @Test
    void doesNotGenerateWhenDisabled() throws Exception {
        config.setGenerateIfMissing(false);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertThat(res.getHeader(config.getHeaderName())).isNull();
    }

    @Test
    void clearsMdcAfterRequest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(config.getHeaderName(), "x");

        filter.doFilter(req, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(MDC.get(config.getMdcKey())).isNull();
    }

    @Test
    void clearsMdcEvenWhenChainThrows() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(config.getHeaderName(), "x");
        FilterChain chain = Mockito.mock(FilterChain.class);
        doAnswer(invocation -> {
            throw new RuntimeException("boom");
        }).when(chain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        try {
            filter.doFilter(req, new MockHttpServletResponse(), chain);
        } catch (RuntimeException ignored) {
            // expected
        }
        assertThat(MDC.get(config.getMdcKey())).isNull();
    }
}
