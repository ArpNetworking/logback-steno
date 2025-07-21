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
package com.arpnetworking.logback.serialization.steno;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arpnetworking.logback.StenoEncoder;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import javax.annotation.Nullable;

/**
 * Serialization strategy for JSON array based message specifications.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 * @since 1.3.1
 */
public class ArrayOfJsonSerialziationStrategy implements Serializable {

    /**
     * Public constructor.
     *
     * @param encoder Instance of {@link StenoEncoder}.
     * @param jsonFactory Instance of {@link JsonFactory}.
     * @param objectMapper Instance of {@link ObjectMapper}.
     */
    public ArrayOfJsonSerialziationStrategy(
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
     * @param jsonValues The message json values.
     * @return Serialization of message as a {@link String}.
     * @throws IOException Serialization may throw any {@link IOException}.
     */
    public String serialize(
            final ILoggingEvent event,
            final String eventName,
            @Nullable final String[] keys,
            @Nullable final String[] jsonValues)
            throws IOException {
        final StringWriter jsonWriter = new StringWriter();
        final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);

        // Start wrapper
        StenoSerializationHelper.startStenoWrapper(event, eventName, jsonGenerator, _objectMapper);

        // Write event data
        jsonGenerator.writeObjectFieldStart("data");
        final int argsLength = jsonValues == null ? 0 : jsonValues.length;
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                if (i >= argsLength) {
                    jsonGenerator.writeObjectField(keys[i], null);
                } else {
                    jsonGenerator.writeFieldName(keys[i]);
                    jsonGenerator.writeRawValue(jsonValues[i]);
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

    private static final long serialVersionUID = 7573804802842216301L;
}
