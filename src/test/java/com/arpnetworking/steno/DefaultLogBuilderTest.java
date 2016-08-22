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
package com.arpnetworking.steno;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for <code>DefaultLogBuilder</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class DefaultLogBuilderTest {

    @Test
    public void testBuilder() {
        final Logger logger = Mockito.mock(Logger.class);
        final List<String> dataKeys = Collections.singletonList(Logger.MESSAGE_DATA_KEY);
        final List<Object> dataValues = Collections.singletonList("MyMessage");
        new DefaultLogBuilder(logger, LogLevel.DEBUG)
                .setEvent("MyEvent")
                .setMessage("MyMessage")
                .setThrowable(EXCEPTION)
                .log();
        Mockito.verify(logger).log(
                LogLevel.DEBUG,
                "MyEvent",
                dataKeys,
                dataValues,
                Collections.emptyList(),
                Collections.emptyList(),
                EXCEPTION);
    }

    @Test
    public void testBuilderNoData() {
        final Logger logger = Mockito.mock(Logger.class);
        new DefaultLogBuilder(logger, LogLevel.DEBUG)
                .setEvent("MyEvent")
                .setThrowable(EXCEPTION)
                .log();
        Mockito.verify(logger).log(
                LogLevel.DEBUG,
                "MyEvent",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                EXCEPTION);
    }

    @Test
    public void testBuilderWithData() {
        final Logger logger = Mockito.mock(Logger.class);
        final List<String> dataKeys = Arrays.asList(Logger.MESSAGE_DATA_KEY, "KEY1", "KEY2");
        final List<Object> dataValues = Arrays.asList("MyMessage", "VALUE1", "VALUE2");
        new DefaultLogBuilder(logger, LogLevel.DEBUG)
                .setEvent("MyEvent")
                .setMessage("MyMessage")
                .setThrowable(EXCEPTION)
                .addData("KEY1", "VALUE1")
                .addData("KEY2", "VALUE2")
                .log();
        Mockito.verify(logger).log(
                LogLevel.DEBUG,
                "MyEvent",
                dataKeys,
                dataValues,
                Collections.emptyList(),
                Collections.emptyList(),
                EXCEPTION);
    }

    @Test
    public void testBuilderWithContext() {
        final Logger logger = Mockito.mock(Logger.class);
        final List<String> dataKeys = Collections.singletonList(Logger.MESSAGE_DATA_KEY);
        final List<Object> dataValues = Collections.singletonList("MyMessage");
        final List<String> contextKeys = Arrays.asList("KEY1", "KEY2");
        final List<Object> contextValues = Arrays.asList("VALUE1", "VALUE2");
        new DefaultLogBuilder(logger, LogLevel.DEBUG)
                .setEvent("MyEvent")
                .setMessage("MyMessage")
                .setThrowable(EXCEPTION)
                .addContext("KEY1", "VALUE1")
                .addContext("KEY2", "VALUE2")
                .log();
        Mockito.verify(logger).log(
                LogLevel.DEBUG,
                "MyEvent",
                dataKeys,
                dataValues,
                contextKeys,
                contextValues,
                EXCEPTION);
    }

    @Test
    public void testBuilderWithDuplicateKeys() {
        final Logger logger = Mockito.mock(Logger.class);
        final List<String> dataKeys = Arrays.asList(Logger.MESSAGE_DATA_KEY, "D-KEY1", "D-KEY2");
        final List<Object> dataValues = Arrays.asList("MyMessage", "D-VALUE1B", "D-VALUE2B");
        final List<String> contextKeys = Arrays.asList("C-KEY1", "C-KEY2");
        final List<Object> contextValues = Arrays.asList("C-VALUE1B", "C-VALUE2B");
        new DefaultLogBuilder(logger, LogLevel.DEBUG)
                .setEvent("MyEvent")
                .setMessage("MyMessage")
                .setThrowable(EXCEPTION)
                .addData("D-KEY1", "D-VALUE1A")
                .addContext("C-KEY1", "C-VALUE1A")
                .addData("D-KEY2", "D-VALUE2A")
                .addContext("C-KEY2", "C-VALUE2A")
                .addData("D-KEY1", "D-VALUE1B")
                .addContext("C-KEY1", "C-VALUE1B")
                .addData("D-KEY2", "D-VALUE2B")
                .addContext("C-KEY2", "C-VALUE2B")
                .log();
        Mockito.verify(logger).log(
                LogLevel.DEBUG,
                "MyEvent",
                dataKeys,
                dataValues,
                contextKeys,
                contextValues,
                EXCEPTION);
    }

    @Test
    public void testToString() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        final String asString = new DefaultLogBuilder(new Logger(slf4jLogger), LogLevel.DEBUG)
                .setEvent("MyEvent")
                .setMessage("MyMessage")
                .setThrowable(EXCEPTION)
                .addData("Foo", "Bar")
                .toString();
        Assert.assertNotNull(asString);
        Assert.assertFalse(asString.isEmpty());
    }

    private static final Throwable EXCEPTION = new NullPointerException("NPE!");
}
