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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gil Markham (gil at groupon dot com)
 */
public class KeyValueEncoderTest {

    private KeyValueEncoder encoder;
    private ByteArrayOutputStream baos;
    private LoggerContext context;

    @Before
    public void setup() throws Exception {
        context = new LoggerContext();
        context.start();
        baos = new ByteArrayOutputStream();
        encoder = new KeyValueEncoder();
        encoder.setImmediateFlush(true);
        encoder.init(baos);
        final PatternLayout layout = new PatternLayout();
        layout.setPattern("[%d{dd MMM yyyy HH:mm:ss.SSS,UTC}] %t - %m%n");
        layout.setContext(context);
        layout.start();
        encoder.setLayout(layout);
        encoder.setContext(context);
    }

    @Test
    public void testEncodeArray() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = new Object[] {Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("[01 Jan 1970 00:00:00.000] thread - name=\"logEvent\", key1=\"1234\", key2=\"foo\"\n", logOutput);
    }

    @Test
    public void testEncodeJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = "json";
        argArray[1] = "{\"foo\":\"bar\"}";
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("[01 Jan 1970 00:00:00.000] thread - name=\"logEvent\", json=\"{\\\"foo\\\":\\\"bar\\\"}\"\n", logOutput);
    }

    @Test
    public void testEncodeStandardEvent() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMessage("logEvent: foo = {}");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("[01 Jan 1970 00:00:00.000] thread - logEvent: foo = bar\n", logOutput);
    }

    @Test
    public void testEncodeArrayEmptyKeysAndValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {};
        argArray[1] = new Object[] {};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("[01 Jan 1970 00:00:00.000] thread - name=\"logEvent\"\n", logOutput);
    }

    @Test
    public void testEncodeArrayStringNullKeys() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = null;
        argArray[1] = new Object[] {Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("[01 Jan 1970 00:00:00.000] thread - name=\"logEvent\"\n", logOutput);
    }

    @Test
    public void testEncodeJsonNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = "json";
        argArray[1] = null;
        event.setArgumentArray(argArray);
        encoder.doEncode(event);
        final String logOutput = baos.toString(StandardCharsets.UTF_8.name());
        assertEquals("[01 Jan 1970 00:00:00.000] thread - name=\"logEvent\", json=\"null\"\n", logOutput);
    }
}
