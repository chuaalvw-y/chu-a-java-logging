// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.masking;

/**
 * Replaces sensitive substrings inside a message with a fixed mask token.
 *
 * <p>Implementations must be thread-safe — a single instance is shared across the application.
 */
public interface SensitiveDataMasker {

    /**
     * @param input raw text that may contain sensitive values
     * @return text with sensitive values replaced; never {@code null} when {@code input} is non-null
     */
    String mask(String input);

    /**
     * @param headerName HTTP header name
     * @param value      header value
     * @return either the original value or the configured mask
     */
    String maskHeader(String headerName, String value);
}
