/**
 * Copyright 2015 Groupon.com
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
package com.arpnetworking.logback.serialization.keyvalue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.arpnetworking.logback.KeyValueEncoder;
import com.arpnetworking.logback.StenoMarker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;

/**
 * Tests for <code>KeyValueSerializationHelper</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class KeyValueSerializationHelperTest {

    @Before
    public void setUp() throws IOException {
        _context = new LoggerContext();
        _context.start();
        _baos = new ByteArrayOutputStream();
        _encoder = new KeyValueEncoder();
        _encoder.init(_baos);
        _encoder.setContext(_context);
        _encoder.start();
    }

    @Test
    public void testCreateContext() {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> context = KeyValueSerializationHelper.createContext(
                _encoder,
                event,
                Collections.singletonList("foo"),
                Collections.singletonList("bar"));
        Assert.assertEquals(4, context.size());
        Assert.assertTrue(context.containsKey("host"));
        Assert.assertTrue(context.containsKey("threadId"));
        Assert.assertTrue(context.containsKey("processId"));
        Assert.assertTrue(context.containsKey("foo"));
        Assert.assertEquals("bar", context.get("foo"));
    }

    @Test
    public void testCreateContextEmpty() {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> context = KeyValueSerializationHelper.createContext(
                _encoder,
                event,
                Collections.emptyList(),
                Collections.emptyList());
        Assert.assertEquals(3, context.size());
        Assert.assertTrue(context.containsKey("host"));
        Assert.assertTrue(context.containsKey("threadId"));
        Assert.assertTrue(context.containsKey("processId"));
    }

    @Test
    public void testCreateContextKeysNull() {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> context = KeyValueSerializationHelper.createContext(
                _encoder,
                event,
                null,
                Collections.emptyList());
        Assert.assertEquals(3, context.size());
        Assert.assertTrue(context.containsKey("host"));
        Assert.assertTrue(context.containsKey("threadId"));
        Assert.assertTrue(context.containsKey("processId"));
    }

    @Test
    public void testCreateContextValuesNull() {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> context = KeyValueSerializationHelper.createContext(
                _encoder,
                event,
                Collections.emptyList(),
                null);
        Assert.assertEquals(3, context.size());
        Assert.assertTrue(context.containsKey("host"));
        Assert.assertTrue(context.containsKey("threadId"));
        Assert.assertTrue(context.containsKey("processId"));
    }

    @Test
    public void testCreateContextTooManyKeys() {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> context = KeyValueSerializationHelper.createContext(
                _encoder,
                event,
                Collections.singletonList("foo"),
                Collections.emptyList());
        Assert.assertEquals(4, context.size());
        Assert.assertTrue(context.containsKey("host"));
        Assert.assertTrue(context.containsKey("threadId"));
        Assert.assertTrue(context.containsKey("processId"));
        Assert.assertTrue(context.containsKey("foo"));
        Assert.assertNull(context.get("foo"));
    }

    @Test
    public void testCreateContextTooManyValues() {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> context = KeyValueSerializationHelper.createContext(
                _encoder,
                event,
                Collections.emptyList(),
                Collections.singletonList("foo"));
        Assert.assertEquals(3, context.size());
        Assert.assertTrue(context.containsKey("host"));
        Assert.assertTrue(context.containsKey("threadId"));
        Assert.assertTrue(context.containsKey("processId"));
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        final Constructor<KeyValueSerializationHelper> constructor =
                KeyValueSerializationHelper.class.getDeclaredConstructor();
        Assert.assertNotNull(constructor);
        try {
            constructor.newInstance();
            Assert.fail("Static helper class should have private no-args constructor");
        } catch (final IllegalAccessException e) {
            constructor.setAccessible(true);
            final KeyValueSerializationHelper keyValueSerializationHelper = constructor.newInstance();
            Assert.assertNotNull(keyValueSerializationHelper);
        }
    }

    private LoggerContext _context;
    private ByteArrayOutputStream _baos;
    private KeyValueEncoder _encoder;
}
