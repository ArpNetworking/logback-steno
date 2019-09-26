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

/**
 * Wraps a {@link org.slf4j.Logger} instance in a {@link com.arpnetwoprking.steno.Logger} instance.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class TestLoggerFactory {

    /**
     * Return a Steno {@link Logger} around a {@link org.slf4j.Logger} instance.
     *
     * @param logger The  {@link org.slf4j.Logger} instance.
     * @return Steno {@link Logger} instance.
     */
    public static Logger getLogger(final org.slf4j.Logger logger) {
        return new Logger(logger);
    }

    private TestLoggerFactory() {}
}
