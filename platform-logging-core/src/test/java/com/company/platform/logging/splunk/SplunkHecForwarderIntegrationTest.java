// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.splunk;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Placeholder for an integration test that forwards a structured log line to a real
 * Splunk HTTP Event Collector endpoint and verifies indexed presence via the search API.
 *
 * <p>Requires environment variables {@code SPLUNK_HEC_URL} and {@code SPLUNK_HEC_TOKEN}
 * and is intentionally disabled by default — enable via {@code -Dgroups=integration} in CI
 * pipelines that provision a Splunk instance.
 */
@Disabled("Requires a live Splunk HEC endpoint; enable in CI integration profile.")
class SplunkHecForwarderIntegrationTest {

    @Test
    void postsStructuredLogAndVerifiesIndexed() {
        // TODO: assemble JSON envelope, POST to /services/collector, search by correlationId
    }
}
