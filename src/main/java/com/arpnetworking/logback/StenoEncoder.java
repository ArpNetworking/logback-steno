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
import com.arpnetworking.logback.jackson.StenoBeanSerializerModifier;
import com.arpnetworking.logback.jackson.ThrowableMixIn;
import com.arpnetworking.logback.serialization.steno.ArrayOfJsonSerialziationStrategy;
import com.arpnetworking.logback.serialization.steno.ArraySerialziationStrategy;
import com.arpnetworking.logback.serialization.steno.ListsSerialziationStrategy;
import com.arpnetworking.logback.serialization.steno.MapOfJsonSerialziationStrategy;
import com.arpnetworking.logback.serialization.steno.MapSerialziationStrategy;
import com.arpnetworking.logback.serialization.steno.ObjectAsJsonSerialziationStrategy;
import com.arpnetworking.logback.serialization.steno.ObjectSerialziationStrategy;
import com.arpnetworking.logback.serialization.steno.SafeSerializationHelper;
import com.arpnetworking.logback.serialization.steno.StandardSerializationStrategy;
import com.arpnetworking.logback.serialization.steno.StenoSerializationHelper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

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
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.0.0
 */
public class StenoEncoder extends BaseLoggingEncoder implements Serializable {

    /**
     * Public constructor.
     */
    public StenoEncoder() {
        // Each instance of StenoEncoder requires its own ObjectMapper instance
        // in order to apply the correct filtering settings to support redaction.
        this(new ObjectMapper());
    }

    /* package private */ StenoEncoder(final ObjectMapper objectMapper) {
        // Each instance of StenoEncoder requires its own ObjectMapper instance
        // in order to apply the correct filtering settings to support redaction.
        this(objectMapper.getFactory(), objectMapper);
    }

    /* package private */ StenoEncoder(final JsonFactory jsonFactory, final ObjectMapper objectMapper) {

        // Initialize object mapper;
        _objectMapper = objectMapper;
        _objectMapper.setAnnotationIntrospector(new StenoAnnotationIntrospector(_objectMapper));
        final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(!DEFAULT_REDACT_NULL));
        // Initialize this here based on the above code, if it was initialized at the declaration site then things
        // could get out of sync
        _redactEnabled = true;
        _objectMapper.setFilterProvider(simpleFilterProvider);

        // Setup writing of Date/DateTime values
        _objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        _objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        _objectMapper.setDateFormat(new ISO8601DateFormat());

        // Simple module with customizations
        final SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new StenoBeanSerializerModifier(this));
        _objectMapper.registerModule(module);

        // Throwable mix-in
        _objectMapper.setMixIns(Collections.singletonMap(Throwable.class, ThrowableMixIn.class));

        // After burner to improve data-bind performance
        _objectMapper.registerModule(new AfterburnerModule());

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
     * {@inheritDoc}
     */
    @Override
    public void start() {
        // Add configured Jackson modules
        _objectMapper.registerModules(_jacksonModules);
    }

    /**
     * Enables/Disables redaction support when serializing complex objects.  Redacted fields/properties marked
     * with the @LogRedact annotation will be output as a string with the value "{@code <REDACTED>}".
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
        _objectMapper.setFilterProvider(simpleFilterProvider);
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
            _objectMapper.setFilterProvider(simpleFilterProvider);
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
     * Add MDC property to inject into the context. This controls which MDC properties are injected into each message's
     * context. By default no properties are injected.
     *
     * @param key The MDC property key to inject into the context.
     *
     * @since 1.4.0
     */
    public void addInjectContextMdc(final String key) {
        _injectMdcProperties.add(key);
    }

    /**
     * Which MDC properties are injected into the context. By default this is none.
     *
     * @return The iterator over MDC properties injected into the context.
     *
     * @since 1.4.0
     */
    public Iterator<String> iteratorForInjectContextMdc() {
        return _injectMdcProperties.iterator();
    }

    /**
     * Determine if the specific MDC property key is injected into the context. By default it is not.
     *
     * @param key The MDC property key to check.
     * @return True if and only if the key is injected into the context.
     *
     * @since 1.4.0
     */
    public boolean isInjectContextMdc(final String key) {
        return _injectMdcProperties.contains(key);
    }

    /**
     * Add Jackson <code>Module</code>.
     *
     * @param module The Jackson <code>Module</code>.
     *
     * @since 1.5.0
     */
    public void addJacksonModule(final Module module) {
        _jacksonModules.add(module);
    }

    /**
     * Which Jackson <code>Module</code> instances are configured.
     *
     * @return The iterator over configured Jackson <code>Module</code> instances.
     *
     * @since 1.5.0
     */
    public Iterator<Module> iteratorForJacksonModule() {
        return _jacksonModules.iterator();
    }

    /**
     * Determine if the specific Jackson <code>Module</code> is configured.
     *
     * @param module The Jackson <code>Module</code>.
     * @return True if and only if the <code>Module</code> is configured.
     *
     * @since 1.5.0
     */
    public boolean isJacksonModule(final Module module) {
        return _jacksonModules.contains(module);
    }

    /**
     * This controls whether the encoder should only encode types when safe to do so. By default this is true. Types are
     * considered safe for serialization if any of the following are true:
     *
     * <ul>
     *     <li>The instance is null.</li>
     *     <li>The type is a String, Number or Boolean.</li>
     *     <li>The type is a JsonNode.</li>
     *     <li>The type is associated with a custom serializer (e.g. not BeanSerializer):
     *          <ul>
     *              <li>Set with the class annotation @JsonSerialize.</li>
     *              <li>Set with by a registered module.</li>
     *              <li>Set with the method annotation @JsonValue.</li>
     *              <li>Set with the method annotation @LogValue.</li>
     *          </ul>
     *     </li>
     *     <li>The class is explicitly marked safe for logging with the annotation @Loggable.</li>
     * </ul>
     *
     * When safe is set to true and a type is determined to be unsafe it is serialized as a reference only.
     *
     * @param value True if and only if the encoder should only encode types when safe to do so.
     *
     * @since 1.8.0
     */
    public void setSafe(final boolean value) {
        _safe = value;
    }

    /**
     * Whether the encoder should only encode types when safe to do so. By default this is true.
     *
     * @return True if and only if the encoder should only encode types when safe to do so.
     *
     * @since 1.8.0
     */
    public boolean isSafe() {
        return _safe;
    }

    /**
     * Inject bean identifier attributes. This controls whether the the instance identifier and class name are
     * always injected into each serialized data or context value. The values are always injected for non-loggable
     * types and this setting allows injection for all types. By default this is false.
     *
     * @since 1.9.0
     *
     * @param value Whether to inject the bean identifier attributes for all beans irregardless of loggability.
     */
    public void setInjectBeanIdentifier(final boolean value) {
        _injectBeanIdentifier = value;
    }

    /**
     * Whether bean identifier attributes are injected. By default this is false.
     *
     * @since 1.9.0
     *
     * @return True if and only if bean identifier attributes are injected.
     */
    public boolean isInjectBeanIdentifier() {
        return _injectBeanIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String encodeAsString(final ILoggingEvent event, final EncodingException ee) {
        final StringBuilder encoder = new StringBuilder()
                .append("{\"time\":\"")
                .append(ISO_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp())))
                .append("\",\"name\":\"EncodingException\",\"level\":\"warn\",\"data\":{\"originalMessage\":");
        SafeSerializationHelper.safeEncodeValue(encoder, event.getMessage());
        encoder.append("},\"exception\":");
        SafeSerializationHelper.safeEncodeValue(encoder, ee);
        encoder.append(",\"context\":");
        SafeSerializationHelper.safeEncodeValue(encoder, ee.getContext());
        encoder.append(",\"id\":\"")
                .append(StenoSerializationHelper.createId())
                .append("\",\"version\":\"0\"}\n");
        return encoder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildStandardMessage(final ILoggingEvent event) throws EncodingException {
        try {
            return _standardSerializationStrategy.serialize(event, _logEventName);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(SafeSerializationHelper.createSafeContext(this, event, _objectMapper), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildArrayMessage(
            final ILoggingEvent event,
            @Nullable final String eventName,
            @Nullable final String[] keys,
            @Nullable final Object[] values)
            throws EncodingException {

        try {
            return _arraySerialziationStrategy.serialize(
                    event,
                    firstNonNull(eventName, _logEventName),
                    keys,
                    values);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(SafeSerializationHelper.createSafeContext(this, event, _objectMapper), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildArrayJsonMessage(
            final ILoggingEvent event,
            @Nullable final String eventName,
            @Nullable final String[] keys,
            @Nullable final String[] jsonValues)
            throws EncodingException {

        try {
            return _arrayOfJsonSerialziationStrategy.serialize(
                    event,
                    firstNonNull(eventName, _logEventName),
                    keys,
                    jsonValues);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(SafeSerializationHelper.createSafeContext(this, event, _objectMapper), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMapMessage(
            final ILoggingEvent event,
            @Nullable final String eventName,
            @Nullable final Map<String, ?> map)
            throws EncodingException {

        try {
            return _mapSerialziationStrategy.serialize(
                    event,
                    firstNonNull(eventName, _logEventName),
                    map);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(SafeSerializationHelper.createSafeContext(this, event, _objectMapper), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMapJsonMessage(
            final ILoggingEvent event,
            @Nullable final String eventName,
            @Nullable final Map<String, String> map)
            throws EncodingException {

        try {
            return _mapOfJsonSerialziationStrategy.serialize(
                    event,
                    firstNonNull(eventName, _logEventName),
                    map);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(SafeSerializationHelper.createSafeContext(this, event, _objectMapper), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectMessage(
            final ILoggingEvent event,
            @Nullable final String eventName,
            @Nullable final Object data)
            throws EncodingException {

        try {
            return _objectSerialziationStrategy.serialize(
                    event,
                    firstNonNull(eventName, _logEventName),
                    // TODO(ville): This is where the switch for null as-is for object marker should go [issue #4]
                    data == null ? Collections.emptyMap() : data);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(SafeSerializationHelper.createSafeContext(this, event, _objectMapper), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectJsonMessage(
            final ILoggingEvent event,
            @Nullable final String eventName,
            final String jsonData)
            throws EncodingException {

        try {
            return _objectAsJsonSerialziationStrategy.serialize(
                    event,
                    firstNonNull(eventName, _logEventName),
                    jsonData);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(SafeSerializationHelper.createSafeContext(this, event, _objectMapper), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildListsMessage(
            final ILoggingEvent event,
            @Nullable final String eventName,
            @Nullable final List<String> dataKeys,
            @Nullable final List<Object> dataValues,
            @Nullable final List<String> contextKeys,
            @Nullable final List<Object> contextValues)
            throws EncodingException {

        try {
            return _listsSerialziationStrategy.serialize(
                    event,
                    firstNonNull(eventName, _logEventName),
                    dataKeys,
                    dataValues,
                    contextKeys,
                    contextValues);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(
                    SafeSerializationHelper.createSafeContext(
                            this,
                            event,
                            _objectMapper,
                            contextKeys,
                            contextValues),
                    t);
        }
    }

    /* package private */ static <T> T firstNonNull(@Nullable final T first, @Nullable final T second) {
        if (first != null) {
            return first;
        } else if (second != null) {
            return second;
        }
        throw new NullPointerException("Both arguments are null");
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
    private Set<String> _injectMdcProperties = new LinkedHashSet<>();
    private Set<Module> _jacksonModules = new LinkedHashSet<>();
    private boolean _safe = true;
    private boolean _injectBeanIdentifier = false;

    private static final boolean DEFAULT_REDACT_NULL = true;
    private static final String STANDARD_LOG_EVENT_NAME = "log";
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ").withZone(ZoneId.of("UTC"));
    private static final long serialVersionUID = -1803222342605243667L;
}
