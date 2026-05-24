package com.company.platform.logging.masking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.company.platform.logging.config.LoggingProperties;

class RegexSensitiveDataMaskerTest {

    private final RegexSensitiveDataMasker masker =
            new RegexSensitiveDataMasker(new LoggingProperties.Masking());

    @Test
    void masksJsonStringValuesForKnownFields() {
        String input = "{\"username\":\"alice\",\"password\":\"hunter2\"}";
        assertThat(masker.mask(input))
                .isEqualTo("{\"username\":\"alice\",\"password\":\"****\"}");
    }

    @Test
    void masksJsonNumericValuesForKnownFields() {
        String input = "{\"accountNumber\":1234567890}";
        assertThat(masker.mask(input))
                .isEqualTo("{\"accountNumber\":****}");
    }

    @Test
    void masksFormStyleValues() {
        String input = "user=alice&password=hunter2&remember=true";
        assertThat(masker.mask(input))
                .isEqualTo("user=alice&password=****&remember=true");
    }

    @Test
    void masksCaseInsensitively() {
        String input = "{\"Password\":\"hunter2\",\"TOKEN\":\"abc\"}";
        assertThat(masker.mask(input))
                .isEqualTo("{\"Password\":\"****\",\"TOKEN\":\"****\"}");
    }

    @Test
    void leavesNonSensitiveFieldsUntouched() {
        String input = "{\"customerId\":\"alice\",\"amount\":1234}";
        assertThat(masker.mask(input)).isEqualTo(input);
    }

    @Test
    void maskHeaderReplacesSensitiveHeader() {
        assertThat(masker.maskHeader("Authorization", "Bearer xyz")).isEqualTo("****");
        assertThat(masker.maskHeader("authorization", "Bearer xyz")).isEqualTo("****");
    }

    @Test
    void maskHeaderPassesThroughOtherHeaders() {
        assertThat(masker.maskHeader("X-Request-Id", "abc")).isEqualTo("abc");
    }

    @Test
    void appliesAdditionalUserDefinedPatterns() {
        LoggingProperties.Masking cfg = new LoggingProperties.Masking();
        cfg.setAdditionalPatterns(List.of("(?i)customSecret=([^\\s]+)"));
        RegexSensitiveDataMasker custom = new RegexSensitiveDataMasker(cfg);
        assertThat(custom.mask("note customSecret=abc123 trailing")).contains("****");
        assertThat(custom.mask("note customSecret=abc123 trailing")).doesNotContain("abc123");
    }

    @Test
    void handlesNullAndEmpty() {
        assertThat(masker.mask(null)).isNull();
        assertThat(masker.mask("")).isEmpty();
    }
}
