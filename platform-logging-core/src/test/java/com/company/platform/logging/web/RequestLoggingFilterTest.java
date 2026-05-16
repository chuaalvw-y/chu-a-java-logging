package com.company.platform.logging.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.company.platform.logging.config.LoggingProperties;
import com.company.platform.logging.correlation.CorrelationIdFilter;
import com.company.platform.logging.masking.RegexSensitiveDataMasker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class RequestLoggingFilterTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger filterLogger;

    @BeforeEach
    void setUpAppender() {
        filterLogger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        filterLogger.addAppender(appender);
        filterLogger.setLevel(Level.INFO);
    }

    @AfterEach
    void tearDown() {
        filterLogger.detachAppender(appender);
    }

    private MockMvc mockMvc(LoggingProperties props) {
        return standaloneSetup(new TestController())
                .addFilters(
                        new CorrelationIdFilter(props.getCorrelation()),
                        new RequestLoggingFilter(props.getRequestLogging(),
                                new RegexSensitiveDataMasker(props.getMasking())))
                .build();
    }

    @Test
    void logsOneEntryPerRequest() throws Exception {
        mockMvc(new LoggingProperties())
                .perform(get("/test/ping"))
                .andExpect(status().isOk());

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(1);
        String message = events.get(0).getFormattedMessage();
        assertThat(message).contains("GET").contains("/test/ping").contains("status=200");
    }

    @Test
    void masksSensitiveJsonPayload() throws Exception {
        LoggingProperties props = new LoggingProperties();
        props.getRequestLogging().setIncludePayload(true);

        mockMvc(props)
                .perform(post("/test/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"hunter2\"}"))
                .andExpect(status().isOk());

        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).contains("\"password\":\"****\"");
        assertThat(message).doesNotContain("hunter2");
    }

    @Test
    void masksSensitiveHeadersWhenHeadersIncluded() throws Exception {
        LoggingProperties props = new LoggingProperties();
        props.getRequestLogging().setIncludeHeaders(true);

        mockMvc(props)
                .perform(get("/test/ping").header("Authorization", "Bearer abc123"))
                .andExpect(status().isOk());

        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).contains("****");
        assertThat(message).doesNotContain("Bearer abc123");
    }

    @Test
    void respectsExcludedPaths() throws Exception {
        LoggingProperties props = new LoggingProperties();
        props.getRequestLogging().setExcludedPaths(List.of("/test/**"));

        mockMvc(props).perform(get("/test/ping")).andExpect(status().isOk());

        assertThat(appender.list).isEmpty();
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @org.springframework.web.bind.annotation.GetMapping("/ping")
        String ping() {
            return "pong";
        }

        @PostMapping("/echo")
        String echo(@RequestBody String body) {
            return body;
        }
    }
}
