/*
 * Copyright 2016 Ville Koskela
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
package com.arpnetworking.logback.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Jackson mix-in for <code>Throwable</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.13.1
 */
public interface ThrowableMixIn {

    /**
     * Suppress automatic serialize of message field.
     *
     * @return The message.
     */
    @JsonIgnore
    String getMessage();

    /**
     * Suppress automatic serialize of localized message field.
     *
     * @return The localized message.
     */
    @JsonIgnore
    String getLocalizedMessage();

    /**
     * Suppress automatic serialize of cause field.
     *
     * @return The cause.
     */
    @JsonIgnore
    Throwable getCause();

    /**
     * Suppress automatic serialize of stack trace field.
     *
     * @return The stack trace.
     */
    @JsonIgnore
    StackTraceElement[] getStackTrace();

    /**
     * Suppress automatic serialize of suppressed field.
     *
     * @return The suppressed.
     */
    @JsonIgnore
    Throwable[] getSuppressed();
}
