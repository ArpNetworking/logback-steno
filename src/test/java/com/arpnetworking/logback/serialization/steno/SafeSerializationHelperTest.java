/*
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
package com.arpnetworking.logback.serialization.steno;

import com.arpnetworking.steno.LogValueMapFactory;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for <code>SafeSerializationHelper</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class SafeSerializationHelperTest {

    @Test
    public void testSafeEncodeValueNull() {
        final StringBuilder stringBuilder = new StringBuilder();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, null);
        Assert.assertEquals("null", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueMap() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Map<String, String> map = new LinkedHashMap<>();
        map.put("A", "one");
        map.put("B", "two");
        SafeSerializationHelper.safeEncodeValue(stringBuilder, map);
        Assert.assertEquals("{\"A\":\"one\",\"B\":\"two\"}", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueMapEmpty() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Map<String, String> map = new LinkedHashMap<>();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, map);
        Assert.assertEquals("{}", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueList() {
        final StringBuilder stringBuilder = new StringBuilder();
        final List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        SafeSerializationHelper.safeEncodeValue(stringBuilder, list);
        Assert.assertEquals("[\"A\",\"B\"]", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueListEmpty() {
        final StringBuilder stringBuilder = new StringBuilder();
        final List<String> list = new ArrayList<>();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, list);
        Assert.assertEquals("[]", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueArray() {
        final StringBuilder stringBuilder = new StringBuilder();
        final String[] array = new String[2];
        array[0] = "A";
        array[1] = "B";
        SafeSerializationHelper.safeEncodeValue(stringBuilder, array);
        Assert.assertEquals("[\"A\",\"B\"]", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueArrayEmpty() {
        final StringBuilder stringBuilder = new StringBuilder();
        final String[] array = new String[0];
        SafeSerializationHelper.safeEncodeValue(stringBuilder, array);
        Assert.assertEquals("[]", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeLogValueMap() {
        final StringBuilder stringBuilder = new StringBuilder();
        final String target = "hello world";
        final LogValueMapFactory.LogValueMap logValueMap = LogValueMapFactory.builder(target).put("key", "value").build();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, logValueMap);
        final String actual = stringBuilder.toString();
        Assert.assertTrue(actual, stringBuilder.toString().matches("\\{\"_id\":\"[a-z0-9]+\",\"_class\":\"java.lang.String\"\\}"));
    }

    @Test
    public void testSafeEncodeLogValueMapNoTarget() {
        final StringBuilder stringBuilder = new StringBuilder();
        // CHECKSTYLE.OFF: RegexpSinglelineCheck - For testing
        final LogValueMapFactory.LogValueMap logValueMap = LogValueMapFactory.of("key", "value");
        // CHECKSTYLE.ON: RegexpSinglelineCheck
        SafeSerializationHelper.safeEncodeValue(stringBuilder, logValueMap);
        Assert.assertEquals("{\"_id\":null,\"_class\":null}", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeThrowable() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Throwable t = new Throwable("failure");
        SafeSerializationHelper.safeEncodeValue(stringBuilder, t);
        final String actual = stringBuilder.toString();
        // CHECKSTYLE.OFF: LineLengthCheck
        Assert.assertTrue(actual, actual.matches("\\{\"type\":\"java.lang.Throwable\",\"message\":\"failure\",\"backtrace\":\\[[^\\]]+\\],\"data\":\\{\\}\\}"));
        // CHECKSTYLE.ON: LineLengthCheck
    }

    @Test
    public void testSafeEncodeThrowableNoStackTrace() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Throwable t = new ThrowableWithoutStacktrace();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, t);
        // CHECKSTYLE.OFF: LineLengthCheck
        Assert.assertEquals("{\"type\":\"com.arpnetworking.logback.serialization.steno.SafeSerializationHelperTest$ThrowableWithoutStacktrace\",\"message\":\"failure\",\"backtrace\":[],\"data\":{}}", stringBuilder.toString());
        // CHECKSTYLE.ON: LineLengthCheck
    }

    @Test
    public void testSafeEncodeThrowableNoStackTraceWithSuppressedExceptions() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Throwable[] suppressed = new Throwable[1];
        suppressed[0] = new ThrowableWithoutStacktrace("suppressed");
        final Throwable t = new ThrowableWithoutStacktrace(suppressed);
        SafeSerializationHelper.safeEncodeValue(stringBuilder, t);
        // CHECKSTYLE.OFF: LineLengthCheck
        Assert.assertEquals("{\"type\":\"com.arpnetworking.logback.serialization.steno.SafeSerializationHelperTest$ThrowableWithoutStacktrace\",\"message\":\"failure\",\"backtrace\":[],\"data\":{\"suppressed\":[{\"type\":\"com.arpnetworking.logback.serialization.steno.SafeSerializationHelperTest$ThrowableWithoutStacktrace\",\"message\":\"suppressed\",\"backtrace\":[],\"data\":{}}]}}", stringBuilder.toString());
        // CHECKSTYLE.ON: LineLengthCheck
    }

    @Test
    public void testSafeEncodeThrowableNoStackTraceWithSuppressedExceptionsEmpty() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Throwable t = new ThrowableWithoutStacktrace(new Throwable[0]);
        SafeSerializationHelper.safeEncodeValue(stringBuilder, t);
        // CHECKSTYLE.OFF: LineLengthCheck
        Assert.assertEquals("{\"type\":\"com.arpnetworking.logback.serialization.steno.SafeSerializationHelperTest$ThrowableWithoutStacktrace\",\"message\":\"failure\",\"backtrace\":[],\"data\":{}}", stringBuilder.toString());
        // CHECKSTYLE.ON: LineLengthCheck
    }

    @Test
    public void testSafeEncodeThrowableNoStackTraceWithCause() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Throwable t = new ThrowableWithoutStacktrace(new ThrowableWithoutStacktrace("npe"));
        SafeSerializationHelper.safeEncodeValue(stringBuilder, t);
        // CHECKSTYLE.OFF: LineLengthCheck
        Assert.assertEquals("{\"type\":\"com.arpnetworking.logback.serialization.steno.SafeSerializationHelperTest$ThrowableWithoutStacktrace\",\"message\":\"failure\",\"backtrace\":[],\"data\":{\"cause\":{\"type\":\"com.arpnetworking.logback.serialization.steno.SafeSerializationHelperTest$ThrowableWithoutStacktrace\",\"message\":\"npe\",\"backtrace\":[],\"data\":{}}}}", stringBuilder.toString());
        // CHECKSTYLE.ON: LineLengthCheck
    }

    @Test
    public void testSafeEncodeValueSimpleTypeBoolean() {
        final StringBuilder stringBuilder = new StringBuilder();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, Boolean.TRUE);
        Assert.assertEquals("true", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueSimpleTypeDouble() {
        final StringBuilder stringBuilder = new StringBuilder();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, Double.valueOf(1.23d));
        Assert.assertEquals("1.23", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueSimpleTypeFloat() {
        final StringBuilder stringBuilder = new StringBuilder();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, Float.valueOf(1.23f));
        Assert.assertEquals("1.23", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueSimpleTypeLong() {
        final StringBuilder stringBuilder = new StringBuilder();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, Long.valueOf(100L));
        Assert.assertEquals("100", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueSimpleTypeInteger() {
        final StringBuilder stringBuilder = new StringBuilder();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, Integer.valueOf(100));
        Assert.assertEquals("100", stringBuilder.toString());
    }

    @Test
    public void testSafeEncodeValueSimpleTypeString() {
        final StringBuilder stringBuilder = new StringBuilder();
        SafeSerializationHelper.safeEncodeValue(stringBuilder, "hello world");
        Assert.assertEquals("\"hello world\"", stringBuilder.toString());
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        final Constructor<SafeSerializationHelper> constructor =
                SafeSerializationHelper.class.getDeclaredConstructor();
        Assert.assertNotNull(constructor);
        try {
            constructor.newInstance();
            Assert.fail("Static helper class should have private no-args constructor");
        } catch (final IllegalAccessException e) {
            constructor.setAccessible(true);
            final SafeSerializationHelper safeSerializationHelper = constructor.newInstance();
            Assert.assertNotNull(safeSerializationHelper);
        }
    }

    private static class ThrowableWithoutStacktrace extends Throwable {
        ThrowableWithoutStacktrace() {
            this("failure");
        }

        ThrowableWithoutStacktrace(final String message) {
            super(message, null, false, false);
        }

        ThrowableWithoutStacktrace(final Throwable cause) {
            super("failure", cause, false, false);
        }

        ThrowableWithoutStacktrace(final Throwable... suppressed) {
            super("failure", null, true, false);
            for (final Throwable t : suppressed) {
                addSuppressed(t);
            }
        }

        private static final long serialVersionUID = 3904974430218741247L;
    }
}
