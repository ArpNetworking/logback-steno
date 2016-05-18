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
package com.arpnetworking.logback.serialization.steno;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arpnetworking.logback.StenoEncoder;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

/**
 * Serialization strategy for object based message specifications.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.3.1
 */
public class ObjectSerialziationStrategy implements Serializable {

    /**
     * Public constructor.
     *
     * @param encoder Instance of <code>StenoEncoder</code>.
     * @param jsonFactory Instance of <code>JsonFactory</code>.
     * @param objectMapper Instance of <code>ObjectMapper</code>.
     */
    public ObjectSerialziationStrategy(
            final StenoEncoder encoder,
            final JsonFactory jsonFactory,
            final ObjectMapper objectMapper) {
        _objectMapper = objectMapper;
        _objectAsJsonStrategy = new ObjectAsJsonSerialziationStrategy(encoder, jsonFactory, objectMapper);
    }

    /**
     * Serialize an event.
     *
     * @param event The event.
     * @param eventName The event name.
     * @param data The message data <code>Object</code>.
     * @return Serialization of message as a <code>String</code>.
     * @throws Exception Serialization may throw any <code>Exception</code>.
     */
    public String serialize(
            final ILoggingEvent event,
            final String eventName,
            final Object data)
            throws Exception {
        final String jsonData = data == null ? null : _objectMapper.writeValueAsString(data);
        return _objectAsJsonStrategy.serialize(
                event,
                eventName,
                jsonData);
    }

    private final ObjectMapper _objectMapper;
    private final ObjectAsJsonSerialziationStrategy _objectAsJsonStrategy;

    private static final long serialVersionUID = 5931045066524060672L;
}
