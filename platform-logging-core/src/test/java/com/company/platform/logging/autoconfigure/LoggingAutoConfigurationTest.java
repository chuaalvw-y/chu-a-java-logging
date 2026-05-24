// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import com.company.platform.logging.config.LoggingProperties;
import com.company.platform.logging.correlation.CorrelationIdFilter;
import com.company.platform.logging.masking.SensitiveDataMasker;
import com.company.platform.logging.metadata.LoggingMetadataInitializer;
import com.company.platform.logging.web.RequestLoggingFilter;

class LoggingAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

    @Test
    void registersAllBeansByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(LoggingProperties.class);
            assertThat(context).hasSingleBean(SensitiveDataMasker.class);
            assertThat(context).hasSingleBean(CorrelationIdFilter.class);
            assertThat(context).hasSingleBean(RequestLoggingFilter.class);
            assertThat(context).hasSingleBean(LoggingMetadataInitializer.class);
        });
    }

    @Test
    void disablingMasterSwitchSkipsAllBeans() {
        runner.withPropertyValues("platform.logging.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(SensitiveDataMasker.class);
            assertThat(context).doesNotHaveBean(CorrelationIdFilter.class);
            assertThat(context).doesNotHaveBean(RequestLoggingFilter.class);
        });
    }

    @Test
    void disablingCorrelationOnlySkipsFilter() {
        runner.withPropertyValues("platform.logging.correlation.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(CorrelationIdFilter.class);
            assertThat(context).hasSingleBean(SensitiveDataMasker.class);
            assertThat(context).hasSingleBean(RequestLoggingFilter.class);
        });
    }
}
