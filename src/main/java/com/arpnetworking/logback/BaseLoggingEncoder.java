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
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param keys Array of keys; indices must match value indices.
     * @param values Array of values; indices must match key indices.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildArrayMessage(ILoggingEvent event, String eventName, String[] keys, Object[] values);

    /**
     * Encode a JSON based message into a <code>String</code>.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param eventName The name of the event.
     * @param jsonKey The key for the JSON value.
     * @param json The value encoded in JSON.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildJsonMessage(ILoggingEvent event, String eventName, String jsonKey, String json);

    /**
     * Encode a standard message into a <code>String</code>.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @return Message encoded as a <code>String</code>.
     */
    protected abstract String buildStandardMessage(ILoggingEvent event);

    /**
     * Determine whether the <code>marker</code> represents an array event.
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents an array event.
     */
    protected boolean isArrayStenoEvent(final Marker marker) {
        return marker != null && marker.contains(StenoMarker.ARRAY_MARKER);
    }

    /**
     * Determine whether the <code>marker</code> represents a JSON event.
     *
     * @param marker The <code>Marker</code> instance to evaluate.
     * @return True if and only if <code>marker</code> represents a JSON event.
     */
    protected boolean isJsonStenoEvent(final Marker marker) {
        return marker != null && marker.contains(StenoMarker.JSON_MARKER);
    }
}
