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
package com.arpnetworking.logback.serialization;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arpnetworking.logback.StenoEncoder;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Serialization strategy for standard message specification.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.3.1
 */
public class StandardSerializationStrategy extends BaseSerializationStrategy {

    /**
     * Public constructor.
     *
     * @param encoder Instance of <code>StenoEncoder</code>.
     * @param jsonFactory Instance of <code>JsonFactory</code>.
     * @param objectMapper Instance of <code>ObjectMapper</code>.
     */
    public StandardSerializationStrategy(
            final StenoEncoder encoder,
            final JsonFactory jsonFactory,
            final ObjectMapper objectMapper) {
        super(encoder);
        _jsonFactory = jsonFactory;
        _objectMapper = objectMapper;
    }

    /**
     * Serialize an event.
     *
     * @param event The event.
     * @param eventName The event name.
     * @return Serialization of message as a <code>String</code>.
     */
    public String serialize(
        final ILoggingEvent event,
        final String eventName) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator, _objectMapper);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            jsonGenerator.writeObjectField("message", event.getFormattedMessage());
            jsonGenerator.writeEndObject(); // End 'data' field

            // Output throwable
            writeThrowable(event.getThrowableProxy(), jsonGenerator, _objectMapper);

            // End wrapper
            endStenoWrapper(event, eventName, jsonGenerator, _objectMapper);
        } catch (final IOException e) {
            return "Unknown exception: " + e.getMessage();
        }

        return jsonWriter.toString();
    }

    private final JsonFactory _jsonFactory;
    private final ObjectMapper _objectMapper;
}