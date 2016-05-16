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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;

/**
 * Serialization strategy for JSON map based message specifications.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.3.1
 */
public class MapOfJsonSerialziationStrategy implements Serializable {

    /**
     * Public constructor.
     *
     * @param encoder Instance of <code>StenoEncoder</code>.
     * @param jsonFactory Instance of <code>JsonFactory</code>.
     * @param objectMapper Instance of <code>ObjectMapper</code>.
     */
    public MapOfJsonSerialziationStrategy(
            final StenoEncoder encoder,
            final JsonFactory jsonFactory,
            final ObjectMapper objectMapper) {
        _encoder = encoder;
        _jsonFactory = jsonFactory;
        _objectMapper = objectMapper;
    }

    /**
     * Serialize an event.
     *
     * @param event The event.
     * @param eventName The event name.
     * @param map The message key to json-value pairs.
     * @return Serialization of message as a <code>String</code>.
     * @throws Exception Serialization may throw any <code>Exception</code>.
     */
    public String serialize(
            final ILoggingEvent event,
            final String eventName,
            final Map<String, String> map)
            throws Exception {
        final StringWriter jsonWriter = new StringWriter();
        final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);

        // Start wrapper
        StenoSerializationHelper.startStenoWrapper(event, eventName, jsonGenerator, _objectMapper);

        // Write event data
        jsonGenerator.writeObjectFieldStart("data");
        if (map != null) {
            for (final Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    jsonGenerator.writeObjectField(entry.getKey(), null);
                } else {
                    jsonGenerator.writeFieldName(entry.getKey());
                    jsonGenerator.writeRawValue(entry.getValue());
                }
            }
        }
        jsonGenerator.writeEndObject(); // End 'data' field

        // Output throwable
        StenoSerializationHelper.writeThrowable(event.getThrowableProxy(), jsonGenerator, _objectMapper);

        // End wrapper
        StenoSerializationHelper.endStenoWrapper(event, eventName, jsonGenerator, _objectMapper, _encoder);

        return jsonWriter.toString();
    }

    private final StenoEncoder _encoder;
    private final JsonFactory _jsonFactory;
    private final ObjectMapper _objectMapper;

    private static final long serialVersionUID = 6956084087076666267L;
}
