/**
 * Copyright 2015 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.steno;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Extension of Steno logger designed for use particularly with Steno encoder
 * while also providing log event rate limiting. This class is thread safe.
 *
 * @since 1.12.0
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
/* package private */ class RateLimitLogger extends Logger {

    /**
     * Log for a particular level using the <code>ARRAY_MARKER</code>.
     *
     * @param level The log event level.
     * @param event The log event name.
     * @param message The log event message.
     * @param dataKeys The array of data keys.
     * @param dataValues The array of data values.
     * @param throwable The <code>Throwable</code>.
     */
    @Override
    /* package private */ void log(
            final LogLevel level,
            final String event,
            final String message,
            final String[] dataKeys,
            final Object[] dataValues,
            final Throwable throwable) {

        if (shouldLog(level)) {
            final String[] augmentedDataKeys = Arrays.copyOf(dataKeys, dataKeys.length + 2);
            final Object[] augmentedDataValues = Arrays.copyOf(dataValues, dataValues.length + 2);
            augmentedDataKeys[augmentedDataKeys.length - 2] = "_skipped";
            augmentedDataKeys[augmentedDataKeys.length - 1] = "_lastLogTime";
            augmentedDataValues[augmentedDataValues.length - 2] = _skipped.getAndSet(0);
            augmentedDataValues[augmentedDataValues.length - 1] = _lastLogTime.getAndSet(_clock.instant());

            super.log(level, event, message, augmentedDataKeys, augmentedDataValues, throwable);
        }
    }

    /**
     * Log for a particular level using the <code>LISTS_MARKER</code>.
     *
     * @param level The log event level.
     * @param event The log event name.
     * @param dataKeys The <code>List</code> of data keys.
     * @param dataValues The <code>List</code> of data values.
     * @param contextKeys The <code>List</code> of context keys.
     * @param contextValues The <code>List</code> of context values.
     * @param throwable The <code>Throwable</code>.
     */
    @Override
    /* package private */ void log(
            final LogLevel level,
            final String event,
            final List<String> dataKeys,
            final List<Object> dataValues,
            final List<String> contextKeys,
            final List<Object> contextValues,
            final Throwable throwable) {

        if (shouldLog(level)) {
            dataKeys.add("_skipped");
            dataKeys.add("_lastLogTime");
            dataValues.add(_skipped.getAndSet(0));
            dataValues.add(_lastLogTime.getAndSet(_clock.instant()));

            super.log(level, event, dataKeys, dataValues, contextKeys, contextValues, throwable);
        }
    }

    /* package private */ RateLimitLogger(final org.slf4j.Logger slf4jLogger, final Duration duration) {
        this(slf4jLogger, duration, Clock.systemUTC());
    }

    private boolean shouldLog(final LogLevel level) {
        // Check this first to avoid counting unlogged messages against the logging rate
        if (!level.isEnabled(getSlf4jLogger())) {
            return false;
        }
        final Instant now = _clock.instant();
        if (_lastLogTime.get() != null) {
            if (!_lastLogTime.get().plus(_duration).isBefore(now)) {
                _skipped.incrementAndGet();
                return false;
            }
        }
        return true;
    }

    /* package private */ RateLimitLogger(final org.slf4j.Logger slf4jLogger, final Duration duration, final Clock clock) {
        super(slf4jLogger);
        _duration = duration;
        _clock = clock;
    }

    private final Duration _duration;
    private final Clock _clock;
    private AtomicReference<Instant> _lastLogTime = new AtomicReference<>();
    private AtomicInteger _skipped = new AtomicInteger(0);
}
