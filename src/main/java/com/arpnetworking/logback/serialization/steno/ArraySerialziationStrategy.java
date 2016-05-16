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

/**
 * Serialization strategy for array based message specifications.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.3.1
 */
public class ArraySerialziationStrategy implements Serializable {

    /**
     * Public constructor.
     *
     * @param encoder Instance of <code>StenoEncoder</code>.
     * @param jsonFactory Instance of <code>JsonFactory</code>.
     * @param objectMapper Instance of <code>ObjectMapper</code>.
     */
    public ArraySerialziationStrategy(
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
     * @param keys The message keys.
     * @param values The message values.
     * @return Serialization of message as a <code>String</code>.
     * @throws Exception Serialization may throw any <code>Exception</code>.
     */
    public String serialize(
            final ILoggingEvent event,
            final String eventName,
            final String[] keys,
            final Object[] values)
            throws Exception {

        final StringWriter jsonWriter = new StringWriter();
        final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);

        // Start wrapper
        StenoSerializationHelper.startStenoWrapper(event, eventName, jsonGenerator, _objectMapper);

        // Write event data
        jsonGenerator.writeObjectFieldStart("data");
        final int argsLength = values == null ? 0 : values.length;
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                if (i >= argsLength) {
                    jsonGenerator.writeObjectField(keys[i], null);
                } else if (StenoSerializationHelper.isSimpleType(values[i])) {
                    jsonGenerator.writeObjectField(keys[i], values[i]);
                } else {
                    jsonGenerator.writeFieldName(keys[i]);
                    _objectMapper.writeValue(
                            jsonGenerator,
                            values[i]);
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

    private static final long serialVersionUID = 309784279717760584L;
}
