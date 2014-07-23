/**
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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Encoder that builds Steno formatted log messages.
 * <p/>
 * This encoder accepts logging events in various formats and keys off the existence of an slf4j Marker added to the log
 * event to determine the format of the arguments.
 * <p/>
 * For logging events that don't have an associated Marker a standard Steno wrapper will be created with the string
 * logging message added as a 'message' field in the 'data' object.
 * Example Output:
 * <pre>
 *     <code>
 *         {"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"message":"log message"},"context":{"thread_id":"thread"},"id":"oRw59PrARvatGNC7fiWw4A"}
 *     </code>
 * </pre>
 * <p/>
 * For logging events that have the {@link com.arpnetworking.logback.StenoMarker#ARRAY_MARKER} Marker the logging event is
 * expected to contain the name of the event as the 'message', with the first argument being a String array (String[])
 * containing the keys to be added to the 'data' object and the second argument being an Object array (Object[])
 * containing the values for each key.
 * Example logger call:
 * <pre>
 *     <code>
 *         log.info(StenoMarker.ARRAY_MARKER, "log", new String[] {"key1","key2"}, new Object[] {1234, "foo"});
 *     </code>
 * </pre>
 * Example Output:
 * <pre>
 *     <code>
 *         {"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"thread_id":"thread"},"id":"oRw59PrARvatGNC7fiWw4A"}
 *     </code>
 * </pre>
 * <p/>
 * For logging events that have the {@link com.arpnetworking.logback.StenoMarker#JSON_MARKER} Marker the logging event is
 * expected to contain the name of the event as the 'message', with the first argument being the name of the field
 * inside the 'data' object to output and the second argument being the json string to output for that field.
 * Example logger call:
 * <pre>
 *     <code>
 *         log.info(StenoMarker.JSON_MARKER, "log", "json", "{\"key\":\"value\"}");
 *     </code>
 * </pre>
 * Example Output:
 * <pre>
 *     <code>
 *         {"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"json":{"key":"value"}},"context":{"thread_id":"thread"},"id":"oRw59PrARvatGNC7fiWw4A"}
 *     </code>
 * </pre>
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public class StenoEncoder extends BaseLoggingEncoder {
    private static final int UUID_LENGTH_IN_BYTES = 16;
    private static final String STANDARD_LOG_EVENT_NAME = "log";
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime().withZoneUTC();

    private final JsonFactory jsonFactory;

    /**
     * Public constructor.
     */
    public StenoEncoder() {
        this(JSON_FACTORY);
    }

    /* package private */ StenoEncoder(final JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildStandardMessage(final ILoggingEvent event) {
        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, STANDARD_LOG_EVENT_NAME, jsonGenerator);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            jsonGenerator.writeObjectField("message", event.getFormattedMessage());
            jsonGenerator.writeEndObject(); // End 'data' field

            // Output throwable
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, STANDARD_LOG_EVENT_NAME, jsonGenerator);
        } catch (IOException e) {
            return "Unknown exception: " + e.getMessage();
        }

        return jsonWriter.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildJsonMessage(
        final ILoggingEvent event,
        final String eventName,
        final String jsonKey,
        final String json) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            jsonGenerator.writeFieldName(jsonKey);
            jsonGenerator.writeRaw(":");
            jsonGenerator.writeRaw(json);
            jsonGenerator.writeEndObject(); // End 'data' field

            // Output throwable
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, eventName, jsonGenerator);
        } catch (IOException e) {
            return "Unknown exception: " + e.getMessage();
        }

        return jsonWriter.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildArrayMessage(
        final ILoggingEvent event,
        final String eventName,
        final String[] keys,
        final Object[] values) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            final int argsLength = values == null ? 0 : values.length;
            if (keys != null) {
                for (int i = 0; i < keys.length; i++) {
                    if (i >= argsLength) {
                        jsonGenerator.writeObjectField(keys[i], null);
                    } else if (isSimpleType(values[i])) {
                        jsonGenerator.writeObjectField(keys[i], values[i]);
                    } else {
                        jsonGenerator.writeObjectField(keys[i], values[i].toString());
                    }
                }
            }
            jsonGenerator.writeEndObject(); // End 'data' field

            // Output throwable
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, eventName, jsonGenerator);
        } catch (final IOException e) {
            return "Unknown exception: " + e.getMessage();
        }

        return jsonWriter.toString();
    }

    /**
     * Start writing the Steno JSON wrapper.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @throws IOException If writing JSON fails.
     */
    protected void startStenoWrapper(
        final ILoggingEvent event,
        final String eventName,
        final JsonGenerator jsonGenerator)
        throws IOException {

        final StenoLevel level = StenoLevel.findByLogbackLevel(event.getLevel());
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("time", ISO_DATE_TIME_FORMATTER.print(new DateTime(event.getTimeStamp())));
        jsonGenerator.writeObjectField("name", eventName);
        jsonGenerator.writeObjectField("level", level.name());
    }

    /**
     * Complete writing the Steno JSON wrapper.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @throws IOException If writing JSON fails.
     */
    protected void endStenoWrapper(
        final ILoggingEvent event,
        final String eventName,
        final JsonGenerator jsonGenerator)
        throws IOException {

        jsonGenerator.writeObjectFieldStart("context");
        jsonGenerator.writeObjectField("threadId", event.getThreadName());
        jsonGenerator.writeEndObject(); // End 'context' field
        jsonGenerator.writeObjectField("id", createId());
        jsonGenerator.writeEndObject(); // End log message
        jsonGenerator.writeRaw('\n');
        jsonGenerator.flush();
    }

    /**
     * Write a <code>Throwable</code> via <code>IThrowableProxy</code> as JSON.
     *
     * @param throwableProxy Throwable to serialize
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @throws IOException If writing JSON fails.
     */
    protected void writeThrowable(
        final IThrowableProxy throwableProxy,
        final JsonGenerator jsonGenerator)
        throws IOException {

        if (throwableProxy != null) {
            jsonGenerator.writeObjectFieldStart("exception");
            serializeThrowable(throwableProxy, jsonGenerator);
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * This function assumes the field object has already been started for this throwable, this only fills in
     * the fields in the 'exception' or equivalent object and does not create the field in the containing object.
     *
     * @param throwableProxy Throwable to serialize
     * @param jsonGenerator <code>JsonGenerator</code> instance after exception object is started
     * @throws IOException If writing the <code>Throwable</code> as JSON fails.
     */
    protected void serializeThrowable(
        final IThrowableProxy throwableProxy,
        final JsonGenerator jsonGenerator)
        throws IOException {

        jsonGenerator.writeObjectField("type", throwableProxy.getClassName());
        jsonGenerator.writeObjectField("message", throwableProxy.getMessage());
        jsonGenerator.writeArrayFieldStart("backtrace");
        for (StackTraceElementProxy ste : throwableProxy.getStackTraceElementProxyArray()) {
            jsonGenerator.writeString(ste.toString());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeObjectFieldStart("data");
        if (throwableProxy.getSuppressed() != null) {
            jsonGenerator.writeArrayFieldStart("suppressed");
            for (IThrowableProxy suppressed : throwableProxy.getSuppressed()) {
                jsonGenerator.writeStartObject();
                serializeThrowable(suppressed, jsonGenerator);
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
        if (throwableProxy.getCause() != null) {
            jsonGenerator.writeObjectFieldStart("cause");
            serializeThrowable(throwableProxy.getCause(), jsonGenerator);
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndObject();
    }

    /**
     * Create a Steno compatible identifier.
     *
     * @return New identifier as a <code>String</code>.
     */
    protected String createId() {
        final UUID uuid = UUID.randomUUID();
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[UUID_LENGTH_IN_BYTES]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(buffer.array());
    }

    /* package private */ boolean isSimpleType(final Object obj) {
        if (obj == null) {
            return true;
        }

        final Class<?> objClass = obj.getClass();
        if (String.class.isAssignableFrom(objClass)) {
            return true;
        }

        if (Number.class.isAssignableFrom(objClass)) {
            return true;
        }

        if (Boolean.class.isAssignableFrom(objClass)) {
            return true;
        }

        return false;
    }

    /**
     * Log levels used by Steno.
     */
    protected enum StenoLevel {
        debug(Level.DEBUG, Level.TRACE),
        info(Level.INFO),
        warn(Level.WARN),
        crit(Level.ERROR);

        private final Level[] logbackLevels;
        private static final Map<Level, StenoLevel> LOGBACK_LEVEL_MAP = new HashMap<>();

        static {
            for (StenoLevel stenoLevel : values()) {
                for (Level logbackLevel : stenoLevel.logbackLevels) {
                    LOGBACK_LEVEL_MAP.put(logbackLevel, stenoLevel);
                }
            }
        }

        private StenoLevel(final Level... logbackLevels) {
            this.logbackLevels = logbackLevels;
        }

        /**
         * Find the <code>StenoLevel</code> corresponding to the Logback <code>Level</code>.
         *
         * @param logbackLevel The Logback <code>Level</code> to map to a <code>StenoLevel</code>.
         * @return The matching <code>StenoLevel</code> or null if one is not found.
         */
        public static StenoLevel findByLogbackLevel(final Level logbackLevel) {
            return LOGBACK_LEVEL_MAP.get(logbackLevel);
        }
    }
}
