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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.common.base.Throwables;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.arpnetworking.logback.annotations.LogRedact;

/**
 * Tests for <code>StenoEncoder</code>.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class StenoEncoderTest {
    private StenoEncoder encoder;
    private ByteArrayOutputStream baos;
    private LoggerContext context;

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
        } catch (final Throwable t) {
            processId = "<UNKNOWN>";
        }
        PROCESS_ID = processId;

        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (final Throwable t) {
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

    @Before
    public void setup() throws Exception {
        context = new LoggerContext();
        context.start();
        baos = new ByteArrayOutputStream();
        encoder = new StenoEncoder();
        encoder.setRedactEnabled(false);
        encoder.setRedactNull(true);
        encoder.setImmediateFlush(true);
        encoder.init(baos);
        encoder.setContext(context);
        encoder.start();
    }

    @Test
    public void testEncodeArray() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new Object[]{Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":1234,\"key2\":\"foo\"},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayWithException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        event.setThrowableProxy(new ThrowableProxy(new NullPointerException("npe!")));
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"")
                .replaceFirst("\"backtrace\":\\[[^\\]]+\\]", "\"backtrace\":[]");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{\"suppressed\":[]}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayWithCausedException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        event.setThrowableProxy(new ThrowableProxy(new UnsupportedOperationException("uoe!", new NullPointerException("npe!"))));
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"")
                .replaceAll("\"backtrace\":\\[[^\\]]+\\]", "\"backtrace\":[]");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.UnsupportedOperationException\",\"message\":\"uoe!\",\"backtrace\":[],\"data\":{\"suppressed\":[],\"cause\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{\"suppressed\":[]}}}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayWithSuppressedException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Throwable throwable = new NullPointerException("npe!");
        throwable.addSuppressed(new UnsupportedOperationException("uoe!"));
        event.setThrowableProxy(new ThrowableProxy(throwable));
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"")
                .replaceAll("\"backtrace\":\\[[^\\]]+\\]", "\"backtrace\":[]");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{\"suppressed\":[{\"type\":\"java.lang.UnsupportedOperationException\",\"message\":\"uoe!\",\"backtrace\":[],\"data\":{\"suppressed\":[]}}]}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON")
    public void testEncodeArrayWithNullSuppressedException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        event.setThrowableProxy(new ThrowableProxy(new NullPointerException("npe!")) {
            @Override
            @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS")
            public IThrowableProxy[] getSuppressed() {
                return null;
            }
        });
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{};
        argArray[1] = new Object[]{};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"")
                .replaceFirst("\"backtrace\":\\[[^\\]]+\\]", "\"backtrace\":[]");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayComplexValue() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime now = new DateTime(DateTimeZone.UTC);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "redacted"};
        argArray[1] = new Object[]{now, new Redacted("string", 1L)};
        event.setArgumentArray(argArray);
        encoder.setRedactEnabled(true);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\",\"redacted\":{\"stringValue\":\"<REDACTED>\",\"nullValue\":\"<REDACTED>\",\"longValue\":\"<REDACTED>\"}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = null;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":null,\"key2\":null},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayNullKeys() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = null;
        argArray[1] = new Object[]{Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new Object[]{Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeArrayJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new String[]{"{\"foo\":\"bar\"}", "[\"foo\",\"bar\"]"};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":{\"foo\":\"bar\"},\"key2\":[\"foo\",\"bar\"]},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayJsonNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = null;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":null,\"key2\":null},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayJsonNullKeys() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = null;
        argArray[1] = new String[]{"{\"foo\":\"bar\"}", "[\"foo\",\"bar\"]"};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeArrayJsonThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "key2"};
        argArray[1] = new String[]{"{\"foo\":\"bar\"}", "[\"foo\",\"bar\"]"};
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", Integer.valueOf(1234));
        map.put("key2", "foo");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":1234,\"key2\":\"foo\"},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapComplexValue() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime now = new DateTime(DateTimeZone.UTC);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", now);
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\"},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", null);
        map.put("key2", null);
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":null,\"key2\":null},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapNullMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
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
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeMapJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "{\"foo\":\"bar\"}");
        map.put("key2", "[\"foo\",\"bar\"]");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":{\"foo\":\"bar\"},\"key2\":[\"foo\",\"bar\"]},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapJsonNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", null);
        map.put("key2", null);
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":null,\"key2\":null},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapJsonNullMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeMapJsonThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
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
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("Unknown exception: Mock Failure", logOutput);
    }

    @Test
    public void testEncodeObject() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = new Widget("foo");
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"value\":\"foo\"},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeObjectThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = new Widget("foo");
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        Mockito.doThrow(new JsonGenerationException("Mock Failure")).when(objectMapper).writeValueAsString(Mockito.any(Object.class));
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeObjectNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeObjectJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = "{\"key\":\"value\"}";
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key\":\"value\"},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeObjectJsonThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = "{\"key\":\"value\"}";
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeObjectJsonNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEncodeJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMarker(StenoMarker.JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[2];
        argArray[0] = "json";
        argArray[1] = "{\"foo\":\"bar\"}";
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        final String expected = "{\"time\":\"2011-11-11T11:11:11.000Z\",\"name\":\"logEvent\",\"level\":\"debug\",\"data\":{\"json\":" + argArray[1] + "},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n";
        assertEquals(expected, logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEncodeJsonThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMarker(StenoMarker.JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[2];
        argArray[0] = "json";
        argArray[1] = "{\"foo\":\"bar\"}";
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeStandardEvent() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        final String expected = "{\"time\":\"2011-11-11T11:11:11.000Z\",\"name\":\"log\",\"level\":\"debug\",\"data\":{\"message\":\"logEvent - foo = bar\"},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n";
        assertEquals(expected, logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testEncodeStandardEventThrowsIOException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory, objectMapper);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        assertEquals(expected, logOutput);
    }

    @Test
    public void testEncodeStandardEventWithCustomEventName() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        encoder.setLogEventName("custom.name");
        Assert.assertEquals("custom.name", encoder.getLogEventName());
        Assert.assertTrue(encoder.isInjectContextHost());
        Assert.assertTrue(encoder.isInjectContextThread());
        Assert.assertTrue(encoder.isInjectContextProcess());
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        final String expected = "{\"time\":\"2011-11-11T11:11:11.000Z\",\"name\":\"custom.name\",\"level\":\"debug\",\"data\":{\"message\":\"logEvent - foo = bar\"},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n";
        assertEquals(expected, logOutput);
        assertMatchesJsonSchema(logOutput);
    }
    
    @Test
    public void testEncodeStandardEventWithSuppressDefaultContext() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        final DateTime eventTime = DateTime.parse("2011-11-11T11:11:11.000Z");
        event.setLevel(Level.TRACE);
        event.setMessage("logEvent - foo = {}");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        final Object[] argArray = new Object[1];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        Assert.assertFalse(encoder.isInjectContextClass());
        Assert.assertFalse(encoder.isInjectContextFile());
        Assert.assertFalse(encoder.isInjectContextLine());
        Assert.assertFalse(encoder.isInjectContextLogger());
        Assert.assertFalse(encoder.isInjectContextMethod());
        encoder.setInjectContextHost(false);
        Assert.assertFalse(encoder.isInjectContextHost());
        encoder.setInjectContextProcess(false);
        Assert.assertFalse(encoder.isInjectContextProcess());
        encoder.setInjectContextThread(false);
        Assert.assertFalse(encoder.isInjectContextThread());
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        final String expected = "{\"time\":\"2011-11-11T11:11:11.000Z\",\"name\":\"log\",\"level\":\"debug\",\"data\":{\"message\":\"logEvent - foo = bar\"},\"context\":{},\"id\":\"ID\"}\n";
        assertEquals(expected, logOutput);
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
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(eventTime.getMillis());
        encoder.setInjectContextHost(false);
        Assert.assertFalse(encoder.isInjectContextHost());
        encoder.setInjectContextProcess(false);
        Assert.assertFalse(encoder.isInjectContextProcess());
        encoder.setInjectContextThread(false);
        Assert.assertFalse(encoder.isInjectContextThread());
        encoder.setInjectContextClass(true);
        Assert.assertTrue(encoder.isInjectContextClass());
        encoder.setInjectContextFile(true);
        Assert.assertTrue(encoder.isInjectContextFile());
        encoder.setInjectContextLine(true);
        Assert.assertTrue(encoder.isInjectContextLine());
        encoder.setInjectContextLogger(true);
        Assert.assertTrue(encoder.isInjectContextLogger());
        encoder.setInjectContextMethod(true);
        Assert.assertTrue(encoder.isInjectContextMethod());
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        final String expected = "{\"time\":\"2011-11-11T11:11:11.000Z\",\"name\":\"log\",\"level\":\"debug\",\"data\":{\"message\":\"logEvent - foo = bar\"},\"context\":{\"logger\":\"loggerName\",\"file\":\"NativeMethodAccessorImpl.java\",\"class\":\"sun.reflect.NativeMethodAccessorImpl\",\"method\":\"invoke0\",\"line\":\"-2\"},\"id\":\"ID\"}\n";
        assertEquals(expected, logOutput);
        assertMatchesJsonSchema(logOutput);
    }

    @Test
    public void testRedactSettings() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        event.setTimeStamp(0);
        final DateTime now = new DateTime(DateTimeZone.UTC);
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1", "redacted"};
        argArray[1] = new Object[]{now, new Redacted("string", 1L)};
        event.setArgumentArray(argArray);
        encoder.setRedactEnabled(true);
        encoder.setRedactNull(false);
        encoder.setRedactNull(true);
        encoder.setRedactEnabled(false);
        encoder.doEncode(event);
        final String fullLogOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\",\"redacted\":{\"stringValue\":\"string\",\"nullValue\":null,\"longValue\":1}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n", fullLogOutput);
        Assert.assertFalse(encoder.isRedactEnabled());
        assertMatchesJsonSchema(fullLogOutput);
        baos.reset();
        encoder.setRedactEnabled(true);
        encoder.setRedactEnabled(false);
        encoder.setRedactNull(false);
        encoder.doEncode(event);
        final String nonRedactedWithNullLogOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\",\"redacted\":{\"stringValue\":\"string\",\"nullValue\":null,\"longValue\":1}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n",
                nonRedactedWithNullLogOutput);
        Assert.assertFalse(encoder.isRedactNull());
        assertMatchesJsonSchema(nonRedactedWithNullLogOutput);
        baos.reset();
        encoder.setRedactEnabled(true);
        encoder.setRedactNull(true);
        encoder.doEncode(event);
        final String redactedLogOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals(
                "{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\",\"redacted\":{\"stringValue\":\"<REDACTED>\",\"nullValue\":\"<REDACTED>\",\"longValue\":\"<REDACTED>\"}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n",
                redactedLogOutput);
        Assert.assertTrue(encoder.isRedactEnabled());
        assertMatchesJsonSchema(redactedLogOutput);
        baos.reset();
        encoder.setRedactEnabled(true);
        encoder.setRedactNull(true);
        encoder.doEncode(event);
        final String redactedLogOutput2 = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals(
                "{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\",\"redacted\":{\"stringValue\":\"<REDACTED>\",\"nullValue\":\"<REDACTED>\",\"longValue\":\"<REDACTED>\"}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n",
                redactedLogOutput2);
        Assert.assertTrue(encoder.isRedactEnabled());
        assertMatchesJsonSchema(redactedLogOutput2);

        baos.reset();
        encoder.setRedactEnabled(true);
        encoder.setRedactNull(false);
        encoder.doEncode(event);
        final String redactedWithNullLogOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals(
                "{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\",\"redacted\":{\"stringValue\":\"<REDACTED>\",\"nullValue\":null,\"longValue\":\"<REDACTED>\"}},\"context\":{\"host\":\"" + HOST_NAME + "\",\"processId\":\"" + PROCESS_ID + "\",\"threadId\":\"thread\"},\"id\":\"ID\"}\n",
                redactedWithNullLogOutput);
        Assert.assertFalse(encoder.isRedactNull());
        assertMatchesJsonSchema(redactedWithNullLogOutput);
    }

    @Test
    public void testIsSimpleType() {
        assertTrue(encoder.isSimpleType(null));
        assertTrue(encoder.isSimpleType("This is a String"));
        assertTrue(encoder.isSimpleType(Long.valueOf(1)));
        assertTrue(encoder.isSimpleType(Double.valueOf(3.14f)));
        assertTrue(encoder.isSimpleType(BigInteger.ONE));
        assertTrue(encoder.isSimpleType(Boolean.TRUE));
        assertFalse(encoder.isSimpleType(new Object()));
        assertFalse(encoder.isSimpleType(new long[]{}));
        assertFalse(encoder.isSimpleType(new double[]{}));
    }

    @Test
    public void testStenoLevel() {
        for (final StenoEncoder.StenoLevel level : StenoEncoder.StenoLevel.values()) {
            Assert.assertSame(level, StenoEncoder.StenoLevel.valueOf(level.toString()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStenoLevelDoesNotExist() {
        StenoEncoder.StenoLevel.valueOf("does_not_exist");
    }

    private void assertMatchesJsonSchema(final String json) {
        try {
            final JsonNode jsonNode = JsonLoader.fromString(
                    json.replaceAll("\"logger\":\"loggerName\",?", ""));
            final ProcessingReport report = VALIDATOR.validate(STENO_SCHEMA, jsonNode);
            assertTrue(report.toString(), report.isSuccess());
        } catch (final IOException | ProcessingException e) {
            fail("Failed with exception: " + e);
        }
    }

    public static class Redacted {
        @LogRedact
        private String stringValue;

        @LogRedact
        private String nullValue;

        private Long longValue;

        public Redacted(String stringValue, Long longValue) {
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
    }
}
