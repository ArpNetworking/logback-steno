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
import com.arpnetworking.logback.annotations.Loggable;
import com.arpnetworking.steno.LogReferenceOnly;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper functions and functors for Steno serialziation.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.7.0
 */
public final class StenoSerializationHelper {

    /**
     * Start writing the Steno JSON wrapper.
     *
     * @since 1.8.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws java.io.IOException If writing JSON fails.
     */
    public static void startStenoWrapper(
            final ILoggingEvent event,
            final String eventName,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper)
            throws IOException {

        final StenoSerializationHelper.StenoLevel level = StenoSerializationHelper.StenoLevel.findByLogbackLevel(
                event.getLevel());
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("time",
                ISO_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp())));
        jsonGenerator.writeObjectField("name", eventName);
        jsonGenerator.writeObjectField("level", level.name());
    }

    /**
     * Complete writing the Steno JSON wrapper.
     *
     * @since 1.8.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @param encoder <code>StenoEncoder</code> instance.
     * @throws IOException If writing JSON fails.
     */
    public static void endStenoWrapper(
            final ILoggingEvent event,
            final String eventName,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper,
            final StenoEncoder encoder)
            throws IOException {
        endStenoWrapper(
                event,
                eventName,
                Collections.emptyList(),
                Collections.emptyList(),
                jsonGenerator,
                objectMapper,
                encoder);
    }

    /**
     * Complete writing the Steno JSON wrapper.
     *
     * @since 1.8.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param contextKeys The <code>List</code> of context keys.
     * @param contextValues The <code>List</code> of context values.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @param encoder <code>StenoEncoder</code> instance.
     * @throws IOException If writing JSON fails.
     */
    public static void endStenoWrapper(
            final ILoggingEvent event,
            final String eventName,
            final List<String> contextKeys,
            final List<Object> contextValues,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper,
            final StenoEncoder encoder)
            throws IOException {

        jsonGenerator.writeFieldName("context");
        objectMapper.writeValue(
                jsonGenerator,
                StenoSerializationHelper.createContext(
                        encoder,
                        event,
                        objectMapper,
                        contextKeys,
                        contextValues));
        jsonGenerator.writeObjectField("id", StenoSerializationHelper.createId());
        jsonGenerator.writeObjectField("version", "0");
        jsonGenerator.writeEndObject(); // End log message
        jsonGenerator.writeRaw('\n');
        jsonGenerator.flush();
    }

    /**
     * Prepare an object for serialization. The following are serialized as-is:
     * <ul>
     *     <li>Null</li>
     *     <li>Simple types: String, Number and Boolean</li>
     *     <li>JsonNode</li>
     * </ul>
     *
     * If safety is disabled in <code>StenoEncoder</code> then all other types are also serialized as-is. This is not
     * recommended as serialization of many types can fail. Therefore safety is enabled by default and the instance is
     * only serialized as-is if:
     * <ul>
     *     <li>Custom serializer is found (e.g. not Jackson's BeanSerializer)</li>
     *     <li>Instance's class is annotated with @Loggable</li>
     *     <li>Instance's class hierarchy has a usable @LogValue or @JsonValue (*)</li>
     * </ul>
     *
     * Otherwise the instance is wrapped in <code>LogReferenceOnly</code> and returned.
     *
     * (*) The rules for selecting a @LogValue or @JsonValue annotated method are described in detail in the @LogValue
     * implementation and implemented in <code>StenoAnnotationIntrospector</code>.
     *
     * @since 1.8.0
     *
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @param encoder Instance of <code>StenoEncoder</code>.
     * @param o <code>Object</code> instance to prepare.
     * @return Prepared <code>Object</code> instance to serialize.
     */
    public static Object prepareForSerialization(
            final ObjectMapper objectMapper,
            final StenoEncoder encoder,
            final Object o) {
        if (o == null) {
            // Null can be serialized as-is
            return o;
        } else if (StenoSerializationHelper.isSimpleType(o)) {
            // Simple types, String, Number and Boolean, can be serialized as-is
            return o;
        } else if (o instanceof JsonNode) {
            // Any JsonNode can be serialized as-is
            return o;
        } else if (!encoder.isSafe()) {
            // In un-safe mode do no further analysis
            return o;
        } else {
            // If the class has a custom serializer then use it; this covers:
            // - Classes annotated with @JsonSerializer
            // - Serializer for the type registered through a module
            // - Json value serializer declared with @JsonValue
            // - Json value serializer declared with @LogValue
            // NOTE: See StenoAnnotationIntrospector and @LogValue for details regarding the resolution rules
            final SerializerProvider provider = new DefaultSerializerProvider.Impl().createInstance(
                    objectMapper.getSerializationConfig(),
                    objectMapper.getSerializerFactory());
            boolean isDefaultSerializer;
            try {
                final JsonSerializer<?> serializer = provider.findValueSerializer(o.getClass());
                isDefaultSerializer = serializer instanceof BeanSerializer;
            } catch (final JsonMappingException e) {
                // If determining the serializer fails we just pretend the serializer is the default
                // serializer which may resort to reference only serialization.
                isDefaultSerializer = true;
            }
            if (!isDefaultSerializer) {
                return o;
            }

            // If the class is annotated with Loggable then serialize it as-is
            Boolean isLoggable = LOGGABLE_CLASSES.get(o.getClass());
            if (isLoggable == null) {
                isLoggable = o.getClass().getAnnotation(Loggable.class) != null;
                LOGGABLE_CLASSES.put(o.getClass(), isLoggable);
            }
            if (isLoggable) {
                return o;
            }

            // Alternative way to determine if @LogValue and/or @JsonValue apply:
            /*
            final ClassIntrospector classIntrospector = objectMapper.getSerializationConfig().getClassIntrospector();
            final BeanDescription beanDescription = classIntrospector.forSerialization(
                    objectMapper.getSerializationConfig(),
                    SimpleType.construct(o.getClass()),
                    objectMapper.getSerializationConfig());
            if (beanDescription.findJsonValueMethod() != null) {
                return o;
            }
            */
        }

        // The object's type is neither naturally safe to serialize nor does it have explicit serialization or loggability
        // associated with it. Therefore, log the value as a reference only.
        return LogReferenceOnly.of(o);
    }

    /**
     * Write specified key-value pairs into the current block.
     *
     * @since 1.7.0
     * @param keys The <code>List</code> of keys.
     * @param values The <code>List</code> of values.
     * @param jsonGenerator <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @param encoder Instance of <code>StenoEncoder</code>.
     * @throws IOException If writing JSON fails.
     */
    public static void writeKeyValuePairs(
            final List<String> keys,
            final List<Object> values,
            final JsonGenerator jsonGenerator,
            final ObjectMapper objectMapper,
            final StenoEncoder encoder)
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
                        objectMapper.writeValue(
                                jsonGenerator,
                                StenoSerializationHelper.prepareForSerialization(
                                        objectMapper,
                                        encoder,
                                        value));
                    }
                }
            }
        }
    }

    /**
     * Write a <code>Throwable</code> via <code>IThrowableProxy</code> as JSON.
     *
     * @since 1.7.0
     * @param throwableProxy Throwable to serialize
     * @param jsonGenerator  <code>JsonGenerator</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws IOException If writing JSON fails.
     */
    public static void writeThrowable(
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
     * @since 1.7.0
     * @param throwableProxy Throwable to serialize
     * @param jsonGenerator  <code>JsonGenerator</code> instance after exception object is started
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @throws IOException If writing the <code>Throwable</code> as JSON fails.
     */
    public static void serializeThrowable(
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
     * Create a context based on the <code>StenoEncoder</code> configuration.
     *
     * @since 1.7.0
     * @param encoder The <code>StenoEncoder</code> instance.
     * @param event The <code>ILoggingEvent</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @param contextKeys The additional user provided context keys.
     * @param contextValues The additional user provided context values matching the keys.
     * @return <code>Map</code> with event context.
     * @throws IOException If writing creating context fails.
     */
    public static Map<String, Object> createContext(
            final StenoEncoder encoder,
            final ILoggingEvent event,
            final ObjectMapper objectMapper,
            final List<String> contextKeys,
            final List<Object> contextValues)
            throws IOException {
        return createContext(encoder, event, objectMapper, contextKeys, contextValues, false);
    }

    /**
     * Create a context based on the <code>StenoEncoder</code> configuration.
     *
     * @since 1.7.0
     * @param encoder The <code>StenoEncoder</code> instance.
     * @param event The <code>ILoggingEvent</code> instance.
     * @param objectMapper <code>ObjectMapper</code> instance.
     * @param contextKeys The additional user provided context keys.
     * @param contextValues The additional user provided context values matching the keys.
     * @param forceSafe If true encode user provided values as Strings regardless of encoder safe setting.
     * @return <code>Map</code> with event context.
     */
    public static Map<String, Object> createContext(
            final StenoEncoder encoder,
            final ILoggingEvent event,
            final ObjectMapper objectMapper,
            final List<String> contextKeys,
            final List<Object> contextValues,
            final boolean forceSafe) {
        final Map<String, Object> context = new LinkedHashMap<>();
        if (encoder.isInjectContextHost()) {
            context.put("host", StenoSerializationHelper.HOST_CONVERTER.convert(event));
        }
        if (encoder.isInjectContextProcess()) {
            context.put("processId", StenoSerializationHelper.PROCESS_CONVERTER.convert(event));
        }
        if (encoder.isInjectContextThread()) {
            context.put("threadId", StenoSerializationHelper.THREAD_CONVERTER.convert(event));
        }
        if (encoder.isInjectContextLogger()) {
            if (encoder.isCompressLoggerName()) {
                context.put("logger", StenoSerializationHelper.CONDENSED_LOGGER_CONVERTER.convert(event));
            } else {
                context.put("logger", StenoSerializationHelper.LOGGER_CONVERTER.convert(event));
            }
        }
        if (encoder.isInjectContextFile()) {
            context.put("file", StenoSerializationHelper.FILE_CONVERTER.convert(event));
        }
        if (encoder.isInjectContextClass()) {
            context.put("class", StenoSerializationHelper.CLASS_CONVERTER.convert(event));
        }
        if (encoder.isInjectContextMethod()) {
            context.put("method", StenoSerializationHelper.METHOD_CONVERTER.convert(event));
        }
        if (encoder.isInjectContextLine()) {
            context.put("line", StenoSerializationHelper.LINE_CONVERTER.convert(event));
        }
        final Iterator<String> injectContextMdcIterator = encoder.iteratorForInjectContextMdc();
        while (injectContextMdcIterator.hasNext()) {
            final String key = injectContextMdcIterator.next();
            final String value = event.getMDCPropertyMap().get(key);
            context.put(key, value);
        }
        if (contextKeys != null) {
            final int contextValuesLength = contextValues == null ? 0 : contextValues.size();
            for (int i = 0; i < contextKeys.size(); ++i) {
                final String key = contextKeys.get(i);
                final Object value = i < contextValuesLength ? contextValues.get(i) : null;
                if (StenoSerializationHelper.isSimpleType(value)) {
                    // Don't mess with simple types
                    context.put(key, value);
                } else if (!forceSafe) {
                    // Prepare complex types when not forcing safety. This is the only code that should throw
                    // and it is only invoked when forceSafe is false (and thus allowed to throw).
                    context.put(
                            key,
                            StenoSerializationHelper.prepareForSerialization(
                                    objectMapper,
                                    encoder,
                                    value));
                } else {
                    // For this method to be "safe" we don't trust serialization. This includes the
                    // toString implementation on the value. Where classes implement toString using
                    // toLogValue it is especially possible that if toLogValue causes encoding to fail
                    // that toString will also fail.
                    //
                    // Thus, invoking toString on a complex value may suppress the message about
                    // encoding failure and disable the encoder. That result can be disastrous with
                    // asynchronous encoders as the encoder thread is responsible for draining the
                    // event queue potentially permanently blocking all threads that log.
                    //
                    // Consequently, when forcing safety complex objects are wrapped in a LogReferenceOnly
                    // instance when executing with safe equal to true. This outputs only the instance id
                    // and class.
                    context.put(key, LogReferenceOnly.of(value).toString());
                }
            }
        }
        return context;
    }

    /**
     * Create a Steno compatible identifier.
     *
     * @since 1.7.0
     * @return New identifier as a <code>String</code>.
     */
    public static String createId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Determine if an object represents a primitive Json type. These include
     * instances of <code>Number</code>, <code>String</code> and <code>Boolean</code>.
     *
     * @since 1.7.0
     * @param obj The <code>Object</code> to analyze.
     * @return <code>True</code> if and only if the <code>Object</code> is a simple type.
     */
    public static boolean isSimpleType(final Object obj) {
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
     * Converter for host.
     * @since 1.7.0
     */
    public static final ClassicConverter HOST_CONVERTER = new HostConverter();
    /**
     * Converter for process.
     * @since 1.7.0
     */
    public static final ClassicConverter PROCESS_CONVERTER = new ProcessConverter();
    /**
     * Converter for thread.
     * @since 1.7.0
     */
    public static final ClassicConverter THREAD_CONVERTER = new ThreadConverter();
    /**
     * Converter for logger.
     * @since 1.7.0
     */
    public static final ClassicConverter LOGGER_CONVERTER = new LoggerConverter();
    /**
     * Converter for logger in condensed form.
     * @since 1.7.0
     */
    public static final ClassicConverter CONDENSED_LOGGER_CONVERTER = new LoggerConverter();
    /**
     * Converter for file.
     * @since 1.7.0
     */
    public static final ClassicConverter FILE_CONVERTER = new FileOfCallerConverter();
    /**
     * Converter for class.
     * @since 1.7.0
     */
    public static final ClassicConverter CLASS_CONVERTER = new ClassOfCallerConverter();
    /**
     * Converter for method.
     * @since 1.7.0
     */
    public static final ClassicConverter METHOD_CONVERTER = new MethodOfCallerConverter();
    /**
     * Converter for line.
     * @since 1.7.0
     */
    public static final ClassicConverter LINE_CONVERTER = new LineOfCallerConverter();

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
     *
     * @since 1.7.0
     */
    public enum StenoLevel {
        /**
         * Steno level debug. This maps to SLF4J debug and trace levels.
         *
         * @since 1.7.0
         */
        debug(Level.DEBUG, Level.TRACE),
        /**
         * Steno level info. This maps to SLF4J info level.
         *
         * @since 1.7.0
         */
        info(Level.INFO),
        /**
         * Steno level warn. This maps to the SLF4J warn level.
         *
         * @since 1.7.0
         */
        warn(Level.WARN),
        /**
         * Steno level crit (critical). This maps to the SLF4J error level.
         *
         * @since 1.7.0
         */
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
         * @since 1.7.0
         * @param logbackLevel The Logback <code>Level</code> to map to a <code>StenoLevel</code>.
         * @return The matching <code>StenoLevel</code> or null if one is not found.
         */
        public static StenoLevel findByLogbackLevel(final Level logbackLevel) {
            return LOGBACK_LEVEL_MAP.get(logbackLevel);
        }
    }

    private StenoSerializationHelper() {}

    private static final Map<Class<?>, Boolean> LOGGABLE_CLASSES = new ConcurrentHashMap<>();
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ").withZone(ZoneId.of("UTC"));
}
