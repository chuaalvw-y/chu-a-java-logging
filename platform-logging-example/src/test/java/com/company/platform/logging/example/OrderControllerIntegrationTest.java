package com.company.platform.logging.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * End-to-end test against the real Tomcat container so the full filter chain (correlation
 * + request logging) actually runs. Uses the JDK HTTP client to avoid coupling to whatever
 * Spring testing utility happens to ship in this Boot release.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void getReturnsOrderAndEchoesCorrelationHeader() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/orders/abc-123"))
                .header("X-Correlation-Id", "trace-xyz")
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Correlation-Id")).contains("trace-xyz");
        assertThat(response.body()).contains("abc-123");
    }

    @Test
    void generatesCorrelationIdWhenMissing() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/orders/abc-123"))
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Correlation-Id")).isPresent();
        assertThat(response.headers().firstValue("X-Correlation-Id").orElseThrow()).isNotBlank();
    }
}
