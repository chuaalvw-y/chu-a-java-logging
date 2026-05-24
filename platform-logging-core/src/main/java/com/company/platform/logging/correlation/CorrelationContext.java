// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.correlation;

import java.util.UUID;

import org.slf4j.MDC;

/**
 * Thin convenience around SLF4J's {@link MDC} for correlation IDs.
 *
 * <p>This is a static utility because MDC itself is a thread-local global; wrapping it
 * in an injectable bean would add ceremony with no behavioural benefit.
 */
public final class CorrelationContext {

    public static final String DEFAULT_KEY = "correlationId";

    private CorrelationContext() {
    }

    /** Read the current correlation ID for this thread, or {@code null} when none is set. */
    public static String get(String mdcKey) {
        return MDC.get(mdcKey);
    }

    /** Bind a correlation ID to MDC. */
    public static void set(String mdcKey, String correlationId) {
        if (correlationId != null) {
            MDC.put(mdcKey, correlationId);
        }
    }

    /** Remove the binding — callers MUST invoke this at request completion to avoid leaking across reused threads. */
    public static void clear(String mdcKey) {
        MDC.remove(mdcKey);
    }

    /** Generate a fresh correlation ID. The format is implementation-defined (currently UUID v4). */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
