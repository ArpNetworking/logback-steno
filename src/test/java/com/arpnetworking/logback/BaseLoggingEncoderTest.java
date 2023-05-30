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
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

import java.util.List;
import java.util.Map;

/**
 * Tests for {@link BaseLoggingEncoder}.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class BaseLoggingEncoderTest {

    @Test
    public void testIsArrayStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.ARRAY_MARKER);
        Assert.assertTrue(_encoder.isArrayStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_MARKER)));
        Assert.assertTrue(_encoder.isArrayStenoEvent(Lists.newArrayList(rootMarker)));
        Assert.assertFalse(_encoder.isArrayStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_JSON_MARKER)));
        Assert.assertFalse(_encoder.isArrayStenoEvent(Lists.newArrayList(StenoMarker.MAP_MARKER)));
        Assert.assertFalse(_encoder.isArrayStenoEvent(Lists.newArrayList(StenoMarker.MAP_JSON_MARKER)));
        Assert.assertFalse(_encoder.isArrayStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_MARKER)));
        Assert.assertFalse(_encoder.isArrayStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_JSON_MARKER)));
    }

    @Test
    public void testIsArrayJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.ARRAY_JSON_MARKER);
        Assert.assertTrue(_encoder.isArrayJsonStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_JSON_MARKER)));
        Assert.assertTrue(_encoder.isArrayJsonStenoEvent(Lists.newArrayList(rootMarker)));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_MARKER)));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(Lists.newArrayList(StenoMarker.MAP_MARKER)));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(Lists.newArrayList(StenoMarker.MAP_JSON_MARKER)));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_MARKER)));
        Assert.assertFalse(_encoder.isArrayJsonStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_JSON_MARKER)));
    }

    @Test
    public void testIsMapStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.MAP_MARKER);
        Assert.assertTrue(_encoder.isMapStenoEvent(Lists.newArrayList(StenoMarker.MAP_MARKER)));
        Assert.assertTrue(_encoder.isMapStenoEvent(Lists.newArrayList(rootMarker)));
        Assert.assertFalse(_encoder.isMapStenoEvent(Lists.newArrayList(StenoMarker.MAP_JSON_MARKER)));
        Assert.assertFalse(_encoder.isMapStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_MARKER)));
        Assert.assertFalse(_encoder.isMapStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_JSON_MARKER)));
        Assert.assertFalse(_encoder.isMapStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_MARKER)));
        Assert.assertFalse(_encoder.isMapStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_JSON_MARKER)));
    }

    @Test
    public void testIsMapJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.MAP_JSON_MARKER);
        Assert.assertTrue(_encoder.isMapJsonStenoEvent(Lists.newArrayList(StenoMarker.MAP_JSON_MARKER)));
        Assert.assertTrue(_encoder.isMapJsonStenoEvent(Lists.newArrayList(rootMarker)));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(Lists.newArrayList(StenoMarker.MAP_MARKER)));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_MARKER)));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_JSON_MARKER)));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_MARKER)));
        Assert.assertFalse(_encoder.isMapJsonStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_JSON_MARKER)));
    }

    @Test
    public void testIsObjectStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.OBJECT_MARKER);
        Assert.assertTrue(_encoder.isObjectStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_MARKER)));
        Assert.assertTrue(_encoder.isObjectStenoEvent(Lists.newArrayList(rootMarker)));
        Assert.assertFalse(_encoder.isObjectStenoEvent(Lists.newArrayList(StenoMarker.MAP_MARKER)));
        Assert.assertFalse(_encoder.isObjectStenoEvent(Lists.newArrayList(StenoMarker.MAP_JSON_MARKER)));
        Assert.assertFalse(_encoder.isObjectStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_MARKER)));
        Assert.assertFalse(_encoder.isObjectStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_JSON_MARKER)));
        Assert.assertFalse(_encoder.isObjectStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_JSON_MARKER)));
    }

    @Test
    public void testIsObjectJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.OBJECT_JSON_MARKER);
        Assert.assertTrue(_encoder.isObjectJsonStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_JSON_MARKER)));
        Assert.assertTrue(_encoder.isObjectJsonStenoEvent(Lists.newArrayList(rootMarker)));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(Lists.newArrayList(StenoMarker.MAP_MARKER)));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(Lists.newArrayList(StenoMarker.MAP_JSON_MARKER)));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_MARKER)));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(Lists.newArrayList(StenoMarker.ARRAY_JSON_MARKER)));
        Assert.assertFalse(_encoder.isObjectJsonStenoEvent(Lists.newArrayList(StenoMarker.OBJECT_MARKER)));
    }

    private BaseLoggingEncoder _encoder = new TestLoggingEncoder();

    /**
     * Implementation of {@link BaseLoggingEncoder} for tests.
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
