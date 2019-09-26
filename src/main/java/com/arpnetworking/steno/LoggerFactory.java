/*
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

import java.time.Duration;

/**
 * Factory class creates instances of {@link Logger}. To include the
 * context class or name in the context block of the Steno encoder's
 * output remember to enable context logger injection via:
 *
 * {@link com.arpnetworking.logback.StenoEncoder#setInjectContextLogger}
 *
 * @since 1.3.0
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class LoggerFactory {

    /**
     * Return a Steno {@link Logger} for a context class.
     *
     * @param clazz The {@link Logger} context class.
     * @return Steno {@link Logger} instance.
     */
    public static Logger getLogger(final Class<?> clazz) {
        return new Logger(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    /**
     * Return a rate limited Steno {@link Logger} for a context class.
     *
     * @param clazz The {@link Logger} context class.
     * @param duration Minimum time between log message output.
     * @return Steno {@link Logger} instance.
     */
    public static Logger getRateLimitLogger(final Class<?> clazz, final Duration duration) {
        return new RateLimitLogger(org.slf4j.LoggerFactory.getLogger(clazz), duration);
    }

    /**
     * Return a Steno {@link Logger} for a context name.
     *
     * @param name The {@link Logger} context name.
     * @return Steno {@link Logger} instance.
     */
    public static Logger getLogger(final String name) {
        return new Logger(org.slf4j.LoggerFactory.getLogger(name));
    }

    /**
     * Return a rate limited Steno {@link Logger} for a context name.
     *
     * @param name The {@link Logger} context name.
     * @param duration Minimum time between log message output.
     * @return Steno {@link Logger} instance.
     */
    public static Logger getRateLimitLogger(final String name, final Duration duration) {
        return new RateLimitLogger(org.slf4j.LoggerFactory.getLogger(name), duration);
    }

    /**
     * Return a Steno {@link Logger} for an already instantiated {@link org.slf4j.Logger} instance.
     *
     * @param logger The {@link org.slf4j.Logger} instance.
     * @return Steno {@link Logger} instance.
     */
    public static Logger getLogger(final org.slf4j.Logger logger) {
        return new Logger(logger);
    }

    /**
     * Return a rate limited Steno {@link Logger} for an already instantiated {@link org.slf4j.Logger}
     * instance.
     *
     * @param logger The {@link org.slf4j.Logger} instance.
     * @param duration Minimum time between log message output.
     * @return Steno {@link Logger} instance.
     */
    public static Logger getRateLimitLogger(final org.slf4j.Logger logger, final Duration duration) {
        return new RateLimitLogger(logger, duration);
    }

    private LoggerFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
