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

/**
 * Interface for classes which assemble log messages and log them.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public interface LogBuilder extends DeferredLogBuilder {

    /**
     * Set the event.
     *
     * @since 1.3.0
     *
     * @param value The event.
     * @return This instance as {@code <T>}.
     */
    @Override
    LogBuilder setEvent(String value);

    /**
     * Set the message.
     *
     * @since 1.3.0
     *
     * @param value The message.
     * @return This instance as {@code <T>}.
     */
    @Override
    LogBuilder setMessage(String value);

    /**
     * Set the exception (<code>Throwable</code>).
     *
     * @since 1.3.0
     *
     * @param value The exception (<code>Throwable</code>).
     * @return This instance as {@code <T>}.
     */
    @Override
    LogBuilder setThrowable(Throwable value);

    /**
     * Add data key-value pair.
     *
     * @since 1.3.0
     *
     * @param name The key.
     * @param value The value. See the README in the project root for the constraints on the value's type and the
     *              corresponding log content.
     * @return This instance as {@code <T>}.
     */
    @Override
    LogBuilder addData(String name, Object value);

    /**
     * Add context key-value pair.
     *
     * @since 1.3.0
     *
     * @param name The key.
     * @param value The value.
     * @return This instance as {@code <T>}.
     */
    @Override
    LogBuilder addContext(String name, Object value);

    /**
     * Log this message.
     *
     * @since 1.3.0
     */
    void log();
}
