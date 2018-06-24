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

import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

/**
 * Container for Markers used by {@link com.arpnetworking.logback.StenoEncoder} to determine input argument format.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public final class StenoMarker {
    private static final String STENO_ARRAY_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.array";
    private static final String STENO_ARRAY_JSON_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.array.json";
    private static final String STENO_MAP_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.map";
    private static final String STENO_MAP_JSON_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.map.json";
    private static final String STENO_OBJECT_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.object";
    private static final String STENO_OBJECT_JSON_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.object.json";
    private static final String STENO_LISTS_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.lists";

    /**
     * Log event payload is an array of keys and and array of values.
     *
     * @since 1.0.0
     */
    public static final Marker ARRAY_MARKER = new BasicMarkerFactory().getMarker(STENO_ARRAY_MARKER_NAME);

    /**
     * Log event payload is an array of keys and and array of json encoded values.
     *
     * @since 1.0.4
     */
    public static final Marker ARRAY_JSON_MARKER = new BasicMarkerFactory().getMarker(STENO_ARRAY_JSON_MARKER_NAME);

    /**
     * Log event payload is a key to value map.
     *
     * @since 1.0.4
     */
    public static final Marker MAP_MARKER = new BasicMarkerFactory().getMarker(STENO_MAP_MARKER_NAME);

    /**
     * Log event payload is a key to json encoded value map.
     *
     * @since 1.0.4
     */
    public static final Marker MAP_JSON_MARKER = new BasicMarkerFactory().getMarker(STENO_MAP_JSON_MARKER_NAME);

    /**
     * Log event payload is an object to be serialized.
     *
     * @since 1.1.0
     */
    public static final Marker OBJECT_MARKER = new BasicMarkerFactory().getMarker(STENO_OBJECT_MARKER_NAME);

    /**
     * Log event payload is a serialized object.
     *
     * @since 1.1.0
     */
    public static final Marker OBJECT_JSON_MARKER = new BasicMarkerFactory().getMarker(STENO_OBJECT_JSON_MARKER_NAME);

    /**
     * Log event payload is two pairs of lists, the first for data keys and
     * value, the second for context keys and values.
     *
     * @since 1.3.0
     */
    public static final Marker LISTS_MARKER = new BasicMarkerFactory().getMarker(STENO_LISTS_MARKER_NAME);

    private StenoMarker() {
        throw new UnsupportedOperationException();
    }
}
