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

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arpnetworking.logback.jackson.RedactionFilter;
import com.arpnetworking.logback.jackson.StenoAnnotationIntrospector;
import com.arpnetworking.logback.serialization.ArrayOfJsonSerialziationStrategy;
import com.arpnetworking.logback.serialization.ArraySerialziationStrategy;
import com.arpnetworking.logback.serialization.ListsSerialziationStrategy;
import com.arpnetworking.logback.serialization.MapOfJsonSerialziationStrategy;
import com.arpnetworking.logback.serialization.MapSerialziationStrategy;
import com.arpnetworking.logback.serialization.ObjectAsJsonSerialziationStrategy;
import com.arpnetworking.logback.serialization.ObjectSerialziationStrategy;
import com.arpnetworking.logback.serialization.StandardSerializationStrategy;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.base.Objects;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * @author Ville Koskela (vkoskela at groupon dot com)
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

        // Initialize object mapper;
        _objectMapper = objectMapper;
        _objectMapper.setAnnotationIntrospector(new StenoAnnotationIntrospector());
        final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!DEFAULT_REDACT_NULL));
        // Initialize this here based on the above code, if it was initialized at the declaration site then things
        // could get out of sync
        _redactEnabled = true;
        _objectMapper.setFilters(simpleFilterProvider);

        // Setup writing of Date/DateTime values
        _objectMapper.registerModule(new JodaModule());
        _objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        _objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        _objectMapper.setDateFormat(new ISO8601DateFormat());

        // Setup other common modules
        _objectMapper.registerModule(new AfterburnerModule());
        _objectMapper.registerModule(new Jdk7Module());
        _objectMapper.registerModule(new Jdk8Module());
        _objectMapper.registerModule(new GuavaModule());

        // Serialization strategies
        _listsSerialziationStrategy = new ListsSerialziationStrategy(this, jsonFactory, _objectMapper);
        _objectAsJsonSerialziationStrategy = new ObjectAsJsonSerialziationStrategy(this, jsonFactory, _objectMapper);
        _objectSerialziationStrategy = new ObjectSerialziationStrategy(this, jsonFactory, _objectMapper);
        _mapOfJsonSerialziationStrategy = new MapOfJsonSerialziationStrategy(this, jsonFactory, _objectMapper);
        _mapSerialziationStrategy = new MapSerialziationStrategy(this, jsonFactory, _objectMapper);
        _arrayOfJsonSerialziationStrategy = new ArrayOfJsonSerialziationStrategy(this, jsonFactory, _objectMapper);
        _arraySerialziationStrategy = new ArraySerialziationStrategy(this, jsonFactory, _objectMapper);
        _standardSerializationStrategy = new StandardSerializationStrategy(this, jsonFactory, _objectMapper);
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
     * Compress logger name. This controls whether the logger name is compressed prior to injection into each message's
     * context. By default this is false. Compression takes each part of name separated by "." (aka period or dot) and
     * reduces all but the last to only the first character. Compression has no effect if logger name injection is
     * disabled.
     *
     * @param value Whether to compress the logger name.
     *
     * @since 1.3.1
     */
    public void setCompressLoggerName(final boolean value) {
        _compressLoggerName = value;
    }

    /**
     * Whether logger name is compressed. By default this is false. Compression takes each part of name separated by
     * "." (aka period or dot) and reduces all but the last to only the first character.
     *
     * @return True if and only if logger name is to be compressed.
     *
     * @since 1.3.1
     */
    public boolean isCompressLoggerName() {
        return _compressLoggerName;
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
        return _standardSerializationStrategy.serialize(event, _logEventName);
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

        return _arraySerialziationStrategy.serialize(
                event,
                Objects.firstNonNull(eventName, _logEventName),
                keys,
                values);
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

        return _arrayOfJsonSerialziationStrategy.serialize(
                event,
                Objects.firstNonNull(eventName, _logEventName),
                keys,
                jsonValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMapMessage(
            final ILoggingEvent event,
            final String eventName,
            final Map<String, ? extends Object> map) {

        return _mapSerialziationStrategy.serialize(
                event,
                Objects.firstNonNull(eventName, _logEventName),
                map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMapJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final Map<String, String> map) {

        return _mapOfJsonSerialziationStrategy.serialize(
                event,
                Objects.firstNonNull(eventName, _logEventName),
                map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectMessage(
            final ILoggingEvent event,
            final String eventName,
            final Object data) {

        return _objectSerialziationStrategy.serialize(
                event,
                Objects.firstNonNull(eventName, _logEventName),
                data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final String jsonData) {

        return _objectAsJsonSerialziationStrategy.serialize(
                event,
                Objects.firstNonNull(eventName, _logEventName),
                jsonData);
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

        return _listsSerialziationStrategy.serialize(
                event,
                Objects.firstNonNull(eventName, _logEventName),
                dataKeys,
                dataValues,
                contextKeys,
                contextValues);
    }

    private ObjectMapper _objectMapper;
    private final ListsSerialziationStrategy _listsSerialziationStrategy;
    private final ObjectAsJsonSerialziationStrategy _objectAsJsonSerialziationStrategy;
    private final ObjectSerialziationStrategy _objectSerialziationStrategy;
    private final MapOfJsonSerialziationStrategy _mapOfJsonSerialziationStrategy;
    private final MapSerialziationStrategy _mapSerialziationStrategy;
    private final ArrayOfJsonSerialziationStrategy _arrayOfJsonSerialziationStrategy;
    private final ArraySerialziationStrategy _arraySerialziationStrategy;
    private final StandardSerializationStrategy _standardSerializationStrategy;

    private String _logEventName = STANDARD_LOG_EVENT_NAME;
    private boolean _redactEnabled;
    private boolean _redactNull = DEFAULT_REDACT_NULL;
    private boolean _injectContextProcess = true;
    private boolean _injectContextHost = true;
    private boolean _injectContextThread = true;
    private boolean _injectContextLogger = false;
    private boolean _compressLoggerName = false;
    private boolean _injectContextClass = false;
    private boolean _injectContextFile = false;
    private boolean _injectContextMethod = false;
    private boolean _injectContextLine = false;
    private Set<String> _injectMdcProperties = Collections.emptySet();

    private static final int UUID_LENGTH_IN_BYTES = 16;
    private static final boolean DEFAULT_REDACT_NULL = true;
    private static final String STANDARD_LOG_EVENT_NAME = "log";
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
}
