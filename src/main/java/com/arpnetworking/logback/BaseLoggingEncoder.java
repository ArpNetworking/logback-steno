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
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.slf4j.Marker;

import java.io.IOException;
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
    public void doEncode(final ILoggingEvent event) throws IOException {
        final Marker marker = event.getMarker();
        final String name = event.getMessage();
        final Object[] argumentArray = event.getArgumentArray();

        String output;
        try {
            output = encodeAsString(event, marker, name, argumentArray);
        } catch (final EncodingException ee) {
            output = encodeAsString(event, ee);
        }

        outputStream.write(output.getBytes("UTF8"));

        if (isImmediateFlush()) {
            outputStream.flush();
        }
    }

    /**
     * Safely encode an instance of <code>EncodingException</code>.
     *
     * @since 1.9.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param ee Instance of <code>EncodingException</code> to encode.
     * @return Encoded version of <code>EncodingException</code>.
     */
    protected abstract String encodeAsString(final ILoggingEvent event, final EncodingException ee);

    /**
     * Encode an array based message into a <code>String</code>.
     *
     * @since 1.7.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param keys Array of keys; indices must match value indices.
     * @param values Array of values; indices must match key indices.
     * @return Message encoded as a <code>String</code>.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildArrayMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable String[] keys,
            @Nullable Object[] values)
            throws EncodingException;

    /**
     * Encode a JSON array based message into a <code>String</code>.
     *
     * @since 1.7.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param keys Array of keys; indices must match value indices.
     * @param jsonValues Array of values encoded in JSON; indices must match key indices.
     * @return Message encoded as a <code>String</code>.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildArrayJsonMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable String[] keys,
            @Nullable String[] jsonValues)
            throws EncodingException;

    /**
     * Encode a map based message into a <code>String</code>.
     *
     * @since 1.7.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param map Map of key to value pairs.
     * @return Message encoded as a <code>String</code>.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildMapMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable Map<String, ? extends Object> map)
            throws EncodingException;

    /**
     * Encode a map of encoded JSON values based message into a <code>String</code>.
     *
     * @since 1.7.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param map Map of key to json encoded value pairs.
     * @return Message encoded as a <code>String</code>.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildMapJsonMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable Map<String, String> map)
            throws EncodingException;

    /**
     * Encode an object value based message into a <code>String</code>.
     *
     * @since 1.7.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param data Object to be serialized as the data.
     * @return Message encoded as a <code>String</code>.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildObjectMessage(
            ILoggingEvent event,
            @Nullable String eventName,
            @Nullable Object data)
            throws EncodingException;

    /**
     * Encode a JSON encoded object value based message into a <code>String</code>.
     *
     * @since 1.7.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonData JSON encoded object to be serialized as the data.
     * @return Message encoded as a <code>String</code>.
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
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param dataKeys List of data keys.
     * @param dataValues List of data values.
     * @param contextKeys List of context keys.
     * @param contextValues List of context values.
     * @return Message encoded as a <code>String</code>.
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
     * Encode a standard message into a <code>String</code>.
     *
     * @since 1.7.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @return Message encoded as a <code>String</code>.
     * @throws EncodingException If encoding fails for any reason.
     */
    protected abstract String buildStandardMessage(final ILoggingEvent event) throws EncodingException;

    /**
     * Determine whether the <code>marker</code> represents an array event.
     *
     * @since 1.0.0
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents an array event.
     */
    protected boolean isArrayStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.ARRAY_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents a JSON array event.
     *
     * @since 1.0.4
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents a JSON array event.
     */
    protected boolean isArrayJsonStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.ARRAY_JSON_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents a map event.
     *
     * @since 1.0.4
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents an array event.
     */
    protected boolean isMapStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.MAP_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents a JSON map event.
     *
     * @since 1.0.4
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents a JSON map event.
     */
    protected boolean isMapJsonStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.MAP_JSON_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents an object event.
     *
     * @since 1.1.0
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents an object event.
     */
    protected boolean isObjectStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.OBJECT_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents a JSON object event.
     *
     * @since 1.1.0
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents a JSON object event.
     */
    protected boolean isObjectJsonStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.OBJECT_JSON_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents a lists event.
     *
     * @since 1.3.0
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents a lists event.
     */
    protected boolean isListsStenoEvent(@Nullable final Marker marker) {
        return marker != null && marker.contains(StenoMarker.LISTS_MARKER);
    }

    @SuppressWarnings("unchecked")
    private String encodeAsString(
            final ILoggingEvent event,
            final Marker marker,
            final String name,
            final Object[] argumentArray)
            throws EncodingException {
        final String output;
        if (isListsStenoEvent(marker)) {
            output = buildListsMessage(
                    event,
                    name,
                    (List<String>) argumentArray[0],  // data keys
                    (List<Object>) argumentArray[1],  // data object values
                    (List<String>) argumentArray[2],  // context keys
                    (List<Object>) argumentArray[3]); // context object values
        } else if (isArrayStenoEvent(marker)) {
            output = buildArrayMessage(
                    event,
                    name,
                    (String[]) argumentArray[0],  // keys
                    (Object[]) argumentArray[1]); // object values
        } else if (isArrayJsonStenoEvent(marker)) {
            output = buildArrayJsonMessage(
                    event,
                    name,
                    (String[]) argumentArray[0],  // keys
                    (String[]) argumentArray[1]); // json values
        } else if (isMapStenoEvent(marker)) {
            output = buildMapMessage(
                    event,
                    name,
                    (Map<String, Object>) argumentArray[0]); // key to object value map
        } else if (isMapJsonStenoEvent(marker)) {
            output = buildMapJsonMessage(
                    event,
                    name,
                    (Map<String, String>) argumentArray[0]); // key to json value map
        } else if (isObjectStenoEvent(marker)) {
            output = buildObjectMessage(
                    event,
                    name,
                    argumentArray[0]); // data object value
        } else if (isObjectJsonStenoEvent(marker)) {
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
