package com.company.platform.logging.api;

import java.util.Map;

import com.company.platform.logging.masking.SensitiveDataMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Optional convenience facade. SLF4J is the primary API and remains fully supported
 * for direct use; this wrapper exists for callers who want masking applied at the call
 * site or who want to push a transient MDC scope without managing try/finally manually.
 *
 * <p>Do not use this for hot-path logging — prefer SLF4J's parameterised methods so
 * format strings are only resolved when the level is enabled.
 */
public final class PlatformLogger {

    private final Logger delegate;
    private final SensitiveDataMasker masker;

    private PlatformLogger(Logger delegate, SensitiveDataMasker masker) {
        this.delegate = delegate;
        this.masker = masker;
    }

    public static PlatformLogger forClass(Class<?> type, SensitiveDataMasker masker) {
        return new PlatformLogger(LoggerFactory.getLogger(type), masker);
    }

    public Logger slf4j() {
        return delegate;
    }

    public void infoMasked(String message) {
        if (delegate.isInfoEnabled()) {
            delegate.info(masker.mask(message));
        }
    }

    public void warnMasked(String message) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(masker.mask(message));
        }
    }

    public void errorMasked(String message, Throwable throwable) {
        if (delegate.isErrorEnabled()) {
            delegate.error(masker.mask(message), throwable);
        }
    }

    /**
     * Run {@code task} with {@code extra} added to MDC, restoring previous values when done.
     */
    public void withMdc(Map<String, String> extra, Runnable task) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            if (extra != null) {
                extra.forEach(MDC::put);
            }
            task.run();
        } finally {
            if (previous == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(previous);
            }
        }
    }
}
