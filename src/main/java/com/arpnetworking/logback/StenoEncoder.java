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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.arpnetworking.logback.jackson.FilterForcingAnnotationIntrospector;
import com.arpnetworking.logback.jackson.RedactionFilter;

/**
 * Encoder that builds Steno formatted log messages.
 * <p/>
 * This encoder accepts logging events in various formats and keys off the existence of an slf4j Marker added to the log
 * event to determine the format of the arguments.
 * <p/>
 * For logging events that don't have an associated Marker a standard Steno wrapper will be created with the string
 * logging message added as a 'message' field in the 'data' object. In such a case the name defaults to "log" unless a
 * name is specifically configured on the encoder.
 * <p/>
 * <i>Example Output:</i>
 * <pre>
 *     <code>
 *         {"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"message":"log message"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
 *     </code>
 * </pre>
 * <p/>
 * For logging events that have the {@link com.arpnetworking.logback.StenoMarker#ARRAY_MARKER} Marker the logging event is
 * expected to contain the name of the event as the 'message', with the first argument being a String array (String[])
 * containing the keys to be added to the 'data' object and the second argument being an Object array (Object[])
 * containing the values for each key.
 * <p/>
 * <i>Example logger call:</i>
 * <pre>
 *     <code>
 *         log.info(StenoMarker.ARRAY_MARKER, "log", new String[] {"key1","key2"}, new Object[] {1234, "foo"});
 *     </code>
 * </pre>
 * Example Output:
 * <pre>
 *     <code>
 *         {"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
 *     </code>
 * </pre>
 * <p/>
 * For logging events that have the {@link com.arpnetworking.logback.StenoMarker#MAP_MARKER} Marker the logging event is
 * expected to contain the name of the event as the 'message', with the first argument being a map of key to value to be
 * included inside the 'data' object.
 * <p/>
 * <i>Example logger call:</i>
 * <pre>
 *     <code>
 *         log.info(StenoMarker.MAP_MARKER, "log", ImmutableMap.of("key1", 1234, "key2", "foo"));
 *     </code>
 * </pre>
 * <i>Example Output:</i>
 * <pre>
 *     <code>
 *         {"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
 *     </code>
 * </pre>
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class StenoEncoder extends BaseLoggingEncoder {
    private static final int UUID_LENGTH_IN_BYTES = 16;
    private static final boolean DEFAULT_REDACT_NULL = true;
    private static final String STANDARD_LOG_EVENT_NAME = "log";
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime().withZoneUTC();
    private static final ClassicConverter HOST_CONVERTER = new HostConverter();
    private static final ClassicConverter PROCESS_CONVERTER = new ProcessConverter();
    private static final ClassicConverter THREAD_CONVERTER = new ThreadConverter();
    private static final ClassicConverter LOGGER_CONVERTER = new LoggerConverter();
    private static final ClassicConverter FILE_CONVERTER = new FileOfCallerConverter();
    private static final ClassicConverter CLASS_CONVERTER = new ClassOfCallerConverter();
    private static final ClassicConverter METHOD_CONVERTER = new MethodOfCallerConverter();
    private static final ClassicConverter LINE_CONVERTER = new LineOfCallerConverter();

    private ObjectMapper objectMapper;
    private final JsonFactory jsonFactory;
    private String logEventName = STANDARD_LOG_EVENT_NAME;
    private boolean redactEnabled;
    private boolean redactNull = DEFAULT_REDACT_NULL;
    private boolean injectContextProcess = true;
    private boolean injectContextHost = true;
    private boolean injectContextThread = true;
    private boolean injectContextLogger = false;
    private boolean injectContextClass = false;
    private boolean injectContextFile = false;
    private boolean injectContextMethod = false;
    private boolean injectContextLine = false;

    /**
     * Public constructor.
     */
    public StenoEncoder() {
        // Each instance of StenoEncoder requires its own ObjectMapper instance
        // in order to apply the correct filtering settings to support redaction.
        this(JSON_FACTORY, new ObjectMapper());
    }

    /* package private */ StenoEncoder(final JsonFactory jsonFactory, final ObjectMapper objectMapper) {
        this.jsonFactory = jsonFactory;

        // Initialize object mapper;
        this.objectMapper = objectMapper;
        this.objectMapper.setAnnotationIntrospector(new FilterForcingAnnotationIntrospector());
        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!DEFAULT_REDACT_NULL));
        // Initialize this here based on the above code, if it was initialized at the declaration site then things
        // could get out of sync
        redactEnabled = true;
        this.objectMapper.setFilters(simpleFilterProvider);

        // Setup writing of Date/DateTime values
        this.objectMapper.registerModule(new JodaModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDateFormat(new ISO8601DateFormat());
    }

    /**
     * Determines if fields marked with the annotation @LogRedact will be redacted in log output when serializing
     * complex objects. If true then values for annotated fields/properties will be output as a string with the value
     * "<REDACTED>", otherwise the value will be output as serialized json using a Jackson object mapper. Default: true
     *
     * @return whether redacted fields or filtered
     *
     * @since 1.1.0
     */
    public boolean isRedactEnabled() {
        return redactEnabled;
    }

    /**
     * Enables/Disables redaction support when serializing complex objects.  Redacted fields/properties marked
     * with the @LogRedact annotation will be output as a string with the value "<REDACTED>".
     *
     * @param redactEnabled - true to filter out redacted fields
     *
     * @since 1.1.0
     */
    public void setRedactEnabled(final boolean redactEnabled) {
        final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        if (redactEnabled) {
            simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!redactNull));
        } else {
            simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID,
                    SimpleBeanPropertyFilter.serializeAllExcept(Collections.<String>emptySet()));
        }
        objectMapper.setFilters(simpleFilterProvider);
        this.redactEnabled = redactEnabled;
    }

    /**
     * Determines how null values will be output for fields marked with @LogRedact.  If this is set
     * to false then null values will be output as null, otherwise a string with the value of "<REDACTED>" will
     * be used.  This property only takes effect if 'redactEnabled' is true.  Default: true
     *
     * @return whether nulls will be redacted
     *
     * @since 1.1.0
     */
    public boolean isRedactNull() {
        return redactNull;
    }

    /**
     * Enables/Disables output of null for redacted fields when serializing complex objects.
     *
     * @param redactNull - true to redact null values, assuming redactEnabled is true
     *
     * @since 1.1.0
     */
    public void setRedactNull(final boolean redactNull) {
        if (redactEnabled) {
            final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
            simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!redactNull));
            objectMapper.setFilters(simpleFilterProvider);
        }
        this.redactNull = redactNull;
    }

    /**
     * Sets the log event name. This is used in place of the default log event name for logging events which do not
     * specify a {@link com.arpnetworking.logback.StenoMarker} Marker.
     *
     * @param logEventName The log event name.
     *
     * @since 1.1.0
     */
    public void setLogEventName(final String logEventName) {
        this.logEventName = logEventName;
    }

    public String getLogEventName() {
        return this.logEventName;
    }

    /**
     * Inject thread. This controls whether the process id is injected into each message's context. By default this
     * is <b>true</b>.
     *
     * @param value Whether to inject the process id.
     *
     * @since 1.1.0
     */
    public void setInjectContextProcess(final boolean value) {
        this.injectContextProcess = value;
    }

    public boolean isInjectContextProcess() {
        return this.injectContextProcess;
    }

    /**
     * Inject thread. This controls whether the host name is injected into each message's context. By default this
     * is <b>true</b>.
     *
     * @param value Whether to inject the host name.
     *
     * @since 1.1.0
     */
    public void setInjectContextHost(final boolean value) {
        this.injectContextHost = value;
    }

    public boolean isInjectContextHost() {
        return this.injectContextHost;
    }

    /**
     * Inject thread. This controls whether the thread name is injected into each message's context. By default this
     * is <b>true</b>.
     *
     * @param value Whether to inject the thread name.
     *
     * @since 1.1.0
     */
    public void setInjectContextThread(final boolean value) {
        this.injectContextThread = value;
    }

    public boolean isInjectContextThread() {
        return this.injectContextThread;
    }

    /**
     * Inject logger. This controls whether the logger name is injected into each message's context. By default this
     * is false.
     *
     * <i>Note:</i> This field does not strictly conform to the current Steno standard. The current Steno standard is
     * documented as a Json Schema in <code>resources/steno.schema.json</code>. For this reason the logger context
     * field is disabled by default.
     *
     * @param value Whether to inject the logger name.
     *
     * @since 1.1.0
     */
    public void setInjectContextLogger(final boolean value) {
        this.injectContextLogger = value;
    }

    public boolean isInjectContextLogger() {
        return this.injectContextLogger;
    }

    /**
     * Inject file. This controls whether the source file name is injected into each message's context. By default this
     * is false.
     *
     * @param value Whether to inject the file name.
     *
     * @since 1.1.0
     */
    public void setInjectContextFile(final boolean value) {
        this.injectContextFile = value;
    }

    public boolean isInjectContextFile() {
        return this.injectContextFile;
    }

    /**
     * Inject class. This controls whether the source class name is injected into each message's context. By default this
     * is false.
     *
     * @param value Whether to inject the class name.
     *
     * @since 1.1.0
     */
    public void setInjectContextClass(final boolean value) {
        this.injectContextClass = value;
    }

    public boolean isInjectContextClass() {
        return this.injectContextClass;
    }

    /**
     * Inject method. This controls whether the method name is injected into each message's context. By default this
     * is false.
     *
     * @param value Whether to inject the method name.
     *
     * @since 1.1.0
     */
    public void setInjectContextMethod(final boolean value) {
        this.injectContextMethod = value;
    }

    public boolean isInjectContextMethod() {
        return this.injectContextMethod;
    }

    /**
     * Inject line. This controls whether the line number is injected into each message's context. By default this
     * is false.
     *
     * @param value Whether to inject the line number.
     *
     * @since 1.1.0
     */
    public void setInjectContextLine(final boolean value) {
        this.injectContextLine = value;
    }

    public boolean isInjectContextLine() {
        return this.injectContextLine;
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
            startStenoWrapper(event, logEventName, jsonGenerator);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            jsonGenerator.writeObjectField("message", event.getFormattedMessage());
            jsonGenerator.writeEndObject(); // End 'data' field

            // Output throwable
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, logEventName, jsonGenerator);
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
                        jsonGenerator.writeFieldName(keys[i]);
                        objectMapper.writeValue(jsonGenerator, values[i]);
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
     * {@inheritDoc}
     */
    @Override
    protected String buildArrayJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final String[] keys,
            final String[] jsonValues) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator);

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
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, eventName, jsonGenerator);
        } catch (final IOException e) {
            return "Unknown exception: " + e.getMessage();
        }

        return jsonWriter.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMapMessage(
            final ILoggingEvent event,
            final String eventName,
            final Map<String, ? extends Object> map) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            if (map != null) {
                for (final Map.Entry<String, ? extends Object> entry : map.entrySet()) {
                    if (isSimpleType(entry.getValue())) {
                        jsonGenerator.writeObjectField(entry.getKey(), entry.getValue());
                    } else {
                        jsonGenerator.writeFieldName(entry.getKey());
                        objectMapper.writeValue(jsonGenerator, entry.getValue());
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
     * {@inheritDoc}
     */
    @Override
    protected String buildMapJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final Map<String, String> map) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator);

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
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, eventName, jsonGenerator);
        } catch (final IOException e) {
            return "Unknown exception: " + e.getMessage();
        }

        return jsonWriter.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectMessage(
            final ILoggingEvent event,
            final String eventName,
            final Object data) {

        final String jsonData;
        try {
            jsonData = data == null ? null : objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "Unknown exception: " + e.getMessage();
        }
        return buildObjectJsonMessage(
                event,
                eventName,
                jsonData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final String jsonData) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator);

            // Write event data
            jsonGenerator.writeFieldName("data");
            if (jsonData == null) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeEndObject();
            } else {
                jsonGenerator.writeRawValue(jsonData);
            }
            // TODO(vkoskela): Support writing null objects as-is via configuration [MAI-333]
            // e.g. "data":null -- although this is not supported by the current Steno specification

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
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
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
            jsonGenerator.writeRawValue(json);
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
     * Start writing the Steno JSON wrapper.
     *
     * @param event         Instance of <code>ILoggingEvent</code>.
     * @param eventName     The name of the event.
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
        jsonGenerator.writeObjectField("time",
                ISO_DATE_TIME_FORMATTER.print(new DateTime(event.getTimeStamp(), DateTimeZone.UTC)));
        jsonGenerator.writeObjectField("name", eventName);
        jsonGenerator.writeObjectField("level", level.name());
    }

    /**
     * Complete writing the Steno JSON wrapper.
     *
     * @param event         Instance of <code>ILoggingEvent</code>.
     * @param eventName     The name of the event.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @throws IOException If writing JSON fails.
     */
    // CS.OFF: NPathComplexity
    protected void endStenoWrapper(
        final ILoggingEvent event,
        final String eventName,
        final JsonGenerator jsonGenerator)
        throws IOException {

        jsonGenerator.writeObjectFieldStart("context");
        if (this.injectContextHost) {
            jsonGenerator.writeStringField("host", HOST_CONVERTER.convert(event));
        }
        if (this.injectContextProcess) {
            jsonGenerator.writeStringField("processId", PROCESS_CONVERTER.convert(event));
        }
        if (this.injectContextThread) {
            jsonGenerator.writeObjectField("threadId", THREAD_CONVERTER.convert(event));
        }
        if (this.injectContextLogger) {
            jsonGenerator.writeStringField("logger", LOGGER_CONVERTER.convert(event));
        }
        if (this.injectContextFile) {
            jsonGenerator.writeStringField("file", FILE_CONVERTER.convert(event));
        }
        if (this.injectContextClass) {
            jsonGenerator.writeStringField("class", CLASS_CONVERTER.convert(event));
        }
        if (this.injectContextMethod) {
            jsonGenerator.writeStringField("method", METHOD_CONVERTER.convert(event));
        }
        if (this.injectContextLine) {
            jsonGenerator.writeStringField("line", LINE_CONVERTER.convert(event));
        }
        jsonGenerator.writeEndObject(); // End 'context' field
        jsonGenerator.writeObjectField("id", createId());
        jsonGenerator.writeEndObject(); // End log message
        jsonGenerator.writeRaw('\n');
        jsonGenerator.flush();
    }
    // CS.ON: NPathComplexity

    /**
     * Write a <code>Throwable</code> via <code>IThrowableProxy</code> as JSON.
     *
     * @param throwableProxy Throwable to serialize
     * @param jsonGenerator  <code>JsonGenerator</code> instance.
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
     * @param jsonGenerator  <code>JsonGenerator</code> instance after exception object is started
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
