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
    private static final String STENO_JSON_MARKER_NAME = "com.arpnetworking.logback.stenoMarker.json";
    public static final Marker ARRAY_MARKER = new BasicMarkerFactory().getMarker(STENO_ARRAY_MARKER_NAME);
    public static final Marker JSON_MARKER = new BasicMarkerFactory().getMarker(STENO_JSON_MARKER_NAME);

    private StenoMarker() {
        throw new UnsupportedOperationException();
    }
}
