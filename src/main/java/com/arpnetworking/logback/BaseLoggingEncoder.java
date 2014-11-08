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
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.slf4j.Marker;

/**
 * Base encoder class containing methods to determine if a Steno marker is present.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public abstract class BaseLoggingEncoder extends LayoutWrappingEncoder<ILoggingEvent> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void doEncode(final ILoggingEvent event) throws IOException {
        String output;
        Marker marker = event.getMarker();

        // We could add other markers and handle Maps if we choose
        if (isArrayStenoEvent(marker)) {
            final Object[] argumentArray = event.getArgumentArray();
            final String name = event.getMessage();
            final String[] keys = (String[]) argumentArray[0];
            final Object[] values = (Object[]) argumentArray[1];
            output = buildArrayMessage(event, name, keys, values);
        } else if (isArrayJsonStenoEvent(marker)) {
            final Object[] argumentArray = event.getArgumentArray();
            final String name = event.getMessage();
            final String[] keys = (String[]) argumentArray[0];
            final String[] values = (String[]) argumentArray[1];
            output = buildArrayJsonMessage(event, name, keys, values);
        } else if (isMapStenoEvent(marker)) {
            final Object[] argumentArray = event.getArgumentArray();
            final String name = event.getMessage();
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) argumentArray[0];
            output = buildMapMessage(event, name, map);
        } else if (isMapJsonStenoEvent(marker)) {
            final Object[] argumentArray = event.getArgumentArray();
            final String name = event.getMessage();
            @SuppressWarnings("unchecked")
            final Map<String, String> map = (Map<String, String>) argumentArray[0];
            output = buildMapJsonMessage(event, name, map);
        } else if (isObjectStenoEvent(marker)) {
            final Object[] argumentArray = event.getArgumentArray();
            final String name = event.getMessage();
            output = buildObjectMessage(event, name, argumentArray[0]);
        } else if (isObjectJsonStenoEvent(marker)) {
            final Object[] argumentArray = event.getArgumentArray();
            final String name = event.getMessage();
            output = buildObjectJsonMessage(event, name, (String) argumentArray[0]);
        } else if (isJsonStenoEvent(marker)) {
            final Object[] argumentArray = event.getArgumentArray();
            final String name = event.getMessage();
            output = buildJsonMessage(event, name, (String) argumentArray[0], (String) argumentArray[1]);
        } else {
            output = buildStandardMessage(event);
        }

        outputStream.write(output.getBytes("UTF8"));

        if (isImmediateFlush()) {
            outputStream.flush();
        }
    }

    /**
     * Encode an array based message into a <code>String</code>.
     *
     * @since 1.0.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param keys Array of keys; indices must match value indices.
     * @param values Array of values; indices must match key indices.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildArrayMessage(ILoggingEvent event, String eventName, String[] keys, Object[] values);

    /**
     * Encode a JSON array based message into a <code>String</code>.
     *
     * @since 1.0.4
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param keys Array of keys; indices must match value indices.
     * @param jsonValues Array of values encoded in JSON; indices must match key indices.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildArrayJsonMessage(ILoggingEvent event, String eventName, String[] keys, String[] jsonValues);

    /**
     * Encode a map based message into a <code>String</code>.
     *
     * @since 1.0.4
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param map Map of key to value pairs.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildMapMessage(ILoggingEvent event, String eventName, Map<String, ? extends Object> map);

    /**
     * Encode a map of encoded JSON values based message into a <code>String</code>.
     *
     * @since 1.0.4
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param map Map of key to json encoded value pairs.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildMapJsonMessage(ILoggingEvent event, String eventName, Map<String, String> map);

    /**
     * Encode an object value based message into a <code>String</code>.
     *
     * @since 1.1.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param data Object to be serialized as the data.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildObjectMessage(ILoggingEvent event, String eventName, Object data);

    /**
     * Encode a JSON encoded object value based message into a <code>String</code>.
     *
     * @since 1.1.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonData JSON encoded object to be serialized as the data.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildObjectJsonMessage(ILoggingEvent event, String eventName, String jsonData);

    /**
     * Encode a JSON based message into a <code>String</code>.
     *
     * @since 1.0.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonKey The key for the JSON value.
     * @param json The value encoded in JSON.
     * @return Message encoded as a <code>String</code>.
     * @deprecated This functionality is now provided in more generically by other markers (as of 1.1.0).
     */
    @Deprecated
    protected abstract String buildJsonMessage(ILoggingEvent event, String eventName, String jsonKey, String json);

    /**
     * Encode a standard message into a <code>String</code>.
     *
     * @since 1.0.0
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildStandardMessage(ILoggingEvent event);

    /**
     * Determine whether the <code>marker</code> represents an array event.
     *
     * @since 1.0.0
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents an array event.
     */
    protected boolean isArrayStenoEvent(final Marker marker) {
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
    protected boolean isArrayJsonStenoEvent(final Marker marker) {
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
    protected boolean isMapStenoEvent(final Marker marker) {
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
    protected boolean isMapJsonStenoEvent(final Marker marker) {
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
    protected boolean isObjectStenoEvent(final Marker marker) {
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
    protected boolean isObjectJsonStenoEvent(final Marker marker) {
        return marker != null && marker.contains(StenoMarker.OBJECT_JSON_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents a JSON event.
     *
     * @since 1.0.0
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents a JSON event.
     * @deprecated This functionality is now provided in more generically by other markers (as of 1.1.0).
     */
    @Deprecated
    protected boolean isJsonStenoEvent(final Marker marker) {
        return marker != null && marker.contains(StenoMarker.JSON_MARKER);
    }
}
