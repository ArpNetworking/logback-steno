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

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

/**
 * @author Gil Markham (gil at groupon dot com)
 */
public class BaseLoggingEncoderTest {
    private BaseLoggingEncoder encoder = new TestLoggingEncoder();

    @Test
    public void testIsArrayStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.ARRAY_MARKER);
        assertTrue(encoder.isArrayStenoEvent(StenoMarker.ARRAY_MARKER));
        assertTrue(encoder.isArrayStenoEvent(rootMarker));
        assertFalse(encoder.isArrayStenoEvent(StenoMarker.JSON_MARKER));
    }

    @Test
    public void testIsJsonStenoEvent() throws Exception {
        final Marker rootMarker = new BasicMarkerFactory().getMarker("foo");
        rootMarker.add(StenoMarker.JSON_MARKER);
        assertTrue(encoder.isJsonStenoEvent(StenoMarker.JSON_MARKER));
        assertTrue(encoder.isJsonStenoEvent(rootMarker));
        assertFalse(encoder.isJsonStenoEvent(StenoMarker.ARRAY_MARKER));
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
