/*
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
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.slf4j.Marker;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Base encoder class containing methods to determine if a Steno marker is present.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public abstract class BaseLoggingEncoder extends LayoutWrappingEncoder<ILoggingEvent> {

    @Override
    public Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encode(final ILoggingEvent event) {
        final List<Marker> markers = event.getMarkerList();
        final String name = event.getMessage();
        final Object[] argumentArray = event.getArgumentArray();

        String output;
        try {
            output = encodeAsString(event, markers, name, argumentArray);
        } catch (final EncodingException ee) {
            output = encodeAsString(event, ee);
        }
        return encodeString(output);
    }

    byte[] encodeString(final String s) {
            return s.getBytes(getCharset());
    }

    /**
     * Safely encode an instance of {@link EncodingException}.
     *
     * @since 1.9.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param ee Instance of {@link EncodingException} to encode.
     * @return Encoded version of {@link EncodingException}.
     */
    protected abstract String encodeAsString(ILoggingEvent event, EncodingException ee);

    /**
     * Encode an array based message into a {@link String}.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param eventName The name of the event.
     * @param keys Array of keys; indices must match value indices.
     * @param values Array of values; indices must match key indices.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildArrayMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable String[] keys,
            @Nullable Object[] values)
            throws EncodingException;

    /**
     * Encode a JSON array based message into a {@link String}.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param eventName The name of the event.
     * @param keys Array of keys; indices must match value indices.
     * @param jsonValues Array of values encoded in JSON; indices must match key indices.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildArrayJsonMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable String[] keys,
            @Nullable String[] jsonValues)
            throws EncodingException;

    /**
     * Encode a map based message into a {@link String}.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param eventName The name of the event.
     * @param map Map of key to value pairs.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildMapMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable Map<String, ? extends Object> map)
            throws EncodingException;

    /**
     * Encode a map of encoded JSON values based message into a {@link String}.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param eventName The name of the event.
     * @param map Map of key to json encoded value pairs.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildMapJsonMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable Map<String, String> map)
            throws EncodingException;

    /**
     * Encode an object value based message into a {@link String}.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param eventName The name of the event.
     * @param data Object to be serialized as the data.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildObjectMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable Object data)
            throws EncodingException;

    /**
     * Encode a JSON encoded object value based message into a {@link String}.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param eventName The name of the event.
     * @param jsonData JSON encoded object to be serialized as the data.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildObjectJsonMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            String jsonData)
            throws EncodingException;

    /**
     * Encode a two pairs of lists representing data and context key-value pairs.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @param eventName The name of the event.
     * @param dataKeys List of data keys.
     * @param dataValues List of data values.
     * @param contextKeys List of context keys.
     * @param contextValues List of context values.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildListsMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable List<String> dataKeys,
            @Nullable List<Object> dataValues,
            @Nullable List<String> contextKeys,
            @Nullable List<Object> contextValues)
            throws EncodingException;

    /**
     * Encode a standard message into a {@link String}.
     *
     * @since 1.7.0
     *
     * @param event Instance of {@link ILoggingEvent}.
     * @return Message encoded as a {@link String}.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildStandardMessage(ILoggingEvent event) throws EncodingException;

    /**
     * Determine whether the {@code markers} represents an array event.
     *
     * @param markers The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents an array event.
     * @since 1.0.0
     */
    protected boolean isArrayStenoEvent(@Nullable final List<Marker> markers) {
        return markers != null && markers.stream().anyMatch(marker -> marker.contains(StenoMarker.ARRAY_MARKER));
    }

    /**
     * Determine whether the {@code marker} represents a JSON array event.
     *
     * @param markers The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents a JSON array event.
     * @since 1.0.4
     */
    protected boolean isArrayJsonStenoEvent(@Nullable final List<Marker> markers) {
        return markers != null && markers.stream().anyMatch(m -> m.contains(StenoMarker.ARRAY_JSON_MARKER));
    }

    /**
     * Determine whether the {@code marker} represents a map event.
     *
     * @param markers The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents an array event.
     * @since 1.0.4
     */
    protected boolean isMapStenoEvent(@Nullable final List<Marker> markers) {
        return markers != null && markers.stream().anyMatch(m -> m.contains(StenoMarker.MAP_MARKER));
    }

    /**
     * Determine whether the {@code marker} represents a JSON map event.
     *
     * @param markers The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents a JSON map event.
     * @since 1.0.4
     */
    protected boolean isMapJsonStenoEvent(@Nullable final List<Marker> markers) {
        return markers != null && markers.stream().anyMatch(m -> m.contains(StenoMarker.MAP_JSON_MARKER));
    }

    /**
     * Determine whether the {@code marker} represents an object event.
     *
     * @param markers The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents an object event.
     * @since 1.1.0
     */
    protected boolean isObjectStenoEvent(@Nullable final List<Marker> markers) {
        return markers != null && markers.stream().anyMatch(m -> m.contains(StenoMarker.OBJECT_MARKER));
    }

    /**
     * Determine whether the {@code marker} represents a JSON object event.
     *
     * @param markers The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents a JSON object event.
     * @since 1.1.0
     */
    protected boolean isObjectJsonStenoEvent(@Nullable final List<Marker> markers) {
        return markers != null && markers.stream().anyMatch(m -> m.contains(StenoMarker.OBJECT_JSON_MARKER));
    }

    /**
     * Determine whether the {@code marker} represents a lists event.
     *
     * @since 1.3.0
     *
     * @param marker The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents a lists event.
     */
    protected boolean isListsStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.LISTS_MARKER);
    }

    /**
     * Determine whether the {@code markers} represents a lists event.
     *
     * @since 1.4.0
     *
     * @param markers The {@link Marker} instance to evaluate.
     * @return True if and only if {@code marker} represents a lists event.
     */
    protected boolean isListsStenoEvent(@Nullable final List<Marker> markers) {
        return markers != null && markers.stream().anyMatch(m -> m.contains(StenoMarker.LISTS_MARKER));
    }

    @SuppressWarnings("unchecked")
    private String encodeAsString(
            final ILoggingEvent event,
            final List<Marker> markers,
            final String name,
            final Object[] argumentArray)
            throws EncodingException {
        final String output;
        if (isListsStenoEvent(markers)) {
            output = buildListsMessage(
                    event,
                    name,
                    (List<String>) argumentArray[0],  // data keys
                    (List<Object>) argumentArray[1],  // data object values
                    (List<String>) argumentArray[2],  // context keys
                    (List<Object>) argumentArray[3]); // context object values
        } else if (isArrayStenoEvent(markers)) {
            output = buildArrayMessage(
                    event,
                    name,
                    (String[]) argumentArray[0],  // keys
                    (Object[]) argumentArray[1]); // object values
        } else if (isArrayJsonStenoEvent(markers)) {
            output = buildArrayJsonMessage(
                    event,
                    name,
                    (String[]) argumentArray[0],  // keys
                    (String[]) argumentArray[1]); // json values
        } else if (isMapStenoEvent(markers)) {
            output = buildMapMessage(
                    event,
                    name,
                    (Map<String, Object>) argumentArray[0]); // key to object value map
        } else if (isMapJsonStenoEvent(markers)) {
            output = buildMapJsonMessage(
                    event,
                    name,
                    (Map<String, String>) argumentArray[0]); // key to json value map
        } else if (isObjectStenoEvent(markers)) {
            output = buildObjectMessage(
                    event,
                    name,
                    argumentArray[0]); // data object value
        } else if (isObjectJsonStenoEvent(markers)) {
            output = buildObjectJsonMessage(
                    event,
                    name,
                    (String) argumentArray[0]); // data json value
        } else {
            output = buildStandardMessage(event);
        }
        return output;
    }
}
