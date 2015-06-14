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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Tests for <code>BaseLoggingEncoder</code>.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class BaseLoggingEncoderTest {

    @Test
    public void testIsArrayStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.ARRAY_MARKER);
        Assert.assertTrue(_encoder.isArrayStenoEvent(StenoMarker.ARRAY_MARKER));
        Assert.assertTrue(_encoder.isArrayStenoEvent(rootMarker));
        Assert.assertFalse(_encoder.isArrayStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        Assert.assertFalse(_encoder.isArrayStenoEvent(StenoMarker.MAP_MARKER));
        Assert.assertFalse(_encoder.isArrayStenoEvent(StenoMarker.MAP_JSON_MARKER));
        Assert.assertFalse(_encoder.isArrayStenoEvent(StenoMarker.OBJECT_MARKER));
        Assert.assertFalse(_encoder.isArrayStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
    }

    @Test
    public void testIsArrayJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.ARRAY_JSON_MARKER);
        Assert.assertTrue(_encoder.isArrayJsonStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        Assert.assertTrue(_encoder.isArrayJsonStenoEvent(rootMarker));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(StenoMarker.ARRAY_MARKER));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(StenoMarker.MAP_MARKER));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(StenoMarker.MAP_JSON_MARKER));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(StenoMarker.OBJECT_MARKER));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
    }

    @Test
    public void testIsMapStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.MAP_MARKER);
        Assert.assertTrue(_encoder.isMapStenoEvent(StenoMarker.MAP_MARKER));
        Assert.assertTrue(_encoder.isMapStenoEvent(rootMarker));
        Assert.assertFalse(_encoder.isMapStenoEvent(StenoMarker.MAP_JSON_MARKER));
        Assert.assertFalse(_encoder.isMapStenoEvent(StenoMarker.ARRAY_MARKER));
        Assert.assertFalse(_encoder.isMapStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        Assert.assertFalse(_encoder.isMapStenoEvent(StenoMarker.OBJECT_MARKER));
        Assert.assertFalse(_encoder.isMapStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
    }

    @Test
    public void testIsMapJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.MAP_JSON_MARKER);
        Assert.assertTrue(_encoder.isMapJsonStenoEvent(StenoMarker.MAP_JSON_MARKER));
        Assert.assertTrue(_encoder.isMapJsonStenoEvent(rootMarker));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(StenoMarker.MAP_MARKER));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(StenoMarker.ARRAY_MARKER));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(StenoMarker.OBJECT_MARKER));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
    }

    @Test
    public void testIsObjectStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.OBJECT_MARKER);
        Assert.assertTrue(_encoder.isObjectStenoEvent(StenoMarker.OBJECT_MARKER));
        Assert.assertTrue(_encoder.isObjectStenoEvent(rootMarker));
        Assert.assertFalse(_encoder.isObjectStenoEvent(StenoMarker.MAP_MARKER));
        Assert.assertFalse(_encoder.isObjectStenoEvent(StenoMarker.MAP_JSON_MARKER));
        Assert.assertFalse(_encoder.isObjectStenoEvent(StenoMarker.ARRAY_MARKER));
        Assert.assertFalse(_encoder.isObjectStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        Assert.assertFalse(_encoder.isObjectStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
    }

    @Test
    public void testIsObjectJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.OBJECT_JSON_MARKER);
        Assert.assertTrue(_encoder.isObjectJsonStenoEvent(StenoMarker.OBJECT_JSON_MARKER));
        Assert.assertTrue(_encoder.isObjectJsonStenoEvent(rootMarker));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(StenoMarker.MAP_MARKER));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(StenoMarker.MAP_JSON_MARKER));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(StenoMarker.ARRAY_MARKER));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(StenoMarker.ARRAY_JSON_MARKER));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(StenoMarker.OBJECT_MARKER));
    }

    @Test
    public void testImmediateFlushEnabled() throws Exception {
        final OutputStream outputStream = Mockito.mock(OutputStream.class);
        _encoder.init(outputStream);

        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        _encoder.doEncode(event);

        Mockito.verify(outputStream).flush();
    }

    @Test
    public void testImmediateFlushDisabled() throws Exception {
        final OutputStream outputStream = Mockito.mock(OutputStream.class);
        _encoder.init(outputStream);
        _encoder.setImmediateFlush(false);

        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        _encoder.doEncode(event);

        Mockito.verify(outputStream, Mockito.never()).flush();
    }

    private BaseLoggingEncoder _encoder = new TestLoggingEncoder();

    /**
     * Implementation of <code>BaseLoggingEncoder</code> for tests.
     */
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

        @Override
        protected String buildListsMessage(
                final ILoggingEvent event,
                final String eventName,
                final List<String> dataKeys,
                final List<Object> dataValues,
                final List<String> contextKeys,
                final List<Object> contextValues) {

            return "lists message";
        }

        @Override
        protected String buildStandardMessage(final ILoggingEvent event) {
            return "standard message";
        }

        @Override
        protected String encodeAsString(final ILoggingEvent event, final EncodingException ee) {
            return ee.toString();
        }
    }
}
