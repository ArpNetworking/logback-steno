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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Tests for <code>DefaultLogBuilder</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class DefaultLogBuilderTest {

    @Test
    public void testBuilder() {
        final Logger logger = Mockito.mock(Logger.class);
        final List<String> dataKeys = Lists.newArrayList(Logger.MESSAGE_DATA_KEY);
        final List<Object> dataValues = Lists.newArrayList("MyMessage");
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
                null,
                null,
                EXCEPTION);
    }

    @Test
    public void testBuilderWithData() {
        final Logger logger = Mockito.mock(Logger.class);
        final List<String> dataKeys = Lists.newArrayList(Logger.MESSAGE_DATA_KEY, "KEY1", "KEY2");
        final List<Object> dataValues = Lists.newArrayList("MyMessage", "VALUE1", "VALUE2");
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
                null,
                null,
                EXCEPTION);
    }

    @Test
    public void testBuilderWithContext() {
        final Logger logger = Mockito.mock(Logger.class);
        final List<String> dataKeys = Lists.newArrayList(Logger.MESSAGE_DATA_KEY);
        final List<Object> dataValues = Lists.newArrayList("MyMessage");
        final List<String> contextKeys = Lists.newArrayList("KEY1", "KEY2");
        final List<Object> contextValues = Lists.newArrayList("VALUE1", "VALUE2");
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
