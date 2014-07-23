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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.common.base.Throwables;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Gil Markham (gil at groupon dot com)
 */
public class StenoEncoderTest {
    private StenoEncoder encoder;
    private ByteArrayOutputStream baos;
    private LoggerContext context;

    private static final JsonValidator VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
    private static final JsonNode STENO_SCHEMA;

    static {
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
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":1234,\"key2\":\"foo\"},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{\"suppressed\":[]}},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.UnsupportedOperationException\",\"message\":\"uoe!\",\"backtrace\":[],\"data\":{\"suppressed\":[],\"cause\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{\"suppressed\":[]}}}},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{\"suppressed\":[{\"type\":\"java.lang.UnsupportedOperationException\",\"message\":\"uoe!\",\"backtrace\":[],\"data\":{\"suppressed\":[]}}]}},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"exception\":{\"type\":\"java.lang.NullPointerException\",\"message\":\"npe!\",\"backtrace\":[],\"data\":{}},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        final DateTime now = new DateTime();
        final Object[] argArray = new Object[2];
        argArray[0] = new String[]{"key1"};
        argArray[1] = new Object[]{now};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name())
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"ID\"");
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":\"" + now.toString() + "\"},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{\"key1\":null,\"key2\":null},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        assertEquals("{\"time\":\"1970-01-01T00:00:00.000Z\",\"name\":\"logEvent\",\"level\":\"info\",\"data\":{},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n", logOutput);
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
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("Unknown exception: Mock Failure", logOutput);
    }

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
        final String expected = "{\"time\":\"2011-11-11T11:11:11.000Z\",\"name\":\"logEvent\",\"level\":\"debug\",\"data\":{\"json\":" + argArray[1] + "},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n";
        assertEquals(expected, logOutput);
        assertMatchesJsonSchema(logOutput);
    }

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
        final JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory);
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
        final String expected = "{\"time\":\"2011-11-11T11:11:11.000Z\",\"name\":\"log\",\"level\":\"debug\",\"data\":{\"message\":\"logEvent - foo = bar\"},\"context\":{\"threadId\":\"thread\"},\"id\":\"ID\"}\n";
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
        JsonFactory jsonFactory = Mockito.mock(JsonFactory.class);
        Mockito.doThrow(new IOException("Mock Failure")).when(jsonFactory).createGenerator(Mockito.any(Writer.class));
        encoder = new StenoEncoder(jsonFactory);
        encoder.init(baos);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        final String expected = "Unknown exception: Mock Failure";
        assertEquals(expected, logOutput);
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

    private void assertMatchesJsonSchema(final String json) {
        try {
            final JsonNode jsonNode = JsonLoader.fromString(json);
            final ProcessingReport report = VALIDATOR.validate(STENO_SCHEMA, jsonNode);
            assertTrue(report.isSuccess());
        } catch (final IOException | ProcessingException e) {
            fail("Failed with exception: " + e);
        }
    }
}
