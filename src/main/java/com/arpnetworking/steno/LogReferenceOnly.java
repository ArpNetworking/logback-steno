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

import com.arpnetworking.logback.annotations.LogValue;

/**
 * Log an <code>Object</code> only with its reference id and class. This is
 * useful for logging instances of classes that are not naturally serializable
 * by Jackson.
 *
 * @since 1.7.0
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class LogReferenceOnly {

    /**
     * Wrap an <code>Object</code> such that its Steno logged form is only its
     * reference id and class.
     *
     * @since 1.7.0
     * @param object The <code>Object</code> to wrap.
     * @return Reference only wrapped instance.
     */
    public static LogReferenceOnly of(final Object object) {
        return new LogReferenceOnly(object);
    }

    /**
     * Generate a Steno log compatible representation.
     *
     * @since 1.7.0
     * @return Steno log compatible representation.
     */
    @LogValue
    public Object toLogValue() {
        return LogValueMapFactory.builder(_object).build();
    }

    @Override
    public String toString() {
        return "{id=" + Integer.toHexString(System.identityHashCode(_object))
                + ", class=" + _object.getClass().getName() + "}";
    }

    private LogReferenceOnly(final Object object) {
        _object = object;
    }

    private final Object _object;
}
