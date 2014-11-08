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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.OutputStream;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

/**
 * Tests for <code>BaseLoggingEncoder</code>.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class BaseLoggingEncoderTest {
    private BaseLoggingEncoder encoder = new TestLoggingEncoder();

    @SuppressWarnings("deprecation")
    @Test
    public void testIsArrayStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.ARRAY_MARKER);
        assertTrue(encoder.isArrayStenoEvent(StenoMarker.ARRAY_MARKER));
        assertTrue(encoder.isArrayStenoEvent(rootMarker));
        assertFalse(encoder.isArrayStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        assertFalse(encoder.isArrayStenoEvent(StenoMarker.MAP_MARKER));
        assertFalse(encoder.isArrayStenoEvent(StenoMarker.MAP_JSON_MARKER));
        assertFalse(encoder.isArrayStenoEvent(StenoMarker.OBJECT_MARKER));
        assertFalse(encoder.isArrayStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
        assertFalse(encoder.isArrayStenoEvent(StenoMarker.JSON_MARKER));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsArrayJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.ARRAY_JSON_MARKER);
        assertTrue(encoder.isArrayJsonStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        assertTrue(encoder.isArrayJsonStenoEvent(rootMarker));
        assertFalse(encoder.isArrayJsonStenoEvent(StenoMarker.ARRAY_MARKER));
        assertFalse(encoder.isArrayJsonStenoEvent(StenoMarker.MAP_MARKER));
        assertFalse(encoder.isArrayJsonStenoEvent(StenoMarker.MAP_JSON_MARKER));
        assertFalse(encoder.isArrayJsonStenoEvent(StenoMarker.OBJECT_MARKER));
        assertFalse(encoder.isArrayJsonStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
        assertFalse(encoder.isArrayJsonStenoEvent(StenoMarker.JSON_MARKER));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsMapStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.MAP_MARKER);
        assertTrue(encoder.isMapStenoEvent(StenoMarker.MAP_MARKER));
        assertTrue(encoder.isMapStenoEvent(rootMarker));
        assertFalse(encoder.isMapStenoEvent(StenoMarker.MAP_JSON_MARKER));
        assertFalse(encoder.isMapStenoEvent(StenoMarker.ARRAY_MARKER));
        assertFalse(encoder.isMapStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        assertFalse(encoder.isMapStenoEvent(StenoMarker.OBJECT_MARKER));
        assertFalse(encoder.isMapStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
        assertFalse(encoder.isMapStenoEvent(StenoMarker.JSON_MARKER));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsMapJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.MAP_JSON_MARKER);
        assertTrue(encoder.isMapJsonStenoEvent(StenoMarker.MAP_JSON_MARKER));
        assertTrue(encoder.isMapJsonStenoEvent(rootMarker));
        assertFalse(encoder.isMapJsonStenoEvent(StenoMarker.MAP_MARKER));
        assertFalse(encoder.isMapJsonStenoEvent(StenoMarker.ARRAY_MARKER));
        assertFalse(encoder.isMapJsonStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        assertFalse(encoder.isMapJsonStenoEvent(StenoMarker.OBJECT_MARKER));
        assertFalse(encoder.isMapJsonStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
        assertFalse(encoder.isMapJsonStenoEvent(StenoMarker.JSON_MARKER));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsObjectStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.OBJECT_MARKER);
        assertTrue(encoder.isObjectStenoEvent(StenoMarker.OBJECT_MARKER));
        assertTrue(encoder.isObjectStenoEvent(rootMarker));
        assertFalse(encoder.isObjectStenoEvent(StenoMarker.MAP_MARKER));
        assertFalse(encoder.isObjectStenoEvent(StenoMarker.MAP_JSON_MARKER));
        assertFalse(encoder.isObjectStenoEvent(StenoMarker.ARRAY_MARKER));
        assertFalse(encoder.isObjectStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        assertFalse(encoder.isObjectStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
        assertFalse(encoder.isObjectStenoEvent(StenoMarker.JSON_MARKER));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsObjectJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.OBJECT_JSON_MARKER);
        assertTrue(encoder.isObjectJsonStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
        assertTrue(encoder.isObjectJsonStenoEvent(rootMarker));
        assertFalse(encoder.isObjectJsonStenoEvent(StenoMarker.MAP_MARKER));
        assertFalse(encoder.isObjectJsonStenoEvent(StenoMarker.MAP_JSON_MARKER));
        assertFalse(encoder.isObjectJsonStenoEvent(StenoMarker.ARRAY_MARKER));
        assertFalse(encoder.isObjectJsonStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        assertFalse(encoder.isObjectJsonStenoEvent(StenoMarker.OBJECT_MARKER));
        assertFalse(encoder.isObjectJsonStenoEvent(StenoMarker.JSON_MARKER));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.JSON_MARKER);
        assertTrue(encoder.isJsonStenoEvent(StenoMarker.JSON_MARKER));
        assertTrue(encoder.isJsonStenoEvent(rootMarker));
        assertFalse(encoder.isJsonStenoEvent(StenoMarker.ARRAY_MARKER));
        assertFalse(encoder.isJsonStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        assertFalse(encoder.isJsonStenoEvent(StenoMarker.MAP_MARKER));
        assertFalse(encoder.isJsonStenoEvent(StenoMarker.MAP_JSON_MARKER));
        assertFalse(encoder.isJsonStenoEvent(StenoMarker.OBJECT_MARKER));
        assertFalse(encoder.isJsonStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
    }

    @Test
    public void testImmediateFlushEnabled() throws Exception {
        final OutputStream outputStream = Mockito.mock(OutputStream.class);
        encoder.init(outputStream);

        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        encoder.doEncode(event);

        verify(outputStream).flush();
    }

    @Test
    public void testImmediateFlushDisabled() throws Exception {
        final OutputStream outputStream = Mockito.mock(OutputStream.class);
        encoder.init(outputStream);
        encoder.setImmediateFlush(false);

        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        encoder.doEncode(event);

        verify(outputStream, Mockito.never()).flush();
    }

    @SuppressWarnings("deprecation")
    public static class TestLoggingEncoder extends BaseLoggingEncoder {
        @Override
        protected String buildArrayMessage(
            final ILoggingEvent event,
            final String eventName,
            final String[] keys,
            final Object[] values) {

            return "array message";
        }

        @Override
        protected String buildArrayJsonMessage(
                final ILoggingEvent event,
                final String eventName,
                final String[] keys,
                final String[] values) {

            return "json array message";
        }

        @Override
        protected String buildMapMessage(
                final ILoggingEvent event,
                final String eventName,
                final Map<String, ? extends Object> map) {

            return "map message";
        }

        @Override
        protected String buildMapJsonMessage(
                final ILoggingEvent event,
                final String eventName,
                final Map<String, String> map) {

            return "json map message";
        }

        @Override
        protected String buildObjectMessage(
                final ILoggingEvent event,
                final String eventName,
                final Object data) {

            return "object message";
        }

        @Override
        protected String buildObjectJsonMessage(
                final ILoggingEvent event,
                final String eventName,
                final String jsonData) {

            return "json object message";
        }

        @SuppressWarnings("deprecation")
        @Override
        protected String buildJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final String jsonKey,
            final String json) {

            return "json message";
        }

        @Override
        protected String buildStandardMessage(final ILoggingEvent event) {
            return "standard message";
        }
    }
}
