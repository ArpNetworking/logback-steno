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
import com.arpnetworking.logback.jackson.FilterForcingAnnotationIntrospector;
import com.arpnetworking.logback.jackson.RedactionFilter;
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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Encoder that builds Steno formatted log messages.
 * <br>
 * This encoder accepts logging events in various formats and keys off the existence of an slf4j Marker added to the log
 * event to determine the format of the arguments.
 * <br>
 * For logging events that don't have an associated Marker a standard Steno wrapper will be created with the string
 * logging message added as a 'message' field in the 'data' object. In such a case the name defaults to "log" unless a
 * name is specifically configured on the encoder.
 * <br><br>
 * <i>Example Output:</i>
 * <pre><code>
 * {
 *   "time":"2011-11-11T00:00:00.000Z",
 *   "name":"log",
 *   "level":"info",
 *   "data":{
 *     "message":"log message"
 *   },
 *   "context":{
 *     "host":"h",
 *     "processId":"p",
 *     "threadId":"t"
 *   },
 *   "id":"oRw59PrARvatGNC7fiWw4A"
 * }
 * </code></pre>
 * For logging events that have the {@link com.arpnetworking.logback.StenoMarker#ARRAY_MARKER} Marker the logging event is
 * expected to contain the name of the event as the 'message', with the first argument being a String array (String[])
 * containing the keys to be added to the 'data' object and the second argument being an Object array (Object[])
 * containing the values for each key.
 * <br><br>
 * <i>Example logger call:</i>
 * <pre><code>
 * log.info(StenoMarker.ARRAY_MARKER, "log", new String[] {"key1","key2"}, new Object[] {1234, "foo"});
 * </code></pre>
 * Example Output:
 * <pre><code>
 * {
 *   "time":"2011-11-11T00:00:00.000Z",
 *   "name":"log",
 *   "level":"info",
 *   "data":{
 *     "key1":1234,
 *     "key2":"foo"
 *   },
 *   "context":{
 *     "host":"h",
 *     "processId":"p",
 *     "threadId":"t"
 *   },
 *   "id":"oRw59PrARvatGNC7fiWw4A"
 * }
 * </code></pre>
 * For logging events that have the {@link com.arpnetworking.logback.StenoMarker#MAP_MARKER} Marker the logging event is
 * expected to contain the name of the event as the 'message', with the first argument being a map of key to value to be
 * included inside the 'data' object.
 * <br><br>
 * <i>Example logger call:</i>
 * <pre><code>
 * log.info(StenoMarker.MAP_MARKER, "log", ImmutableMap.of("key1", 1234, "key2", "foo"));
 * </code></pre>
 * <i>Example Output:</i>
 * <pre><code>
 * {
 *   "time":"2011-11-11T00:00:00.000Z",
 *   "name":"log",
 *   "level":"info",
 *   "data":{
 *     "key1":1234,
 *     "key2":"foo"
 *   },
 *   "context":{
 *     "host":"h",
 *     "processId":"p",
 *     "threadId":"t"
 *   },
 *   "id":"oRw59PrARvatGNC7fiWw4A"
 * }
 * </code></pre>
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public class StenoEncoder extends BaseLoggingEncoder {

    /**
     * Public constructor.
     */
    public StenoEncoder() {
        // Each instance of StenoEncoder requires its own ObjectMapper instance
        // in order to apply the correct filtering settings to support redaction.
        this(JSON_FACTORY, new ObjectMapper());
    }

    /* package private */ StenoEncoder(final JsonFactory jsonFactory, final ObjectMapper objectMapper) {
        _jsonFactory = jsonFactory;

        // Initialize object mapper;
        _objectMapper = objectMapper;
        _objectMapper.setAnnotationIntrospector(new FilterForcingAnnotationIntrospector());
        final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!DEFAULT_REDACT_NULL));
        // Initialize this here based on the above code, if it was initialized at the declaration site then things
        // could get out of sync
        _redactEnabled = true;
        _objectMapper.setFilters(simpleFilterProvider);

        // Setup writing of Date/DateTime values
        _objectMapper.registerModule(new JodaModule());
        _objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        _objectMapper.setDateFormat(new ISO8601DateFormat());
    }

    /**
     * Enables/Disables redaction support when serializing complex objects.  Redacted fields/properties marked
     * with the @LogRedact annotation will be output as a string with the value "{@code<REDACTED>}".
     *
     * @param redactEnabled - true to filter out redacted fields
     *
     * @since 1.1.0
     */
    public void setRedactEnabled(final boolean redactEnabled) {
        final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        if (redactEnabled) {
            simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!_redactNull));
        } else {
            simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID,
                    SimpleBeanPropertyFilter.serializeAllExcept(Collections.<String>emptySet()));
        }
        _objectMapper.setFilters(simpleFilterProvider);
        _redactEnabled = redactEnabled;
    }

    /**
     * Determines if fields marked with the annotation @LogRedact will be redacted in log output when serializing
     * complex objects. If true then values for annotated fields/properties will be output as a string with the value
     * "{@code <REDACTED>}", otherwise the value will be output as serialized json using a Jackson object mapper.
     * Default: true
     *
     * @return whether redacted fields or filtered
     *
     * @since 1.1.0
     */
    public boolean isRedactEnabled() {
        return _redactEnabled;
    }

    /**
     * Enables/Disables output of null for redacted fields when serializing complex objects.
     *
     * @param redactNull - true to redact null values, assuming redactEnabled is true
     *
     * @since 1.1.0
     */
    public void setRedactNull(final boolean redactNull) {
        if (_redactEnabled) {
            final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
            simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!redactNull));
            _objectMapper.setFilters(simpleFilterProvider);
        }
        _redactNull = redactNull;
    }

    /**
     * Determines how null values will be output for fields marked with @LogRedact.  If this is set
     * to false then null values will be output as null, otherwise a string with the value of "{@code <REDACTED>}" will
     * be used.  This property only takes effect if 'redactEnabled' is true.  Default: true
     *
     * @return whether nulls will be redacted
     *
     * @since 1.1.0
     */
    public boolean isRedactNull() {
        return _redactNull;
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
        _logEventName = logEventName;
    }

    /**
     * Retrieve the default log event name. This is used in place of the default log event name for logging events which
     * do not specify a {@link com.arpnetworking.logback.StenoMarker} Marker.
     *
     * @return The default log event name.
     *
     * @since 1.1.0
     */
    public String getLogEventName() {
        return _logEventName;
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
        _injectContextProcess = value;
    }

    /**
     * Whether process id is injected into the context. By default this is <b>true</b>.
     *
     * @return True if and only if process id is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextProcess() {
        return _injectContextProcess;
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
        _injectContextHost = value;
    }

    /**
     * Whether host name is injected into the context. By default this is <b>true</b>.
     *
     * @return True if and only if host name is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextHost() {
        return _injectContextHost;
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
        _injectContextThread = value;
    }

    /**
     * Whether thread name is injected into the context. By default this is <b>true</b>.
     *
     * @return True if and only if thread name is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextThread() {
        return _injectContextThread;
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
        _injectContextLogger = value;
    }

    /**
     * Whether logger name is injected into the context. By default this is false.
     *
     * @return True if and only if logger name is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextLogger() {
        return _injectContextLogger;
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
        _injectContextFile = value;
    }

    /**
     * Whether source file name is injected into the context. By default this is false.
     *
     * @return True if and only if source file name is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextFile() {
        return _injectContextFile;
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
        _injectContextClass = value;
    }

    /**
     * Whether class name is injected into the context. By default this is false.
     *
     * @return True if and only if class name is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextClass() {
        return _injectContextClass;
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
        _injectContextMethod = value;
    }

    /**
     * Whether method name is injected into the context. By default this is false.
     *
     * @return True if and only if method name is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextMethod() {
        return _injectContextMethod;
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
        _injectContextLine = value;
    }

    /**
     * Whether line number is injected into the context. By default this is false.
     *
     * @return True if and only if line number is injected into the context.
     *
     * @since 1.1.0
     */
    public boolean isInjectContextLine() {
        return _injectContextLine;
    }

    /**
     * Inject MDC properties. This controls which MDC properties are injected into each message's context. By default
     * no properties are injected.
     *
     * @param values The MDC properties to inject into the context.
     *
     * @since 1.3.0
     */
    public void setMdcProperties(final Set<String> values) {
        _injectMdcProperties = values;
    }

    /**
     * Which MDC properties are injected into the context. By default this is an empty set.
     *
     * @return The set of MDC properties injected into the context.
     *
     * @since 1.3.0
     */
    public Set<String> getMdcProperties() {
        return _injectMdcProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildStandardMessage(final ILoggingEvent event) {
        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, _logEventName, jsonGenerator);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            jsonGenerator.writeObjectField("message", event.getFormattedMessage());
            jsonGenerator.writeEndObject(); // End 'data' field

            // Output throwable
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, _logEventName, jsonGenerator);
        } catch (final IOException e) {
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
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
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
                        _objectMapper.writeValue(jsonGenerator, values[i]);
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
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
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
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
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
                        _objectMapper.writeValue(jsonGenerator, entry.getValue());
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
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
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
            jsonData = data == null ? null : _objectMapper.writeValueAsString(data);
        } catch (final JsonProcessingException e) {
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
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
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
    @Override
    protected String buildListsMessage(
            final ILoggingEvent event,
            final String eventName,
            final List<String> dataKeys,
            final List<Object> dataValues,
            final List<String> contextKeys,
            final List<Object> contextValues) {

        final StringWriter jsonWriter = new StringWriter();
        try {
            final JsonGenerator jsonGenerator = _jsonFactory.createGenerator(jsonWriter);
            // Start wrapper
            startStenoWrapper(event, eventName, jsonGenerator);

            // Write event data
            jsonGenerator.writeObjectFieldStart("data");
            writeKeyValuePairs(dataKeys, dataValues, jsonGenerator);
            jsonGenerator.writeEndObject(); // End 'data' field

            // Output throwable
            writeThrowable(event.getThrowableProxy(), jsonGenerator);

            // End wrapper
            endStenoWrapper(event, eventName, contextKeys, contextValues, jsonGenerator);
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
     * @throws IOException If writing JSON fails.
     */
    // CS.OFF: NPathComplexity
    protected void endStenoWrapper(
            final ILoggingEvent event,
            final String eventName,
            final JsonGenerator jsonGenerator)
            throws IOException {
        endStenoWrapper(
                event,
                eventName,
                Collections.emptyList(),
                Collections.emptyList(),
                jsonGenerator);
    }

    /**
     * Complete writing the Steno JSON wrapper.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param contextKeys The <code>List</code> of context keys.
     * @param contextValues The <code>List</code> of context values.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @throws IOException If writing JSON fails.
     */
    // CS.OFF: NPathComplexity
    protected void endStenoWrapper(
        final ILoggingEvent event,
        final String eventName,
        final List<String> contextKeys,
        final List<Object> contextValues,
        final JsonGenerator jsonGenerator)
        throws IOException {

        jsonGenerator.writeObjectFieldStart("context");
        if (_injectContextHost) {
            jsonGenerator.writeStringField("host", HOST_CONVERTER.convert(event));
        }
        if (_injectContextProcess) {
            jsonGenerator.writeStringField("processId", PROCESS_CONVERTER.convert(event));
        }
        if (_injectContextThread) {
            jsonGenerator.writeObjectField("threadId", THREAD_CONVERTER.convert(event));
        }
        if (_injectContextLogger) {
            jsonGenerator.writeStringField("logger", LOGGER_CONVERTER.convert(event));
        }
        if (_injectContextFile) {
            jsonGenerator.writeStringField("file", FILE_CONVERTER.convert(event));
        }
        if (_injectContextClass) {
            jsonGenerator.writeStringField("class", CLASS_CONVERTER.convert(event));
        }
        if (_injectContextMethod) {
            jsonGenerator.writeStringField("method", METHOD_CONVERTER.convert(event));
        }
        if (_injectContextLine) {
            jsonGenerator.writeStringField("line", LINE_CONVERTER.convert(event));
        }
        for (final String key : _injectMdcProperties) {
            final String value = event.getMDCPropertyMap().get(key);
            jsonGenerator.writeStringField(key, value);
        }
        writeKeyValuePairs(contextKeys, contextValues, jsonGenerator);
        jsonGenerator.writeEndObject(); // End 'context' field
        jsonGenerator.writeObjectField("id", createId());
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
     * @throws IOException If writing JSON fails.
     */
    protected void writeKeyValuePairs(
            final List<String> keys,
            final List<Object> values,
            final JsonGenerator jsonGenerator)
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
                        _objectMapper.writeValue(jsonGenerator, value);
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

    private ObjectMapper _objectMapper;
    private final JsonFactory _jsonFactory;
    private String _logEventName = STANDARD_LOG_EVENT_NAME;
    private boolean _redactEnabled;
    private boolean _redactNull = DEFAULT_REDACT_NULL;
    private boolean _injectContextProcess = true;
    private boolean _injectContextHost = true;
    private boolean _injectContextThread = true;
    private boolean _injectContextLogger = false;
    private boolean _injectContextClass = false;
    private boolean _injectContextFile = false;
    private boolean _injectContextMethod = false;
    private boolean _injectContextLine = false;
    private Set<String> _injectMdcProperties = Collections.emptySet();

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
