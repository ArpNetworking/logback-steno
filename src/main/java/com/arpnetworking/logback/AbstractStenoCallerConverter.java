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
package com.arpnetworking.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Abstract Steno caller converter.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.14.0
 */
public abstract class AbstractStenoCallerConverter extends ClassicConverter {

    /**
     * Retrieve the relevant caller data adjusted for Steno logger wrapping.
     *
     * @param loggingEvent The <code>ILoggingEvent</code> instance.
     * @return The relevant caller data adjusted for Steno logger wrapping.
     */
    protected StackTraceElement getCallerData(final ILoggingEvent loggingEvent) {
        final StackTraceElement[] callerData = loggingEvent.getCallerData();
        if (callerData != null) {
            for (int i = 0; i < callerData.length; ++i) {
                final String callerClassName = callerData[i].getClassName();
                if (!callerClassName.startsWith(STENO_CLASS_NAME_PREFIX)) {
                    return callerData[i];
                }
            }
        }
        return null;
    }

    private static final String STENO_CLASS_NAME_PREFIX = "com.arpnetworking.steno.";
}
