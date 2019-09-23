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
import com.arpnetworking.steno.LogReferenceOnly;
import com.arpnetworking.steno.LogValueMapFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Helper functions and for safe serialziation.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 * @since 1.11.2
 */
public final class SafeSerializationHelper {

    /**
     * Create a serialization safe context based on the {@link StenoEncoder} configuration.
     *
     * @since 1.11.2
     * @param encoder The {@link StenoEncoder} instance.
     * @param event The {@link ILoggingEvent} instance.
     * @param objectMapper {@link ObjectMapper} instance.
     * @return {@link Map} with event context.
     */
    public static Map<String, Object> createSafeContext(
            final StenoEncoder encoder,
            final ILoggingEvent event,
            final ObjectMapper objectMapper) {
        return createSafeContext(encoder, event, objectMapper, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Create a serialization safe context based on the {@link StenoEncoder} configuration.
     *
     * @since 1.11.2
     * @param encoder The {@link StenoEncoder} instance.
     * @param event The {@link ILoggingEvent} instance.
     * @param objectMapper {@link ObjectMapper} instance.
     * @param contextKeys The additional user provided context keys.
     * @param contextValues The additional user provided context values matching the keys.
     * @return {@link Map} with event context.
     */
    public static Map<String, Object> createSafeContext(
            final StenoEncoder encoder,
            final ILoggingEvent event,
            final ObjectMapper objectMapper,
            @Nullable final List<String> contextKeys,
            @Nullable final List<Object> contextValues) {
        return StenoSerializationHelper.createContext(encoder, event, objectMapper, contextKeys, contextValues);
    }

    /**
     * Safely serialize a value.
     *
     * @since 1.11.2
     * @param encoder The {@link StenoEncoder} instance.
     * @param value The {@link Object} instance to safely serialize.
     */
    public static void safeEncodeValue(final StringBuilder encoder, @Nullable final Object value) {
        if (value == null) {
            encoder.append("null");
        } else if (value instanceof Map) {
            safeEncodeMap(encoder, (Map<?, ?>) value);
        } else if (value instanceof List) {
            safeEncodeList(encoder, (List<?>) value);
        } else if (value.getClass().isArray()) {
            safeEncodeArray(encoder, value);
        } else if (value instanceof LogValueMapFactory.LogValueMap) {
            safeEncodeLogValueMap(encoder, (LogValueMapFactory.LogValueMap) value);
        } else if (value instanceof Throwable) {
            safeEncodeThrowable(encoder, (Throwable) value);
        } else if (StenoSerializationHelper.isSimpleType(value)) {
            if (value instanceof Boolean) {
                encoder.append(BooleanNode.valueOf((Boolean) value).toString());
            } else if (value instanceof Double) {
                encoder.append(DoubleNode.valueOf((Double) value).toString());
            } else if (value instanceof Float) {
                encoder.append(FloatNode.valueOf((Float) value).toString());
            } else if (value instanceof Long) {
                encoder.append(LongNode.valueOf((Long) value).toString());
            } else if (value instanceof Integer) {
                encoder.append(IntNode.valueOf((Integer) value).toString());
            } else {
                encoder.append(new TextNode(value.toString()).toString());
            }
        } else {
            safeEncodeValue(encoder, LogReferenceOnly.of(value).toLogValue());
        }
    }

    /* package private */ static void safeEncodeThrowable(final StringBuilder encoder, final Throwable throwable) {
        encoder.append("{\"type\":\"")
                .append(throwable.getClass().getName())
                .append("\",\"message\":");
        safeEncodeValue(encoder, throwable.getMessage());
        encoder.append(",\"backtrace\":[");
        for (final StackTraceElement ste : throwable.getStackTrace()) {
            safeEncodeValue(encoder, ste.toString());
            encoder.append(",");
        }
        if (throwable.getStackTrace().length == 0) {
            encoder.append("]");
        } else {
            encoder.setCharAt(encoder.length() - 1, ']');
        }
        encoder.append(",\"data\":{");
        if (throwable.getSuppressed().length > 0) {
            encoder.append("\"suppressed\":[");
            for (final Throwable suppressed : throwable.getSuppressed()) {
                safeEncodeThrowable(encoder, suppressed);
                encoder.append(",");
            }
            encoder.setCharAt(encoder.length() - 1, ']');
            encoder.append(",");
        }
        if (throwable.getCause() != null) {
            encoder.append("\"cause\":");
            safeEncodeThrowable(encoder, throwable.getCause());
            encoder.append(",");
        }
        if (encoder.charAt(encoder.length() - 1) == ',') {
            encoder.setCharAt(encoder.length() - 1, '}');
        } else {
            encoder.append("}");
        }
        encoder.append("}");
    }

    /* package private */ static void safeEncodeMap(final StringBuilder encoder, final Map<?, ?> valueAsMap) {
        encoder.append("{");
        for (Map.Entry<?, ?> entry : valueAsMap.entrySet()) {
            encoder.append("\"")
                    .append(entry.getKey().toString())
                    .append("\":");
            safeEncodeValue(encoder, entry.getValue());
            encoder.append(",");
        }
        if (valueAsMap.isEmpty()) {
            encoder.append("}");
        } else {
            encoder.setCharAt(encoder.length() - 1, '}');
        }
    }

    /* package private */ static void safeEncodeList(final StringBuilder encoder, final List<?> valueAsList) {
        encoder.append("[");
        for (Object listValue : valueAsList) {
            safeEncodeValue(encoder, listValue);
            encoder.append(",");
        }
        if (valueAsList.isEmpty()) {
            encoder.append("]");
        } else {
            encoder.setCharAt(encoder.length() - 1, ']');
        }
    }

    /* package private */ static void safeEncodeArray(final StringBuilder encoder, final Object value) {
        encoder.append("[");
        for (int i = 0; i < Array.getLength(value); ++i) {
            safeEncodeValue(encoder, Array.get(value, i));
            encoder.append(",");
        }
        if (Array.getLength(value) == 0) {
            encoder.append("]");
        } else {
            encoder.setCharAt(encoder.length() - 1, ']');
        }
    }

    /* package private */ static void safeEncodeLogValueMap(
            final StringBuilder encoder,
            final LogValueMapFactory.LogValueMap logValueMap) {
        final Map<String, Object> safeLogValueMap = new LinkedHashMap<>();
        final Optional<Object> target = logValueMap.getTarget();
        safeLogValueMap.put("_id", target.isPresent() ? Integer.toHexString(System.identityHashCode(target.get())) : null);
        safeLogValueMap.put("_class", target.isPresent() ? target.get().getClass().getName() : null);
        safeEncodeValue(encoder, safeLogValueMap);
    }

    private SafeSerializationHelper() {}
}
