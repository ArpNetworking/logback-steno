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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.FileOfCallerConverter;
import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.MethodOfCallerConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.arpnetworking.logback.HostConverter;
import com.arpnetworking.logback.ProcessConverter;
import com.arpnetworking.logback.StenoEncoder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Common methods for Steno serialization strategies.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.3.1
 */
/* package private */ abstract class BaseSerializationStrategy {

    /**
     * Protected constructor.
     *
     * @param encoder Instance of <code>StenoEncoder</code>.
     */
    protected BaseSerializationStrategy(final StenoEncoder encoder) {
        _encoder = encoder;
    }

    /**
     * Start writing the Steno JSON wrapper.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws java.io.IOException If writing JSON fails.
     */
    protected void startStenoWrapper(
            final ILoggingEvent event,
            final String eventName,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper)
            throws IOException {

        final StenoLevel level = StenoLevel.findByLogbackLevel(event.getLevel());
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("time",
                ISO_DATE_TIME_FORMATTER.print(new DateTime(event.getTimeStamp(), DateTimeZone.UTC)));
        jsonGenerator.writeObjectField("name", eventName);
        jsonGenerator.writeObjectField("level", level.name());
    }

    /**
     * Complete writing the Steno JSON wrapper.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws IOException If writing JSON fails.
     */
    protected void endStenoWrapper(
            final ILoggingEvent event,
            final String eventName,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper)
            throws IOException {
        endStenoWrapper(
                event,
                eventName,
                Collections.emptyList(),
                Collections.emptyList(),
                jsonGenerator,
                objectMapper);
    }

    /**
     * Complete writing the Steno JSON wrapper.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param contextKeys The <code>List</code> of context keys.
     * @param contextValues The <code>List</code> of context values.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws IOException If writing JSON fails.
     */
    // CS.OFF: NPathComplexity
    protected void endStenoWrapper(
            final ILoggingEvent event,
            final String eventName,
            final List<String> contextKeys,
            final List<Object> contextValues,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper)
            throws IOException {

        jsonGenerator.writeObjectFieldStart("context");
        if (_encoder.isInjectContextHost()) {
            jsonGenerator.writeStringField("host", HOST_CONVERTER.convert(event));
        }
        if (_encoder.isInjectContextProcess()) {
            jsonGenerator.writeStringField("processId", PROCESS_CONVERTER.convert(event));
        }
        if (_encoder.isInjectContextThread()) {
            jsonGenerator.writeStringField("threadId", THREAD_CONVERTER.convert(event));
        }
        if (_encoder.isInjectContextLogger()) {
            if (_encoder.isCompressLoggerName()) {
                jsonGenerator.writeStringField("logger", CONDENSED_LOGGER_CONVERTER.convert(event));
            } else {
                jsonGenerator.writeStringField("logger", LOGGER_CONVERTER.convert(event));
            }
        }
        if (_encoder.isInjectContextFile()) {
            jsonGenerator.writeStringField("file", FILE_CONVERTER.convert(event));
        }
        if (_encoder.isInjectContextClass()) {
            jsonGenerator.writeStringField("class", CLASS_CONVERTER.convert(event));
        }
        if (_encoder.isInjectContextMethod()) {
            jsonGenerator.writeStringField("method", METHOD_CONVERTER.convert(event));
        }
        if (_encoder.isInjectContextLine()) {
            jsonGenerator.writeStringField("line", LINE_CONVERTER.convert(event));
        }
        for (final String key : _encoder.getMdcProperties()) {
            final String value = event.getMDCPropertyMap().get(key);
            jsonGenerator.writeStringField(key, value);
        }
        writeKeyValuePairs(contextKeys, contextValues, jsonGenerator, objectMapper);
        jsonGenerator.writeEndObject(); // End 'context' field
        jsonGenerator.writeObjectField("id", createId());
        jsonGenerator.writeObjectField("version", "0");
        jsonGenerator.writeEndObject(); // End log message
        jsonGenerator.writeRaw('\n');
        jsonGenerator.flush();
    }

    /**
     * Write specified key-value pairs into the current block.
     *
     * @param keys The <code>List</code> of keys.
     * @param values The <code>List</code> of values.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws IOException If writing JSON fails.
     */
    protected void writeKeyValuePairs(
            final List<String> keys,
            final List<Object> values,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper)
            throws IOException {
        if (keys != null) {
            final int contextValuesLength = values == null ? 0 : values.size();
            for (int i = 0; i < keys.size(); ++i) {
                final String key = keys.get(i);
                if (i >= contextValuesLength) {
                    jsonGenerator.writeObjectField(key, null);
                } else {
                    final Object value = values.get(i);
                    if (isSimpleType(value)) {
                        jsonGenerator.writeObjectField(key, value);
                    } else {
                        jsonGenerator.writeFieldName(key);
                        objectMapper.writeValue(jsonGenerator, value);
                    }
                }
            }
        }
    }
    // CS.ON: NPathComplexity

    /**
     * Write a <code>Throwable</code> via <code>IThrowableProxy</code> as JSON.
     *
     * @param throwableProxy Throwable to serialize
     * @param jsonGenerator  <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws IOException If writing JSON fails.
     */
    protected void writeThrowable(
            final IThrowableProxy throwableProxy,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper)
            throws IOException {

        if (throwableProxy != null) {
            jsonGenerator.writeObjectFieldStart("exception");
            serializeThrowable(throwableProxy, jsonGenerator, objectMapper);
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * This function assumes the field object has already been started for this throwable, this only fills in
     * the fields in the 'exception' or equivalent object and does not create the field in the containing object.
     *
     * @param throwableProxy Throwable to serialize
     * @param jsonGenerator  <code>JsonGenerator</code> instance after exception object is started
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws IOException If writing the <code>Throwable</code> as JSON fails.
     */
    protected void serializeThrowable(
            final IThrowableProxy throwableProxy,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper)
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
                serializeThrowable(suppressed, jsonGenerator, objectMapper);
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
        if (throwableProxy.getCause() != null) {
            jsonGenerator.writeObjectFieldStart("cause");
            serializeThrowable(throwableProxy.getCause(), jsonGenerator, objectMapper);
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
        return UUID.randomUUID().toString();
    }

    /**
     * Determine if an object represents a primitive Json type. These include
     * instances of <code>Number</code>, <code>String</code> and <code>Boolean</code>.
     *
     * @param obj The <code>Object</code> to analyze.
     * @return <code>True</code> if and only if the <code>Object</code> is a simple type.
     */
    protected static boolean isSimpleType(final Object obj) {
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

    private final StenoEncoder _encoder;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime().withZoneUTC();
    private static final ClassicConverter HOST_CONVERTER = new HostConverter();
    private static final ClassicConverter PROCESS_CONVERTER = new ProcessConverter();
    private static final ClassicConverter THREAD_CONVERTER = new ThreadConverter();
    private static final ClassicConverter LOGGER_CONVERTER = new LoggerConverter();
    private static final ClassicConverter CONDENSED_LOGGER_CONVERTER = new LoggerConverter();
    private static final ClassicConverter FILE_CONVERTER = new FileOfCallerConverter();
    private static final ClassicConverter CLASS_CONVERTER = new ClassOfCallerConverter();
    private static final ClassicConverter METHOD_CONVERTER = new MethodOfCallerConverter();
    private static final ClassicConverter LINE_CONVERTER = new LineOfCallerConverter();

    static {
        CONDENSED_LOGGER_CONVERTER.setOptionList(Collections.singletonList("1"));

        HOST_CONVERTER.start();
        PROCESS_CONVERTER.start();
        THREAD_CONVERTER.start();
        LOGGER_CONVERTER.start();
        CONDENSED_LOGGER_CONVERTER.start();
        FILE_CONVERTER.start();
        CLASS_CONVERTER.start();
        METHOD_CONVERTER.start();
        LINE_CONVERTER.start();
    }

    /**
     * Log levels used by Steno.
     */
    protected enum StenoLevel {
        debug(Level.DEBUG, Level.TRACE),
        info(Level.INFO),
        warn(Level.WARN),
        crit(Level.ERROR);

        private final Level[] _logbackLevels;
        private static final Map<Level, StenoLevel> LOGBACK_LEVEL_MAP = new HashMap<>();

        static {
            for (StenoLevel stenoLevel : values()) {
                for (Level logbackLevel : stenoLevel._logbackLevels) {
                    LOGBACK_LEVEL_MAP.put(logbackLevel, stenoLevel);
                }
            }
        }

        private StenoLevel(final Level... logbackLevels) {
            _logbackLevels = logbackLevels;
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
