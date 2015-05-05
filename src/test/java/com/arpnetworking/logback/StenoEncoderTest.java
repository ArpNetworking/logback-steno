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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.arpnetworking.logback.annotations.LogRedact;
import com.arpnetworking.logback.annotations.LogValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.io.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests for <code>StenoEncoder</code>.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class StenoEncoderTest {

    @Before
    public void setUp() throws Exception {
        _context = new LoggerContext();
        _context.start();
        _baos = new ByteArrayOutputStream();
        _encoder = new StenoEncoder();
        _encoder.setRedactEnabled(false);
        _encoder.setRedactNull(true);
        _encoder.setImmediateFlush(true);
        _encoder.init(_baos);
        _encoder.setContext(_context);
        _encoder.start();
    }

    @Test
    public void testEncodeArray() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new Object[]{Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArray.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayWithException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        event.setThrowableProxy(new ThrowableProxy(new NullPointerException("npe!")));
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayWithException.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayWithCausedException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        event.setThrowableProxy(new ThrowableProxy(new UnsupportedOperationException("uoe!", new NullPointerException("npe!"))));
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayWithCausedException.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayWithSuppressedException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Throwable throwable = new NullPointerException("npe!");
        throwable.addSuppressed(new UnsupportedOperationException("uoe!"));
        event.setThrowableProxy(new ThrowableProxy(throwable));
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayWithSuppressedException.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    @SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON")
    public void testEncodeArrayWithNullSuppressedException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        event.setThrowableProxy(new ThrowableProxy(new NullPointerException("npe!")) {
            @Override
            @SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS")
            public IThrowableProxy[] getSuppressed() {
                return null;
            }
        });
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayWithNullSuppressedException.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayComplexValue() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime date = new DateTime(1415737981000L);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "redacted"};
        argArray[1] = new Object[]{date, new Redacted("string", 1L)};
        event.setArgumentArray(argArray);
        _encoder.setRedactEnabled(true);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayComplexValue.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayNullValues.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayNullKeys() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = null;
        argArray[1] = new Object[]{Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayNullKeys.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new Object[]{Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeArrayJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new String[]{"{\"foo\":\"bar\"}", "[\"foo\",\"bar\"]"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayJson.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayJsonNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayJsonNullValues.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayJsonNullKeys() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = null;
        argArray[1] = new String[]{"{\"foo\":\"bar\"}", "[\"foo\",\"bar\"]"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeArrayJsonNullKeys.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayJsonThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new String[]{"{\"foo\":\"bar\"}", "[\"foo\",\"bar\"]"};
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", Integer.valueOf(1234));
        map.put("key2", "foo");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeMap.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapComplexValue() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime date = new DateTime(1415737981000L);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", date);
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeMapComplexValue.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", null);
        map.put("key2", null);
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeMapNullValues.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapNullMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeMapNullMap.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", Integer.valueOf(1234));
        map.put("key2", "foo");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeMapJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "{\"foo\":\"bar\"}");
        map.put("key2", "[\"foo\",\"bar\"]");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeMapJson.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapJsonNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", null);
        map.put("key2", null);
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeMapJsonNullValues.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapJsonNullMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeMapJsonNullMap.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapJsonThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "{\"foo\":\"bar\"}");
        map.put("key2", "[\"foo\",\"bar\"]");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeObject() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = new Widget("foo");
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeObject.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeObjectThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = new Widget("foo");
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        Mockito.doThrow(new JsonGenerationException("Mock Failure")).when(objectMapper).writeValueAsString(Mockito.any(Object.class));
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        Assert.assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeObjectNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeObjectNull.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeObjectJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = "{\"key\":\"value\"}";
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeObjectJson.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeObjectJsonThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = "{\"key\":\"value\"}";
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        Assert.assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeObjectJsonNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeObjectJsonNull.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeLists() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[4];
        argArray[0] = ImmutableList.of("key1", "key2");
        argArray[1] = ImmutableList.of(Integer.valueOf(1234), "foo");
        argArray[2] = ImmutableList.of("CONTEXT_KEY1", "CONTEXT_KEY2");
        argArray[3] = ImmutableList.of("bar", Double.valueOf(3.14));
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeLists.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeListsThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[4];
        argArray[0] = ImmutableList.of("key1", "key2");
        argArray[1] = ImmutableList.of(Integer.valueOf(1234), "foo");
        argArray[2] = ImmutableList.of("CONTEXT_KEY1", "CONTEXT_KEY2");
        argArray[3] = ImmutableList.of("bar", Double.valueOf(3.14));
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        Assert.assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeListsEmpty() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[4];
        argArray[0] = Collections.emptyList();
        argArray[1] = Collections.emptyList();
        argArray[2] = Collections.emptyList();
        argArray[3] = Collections.emptyList();
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeListsEmpty.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeListsNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[4];
        argArray[0] = null;
        argArray[1] = null;
        argArray[2] = null;
        argArray[3] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeListsEmpty.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeListsValuesWithoutKeys() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[4];
        argArray[0] = ImmutableList.of("key1");
        argArray[1] = ImmutableList.of(Integer.valueOf(1234), "foo");
        argArray[2] = ImmutableList.of("CONTEXT_KEY1");
        argArray[3] = ImmutableList.of("bar", Double.valueOf(3.14));
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeListsValuesWithoutKeys.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeListsNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[4];
        argArray[0] = ImmutableList.of("key1", "key2");
        argArray[1] = null;
        argArray[2] = ImmutableList.of("CONTEXT_KEY1", "CONTEXT_KEY2");
        argArray[3] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeListsNullValues.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeListsComplexValue() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime date = new DateTime(1415737981000L);
        final Object[] argArray = new Object[4];
        argArray[0] = ImmutableList.of("key1");
        argArray[1] = ImmutableList.of(date);
        argArray[2] = ImmutableList.of("CONTEXT_KEY1");
        argArray[3] = ImmutableList.of(date);
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeListsComplexValue.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeStandardEvent() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeStandardEvent.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeStandardEventThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        _encoder = new StenoEncoder(jsonFactory, objectMapper);
        _encoder.init(_baos);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        Assert.assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeStandardEventWithCustomEventName() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        _encoder.setLogEventName("custom.name");
        Assert.assertEquals("custom.name", _encoder.getLogEventName());
        Assert.assertTrue(_encoder.isInjectContextHost());
        Assert.assertTrue(_encoder.isInjectContextThread());
        Assert.assertTrue(_encoder.isInjectContextProcess());
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeStandardEventWithCustomEventName.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeStandardEventWithCompressedLoggerName() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setLoggerName("foo.bar.loggerName");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        _encoder.setInjectContextLogger(true);
        _encoder.setCompressLoggerName(true);
        Assert.assertTrue(_encoder.isCompressLoggerName());
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeStandardEventWithCompressedLoggerName.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }
    
    @Test
    public void testEncodeStandardEventWithSuppressDefaultContext() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        Assert.assertFalse(_encoder.isInjectContextClass());
        Assert.assertFalse(_encoder.isInjectContextFile());
        Assert.assertFalse(_encoder.isInjectContextLine());
        Assert.assertFalse(_encoder.isInjectContextLogger());
        Assert.assertFalse(_encoder.isInjectContextMethod());
        _encoder.setInjectContextHost(false);
        Assert.assertFalse(_encoder.isInjectContextHost());
        _encoder.setInjectContextProcess(false);
        Assert.assertFalse(_encoder.isInjectContextProcess());
        _encoder.setInjectContextThread(false);
        Assert.assertFalse(_encoder.isInjectContextThread());
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeStandardEventWithSuppressDefaultContext.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeStandardEventWithMdcProperties() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        MDC.put("MDC_KEY", "MDC_VALUE");
        _encoder.addInjectContextMdc("MDC_KEY");
        Assert.assertEquals("MDC_KEY", Iterators.<String>getOnlyElement(_encoder.iteratorForInjectContextMdc()));
        Assert.assertTrue(_encoder.isInjectContextMdc("MDC_KEY"));
        Assert.assertFalse(_encoder.isInjectContextMdc("FOO_BAR"));
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeStandardEventWithMdcProperties.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeStandardEventWithIncludeOptionalContext() throws Exception {
        final Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(StenoEncoderTest.class);
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        final LoggingEvent event = new LoggingEvent(
                "com.arpnetworking.logback.StenoEncoderTest",
                logger,
                Level.DEBUG,
                "logEvent - foo = {}",
                null,
                argArray);
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLoggerName("loggerName");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        _encoder.setInjectContextHost(false);
        Assert.assertFalse(_encoder.isInjectContextHost());
        _encoder.setInjectContextProcess(false);
        Assert.assertFalse(_encoder.isInjectContextProcess());
        _encoder.setInjectContextThread(false);
        Assert.assertFalse(_encoder.isInjectContextThread());
        _encoder.setInjectContextClass(true);
        Assert.assertTrue(_encoder.isInjectContextClass());
        _encoder.setInjectContextFile(true);
        Assert.assertTrue(_encoder.isInjectContextFile());
        _encoder.setInjectContextLine(true);
        Assert.assertTrue(_encoder.isInjectContextLine());
        _encoder.setInjectContextLogger(true);
        Assert.assertTrue(_encoder.isInjectContextLogger());
        _encoder.setInjectContextMethod(true);
        Assert.assertTrue(_encoder.isInjectContextMethod());
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeStandardEventWithIncludeOptionalContext.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testRedactSettings() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime date = new DateTime(1415737981000L);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "redacted"};
        argArray[1] = new Object[]{date, new Redacted("string", 1L)};
        event.setArgumentArray(argArray);
        _encoder.setRedactEnabled(true);
        _encoder.setRedactNull(false);
        _encoder.setRedactNull(true);
        _encoder.setRedactEnabled(false);
        _encoder.doEncode(event);
        final String fullLogOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertFalse(_encoder.isRedactEnabled());
        assertOutput("StenoEncoderTest.testRedactSettings.1.json", fullLogOutput);
        assertMatchesJsonSchema(fullLogOutput);
        _baos.reset();
        _encoder.setRedactEnabled(true);
        _encoder.setRedactEnabled(false);
        _encoder.setRedactNull(false);
        _encoder.doEncode(event);
        final String nonRedactedWithNullLogOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertFalse(_encoder.isRedactNull());
        assertOutput("StenoEncoderTest.testRedactSettings.2.json", nonRedactedWithNullLogOutput);
        assertMatchesJsonSchema(nonRedactedWithNullLogOutput);
        _baos.reset();
        _encoder.setRedactEnabled(true);
        _encoder.setRedactNull(true);
        _encoder.doEncode(event);
        final String redactedLogOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(_encoder.isRedactEnabled());
        assertOutput("StenoEncoderTest.testRedactSettings.3.json", redactedLogOutput);
        assertMatchesJsonSchema(redactedLogOutput);
        _baos.reset();
        _encoder.setRedactEnabled(true);
        _encoder.setRedactNull(true);
        _encoder.doEncode(event);
        final String redactedLogOutput2 = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(_encoder.isRedactEnabled());
        assertOutput("StenoEncoderTest.testRedactSettings.4.json", redactedLogOutput2);
        assertMatchesJsonSchema(redactedLogOutput2);
        _baos.reset();
        _encoder.setRedactEnabled(true);
        _encoder.setRedactNull(false);
        _encoder.doEncode(event);
        final String redactedWithNullLogOutput = _baos.toString(StandardCharsets.UTF_8.name());
        Assert.assertFalse(_encoder.isRedactNull());
        assertOutput("StenoEncoderTest.testRedactSettings.5.json", redactedWithNullLogOutput);
        assertMatchesJsonSchema(redactedWithNullLogOutput);
    }

    @Test
    public void testEncodeLogValue() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime date = new DateTime(1415737981000L);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "logValue"};
        argArray[1] = new Object[]{date, new LogValueBean("FooBar!")};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("StenoEncoderTest.testEncodeLogValue.json", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    private static void assertOutput(final String expectedResource, final String actualOutput) {
        final String redactedOutput = actualOutput
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"<ID>\"")
                .replaceAll("\"host\":\"[^\"]+\"", "\"host\":\"<HOST>\"")
                .replaceAll("\"processId\":\"[^\"]+\"", "\"processId\":\"<PROCESS_ID>\"")
                .replaceAll("\"threadId\":\"[^\"]+\"", "\"threadId\":\"<THREAD_ID>\"")
                .replaceAll("\"backtrace\":\\[[^\\]]+\\]", "\"backtrace\":[]");
        try {
            final URL resource = Resources.getResource("com/arpnetworking/logback/" + expectedResource);
            Assert.assertEquals(Resources.toString(resource, StandardCharsets.UTF_8), redactedOutput);
        } catch (final IOException e) {
            Assert.fail("Failed with exception: " + e);
        }
    }

    private static void assertMatchesJsonSchema(final String json) {
        try {
            final ObjectNode rootNode = (ObjectNode) JsonLoader.fromString(json);
            final ObjectNode contextNode = (ObjectNode) rootNode.get("context");
            if (contextNode != null) {
                contextNode.remove("logger");
                contextNode.remove("MDC_KEY");
                contextNode.remove("CONTEXT_KEY1");
                contextNode.remove("CONTEXT_KEY2");
            }
            final ProcessingReport report = VALIDATOR.validate(STENO_SCHEMA, rootNode);
            Assert.assertTrue(report.toString(), report.isSuccess());
        } catch (final IOException | ProcessingException e) {
            Assert.fail("Failed with exception: " + e);
        }
    }

    private StenoEncoder _encoder;
    private ByteArrayOutputStream _baos;
    private LoggerContext _context;

    private static final JsonValidator VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
    private static final JsonNode STENO_SCHEMA;
    private static final String HOST_NAME;
    private static final String PROCESS_ID;

    static {
        final Pattern processIdPattern = Pattern.compile("^([\\d]+)@.*$");
        String processId;
        try {
            processId = ManagementFactory.getRuntimeMXBean().getName();
            final Matcher matcher = processIdPattern.matcher(processId);
            if (matcher.matches()) {
                processId = matcher.group(1);
            }
            // CHECKSTYLE.OFF: IllegalCatch - Prevent all failures
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            processId = "<UNKNOWN>";
        }
        PROCESS_ID = processId;

        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            // CHECKSTYLE.OFF: IllegalCatch - Prevent all failures
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            hostName = "<UNKNOWN>";
        }
        HOST_NAME = hostName;

        JsonNode jsonNode = null;
        try {
            jsonNode = JsonLoader.fromResource("/steno.schema.json");
        } catch (final IOException e) {
            Throwables.propagate(e);
        }
        STENO_SCHEMA = jsonNode;
    }

    // CHECKSTYLE.OFF: MemberName - Testing field annotations requires same name as getter.
    // CHECKSTYLE.OFF: HiddenField - Testing field annotations requires same name as getter.
    private static class Redacted {

        public Redacted(final String stringValue, final Long longValue) {
            this.stringValue = stringValue;
            this.longValue = longValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public String getNullValue() {
            return nullValue;
        }

        @LogRedact
        public Long getLongValue() {
            return longValue;
        }

        @LogRedact
        private String stringValue;
        @LogRedact
        private String nullValue;
        private Long longValue;
    }
    // CHECKSTYLE.ON: HiddenField
    // CHECKSTYLE.ON: MemberName

    private static class LogValueBean {

        public LogValueBean(final String value) {
            _value = value;
        }

        public String getValue() {
            return "This is not the value you are looking for";
        }

        @LogValue
        @Override
        public String toString() {
            return _value;
        }

        private final String _value;
    }
}
