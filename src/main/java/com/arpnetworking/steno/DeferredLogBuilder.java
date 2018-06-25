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
 * Interface for classes which assemble log messages where the actual logging
 * is deferred.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public interface DeferredLogBuilder {

    /**
     * Set the event.
     *
     * @since 1.3.0
     *
     * @param value The event.
     * @return This instance as {@code <T>}.
     */
    DeferredLogBuilder setEvent(String value);

    /**
     * Set the message.
     *
     * @since 1.3.0
     *
     * @param value The message.
     * @return This instance as {@code <T>}.
     */
    DeferredLogBuilder setMessage(String value);

    /**
     * Set the exception (<code>Throwable</code>).
     *
     * @since 1.3.0
     *
     * @param value The exception (<code>Throwable</code>).
     * @return This instance as {@code <T>}.
     */
    DeferredLogBuilder setThrowable(Throwable value);

    /**
     * Add data key-value pair.
     *
     * @since 1.3.0
     *
     * @param name The key.
     * @param value The value.
     * @return This instance as {@code <T>}.
     */
    DeferredLogBuilder addData(String name, Object value);

    /**
     * Add context key-value pair.
     *
     * @since 1.3.0
     *
     * @param name The key.
     * @param value The value.
     * @return This instance as {@code <T>}.
     */
    DeferredLogBuilder addContext(String name, Object value);
}
