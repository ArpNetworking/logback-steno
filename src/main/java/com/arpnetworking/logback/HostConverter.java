/*
 * Copyright 2014 Groupon.com
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

import java.util.concurrent.atomic.AtomicReference;

/**
 * Generate the host name for a logging event. This implementation uses the local host name and therefore will not
 * work as expected if log events are serialized on a remote system.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.1.0
 */
public class HostConverter extends ClassicConverter {

    /**
     * Public constructor.
     */
    public HostConverter() {
        this(HostProvider.DEFAULT);
    }

    @Override
    public String convert(final ILoggingEvent event) {
        try {
            String result = this._hostName.get();
            if (result == null) {
                result = _provider.get();
                this._hostName.set(result);
            }
            return result;
            // CHECKSTYLE.OFF: IllegalCatch - Prevent all failures
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            return "<UNKNOWN>";
        }
    }

    /*package private*/ HostConverter(final HostProvider provider) {
        this._provider = provider;
    }

    private final HostProvider _provider;
    private final AtomicReference<String> _hostName = new AtomicReference<>();
}
