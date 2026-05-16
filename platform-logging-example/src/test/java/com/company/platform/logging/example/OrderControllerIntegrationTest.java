package com.company.platform.logging.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void getReturnsOrderAndEchoesCorrelationHeader() throws Exception {
        MvcResult result = mvc.perform(get("/orders/abc-123")
                        .header("X-Correlation-Id", "trace-xyz"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "trace-xyz"))
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).contains("abc-123");
    }

    @Test
    void generatesCorrelationIdWhenMissing() throws Exception {
        mvc.perform(get("/orders/abc-123"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
