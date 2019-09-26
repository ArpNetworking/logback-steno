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
package com.arpnetworking.steno;

import com.arpnetworking.logback.StenoMarker;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link Logger}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class LoggerTest {

    @Test
    public void testIsTraceEnabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        Assert.assertTrue(new Logger(slf4jLogger).isTraceEnabled());
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.reset(slf4jLogger);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        Assert.assertFalse(new Logger(slf4jLogger).isTraceEnabled());
        Mockito.verify(slf4jLogger).isTraceEnabled();
    }

    @Test
    public void testCreateTrace() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace()
            .setEvent(TEST_EVENT)
            .setMessage(TEST_MESSAGE)
            .addData(KEY1, VALUE1)
            .addContext(KEY2, VALUE2)
            .log();
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateTraceWithLambda() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(l -> {
            l.setEvent(TEST_EVENT)
                    .setMessage(TEST_MESSAGE)
                    .addData(KEY1, VALUE1)
                    .addContext(KEY2, VALUE2);
        });
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateTraceWithLambdaDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        Mockito.reset(slf4jLogger);
        new Logger(slf4jLogger).trace(l -> {
            l.setMessage(TEST_MESSAGE);
        });
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testCreateTraceWithException() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .setThrowable(TEST_EXCEPTION)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2),
                TEST_EXCEPTION);
    }

    @Test
    public void testCreateTraceDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        final LogBuilder logBuilder = new Logger(slf4jLogger).trace();
        Mockito.reset(slf4jLogger);
        logBuilder.log();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testTrace() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_MESSAGE);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testTraceWithThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceWithEvent() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testTraceWithEventAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

        @Test
        public void testTraceWithMap() {
            final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
            Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
            new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1);
            Mockito.verify(slf4jLogger).trace(
                    StenoMarker.ARRAY_MARKER,
                    TEST_EVENT,
                    new String[]{MESSAGE_KEY, KEY1},
                    new Object[]{TEST_MESSAGE, VALUE1});
        }

    @Test
    public void testTraceWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceWithExtraction() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testTraceWithExtractionAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceWithOneExplicitExtra() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testTraceWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceWithTwoExplicitExtras() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2});
    }

    @Test
    public void testTraceWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2},
                TEST_EXCEPTION);
    }

    @Test
    public void testIsDebugEnabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        Assert.assertTrue(new Logger(slf4jLogger).isDebugEnabled());
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.reset(slf4jLogger);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        Assert.assertFalse(new Logger(slf4jLogger).isDebugEnabled());
        Mockito.verify(slf4jLogger).isDebugEnabled();
    }

    @Test
    public void testCreateDebug() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateDebugWithLambda() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(l -> {
            l.setEvent(TEST_EVENT)
                    .setMessage(TEST_MESSAGE)
                    .addData(KEY1, VALUE1)
                    .addContext(KEY2, VALUE2);
        });
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateDebugWithLambdaDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        Mockito.reset(slf4jLogger);
        new Logger(slf4jLogger).debug(l -> {
            l.setMessage(TEST_MESSAGE);
        });
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testCreateDebugWithException() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .setThrowable(TEST_EXCEPTION)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2),
                TEST_EXCEPTION);
    }

    @Test
    public void testCreateDebugDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        final LogBuilder logBuilder = new Logger(slf4jLogger).debug();
        Mockito.reset(slf4jLogger);
        logBuilder.log();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDebug() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_MESSAGE);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testDebugWithThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugWithEvent() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testDebugWithEventAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugWithMap() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testDebugWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugWithExtraction() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testDebugWithExtractionAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugWithOneExplicitExtra() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testDebugWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugWithTwoExplicitExtras() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2});
    }

    @Test
    public void testDebugWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2},
                TEST_EXCEPTION);
    }

    @Test
    public void testIsInfoEnabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        Assert.assertTrue(new Logger(slf4jLogger).isInfoEnabled());
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.reset(slf4jLogger);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        Assert.assertFalse(new Logger(slf4jLogger).isInfoEnabled());
        Mockito.verify(slf4jLogger).isInfoEnabled();
    }

    @Test
    public void testCreateInfo() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateInfoWithLambda() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(l -> {
            l.setEvent(TEST_EVENT)
                    .setMessage(TEST_MESSAGE)
                    .addData(KEY1, VALUE1)
                    .addContext(KEY2, VALUE2);
        });
        Mockito.verify(slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateInfoWithLambdaDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        Mockito.reset(slf4jLogger);
        new Logger(slf4jLogger).info(l -> {
            l.setMessage(TEST_MESSAGE);
        });
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testCreateInfoWithException() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .setThrowable(TEST_EXCEPTION)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2),
                TEST_EXCEPTION);
    }

    @Test
    public void testCreateInfoDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        final LogBuilder logBuilder = new Logger(slf4jLogger).info();
        Mockito.reset(slf4jLogger);
        logBuilder.log();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testInfo() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_MESSAGE);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testInfoWithThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoWithEvent() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testInfoWithEventAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoWithMap() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testInfoWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoWithExtraction() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testInfoWithExtractionAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoWithOneExplicitExtra() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testInfoWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoWithTwoExplicitExtras() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2});
    }

    @Test
    public void testInfoWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2},
                TEST_EXCEPTION);
    }

    @Test
    public void testIsWEnabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        Assert.assertTrue(new Logger(slf4jLogger).isWarnEnabled());
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.reset(slf4jLogger);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        Assert.assertFalse(new Logger(slf4jLogger).isWarnEnabled());
        Mockito.verify(slf4jLogger).isWarnEnabled();
    }

    @Test
    public void testCreateWarn() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateWarnWithLambda() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(l -> {
            l.setEvent(TEST_EVENT)
                    .setMessage(TEST_MESSAGE)
                    .addData(KEY1, VALUE1)
                    .addContext(KEY2, VALUE2);
        });
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateWarnWithLambdaDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        Mockito.reset(slf4jLogger);
        new Logger(slf4jLogger).warn(l -> {
            l.setMessage(TEST_MESSAGE);
        });
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testCreateWarnWithException() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .setThrowable(TEST_EXCEPTION)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2),
                TEST_EXCEPTION);
    }

    @Test
    public void testCreateWarnDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        final LogBuilder logBuilder = new Logger(slf4jLogger).warn();
        Mockito.reset(slf4jLogger);
        logBuilder.log();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testWarn() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_MESSAGE);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testWarnWithThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnWithEvent() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testWarnWithEventAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnWithMap() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testWarnWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnWithExtraction() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testWarnWithExtractionAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnWithOneExplicitExtra() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testWarnWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnWithTwoExplicitExtras() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2});
    }

    @Test
    public void testWarnWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2},
                TEST_EXCEPTION);
    }

    @Test
    public void testIsErrorEnabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        Assert.assertTrue(new Logger(slf4jLogger).isErrorEnabled());
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.reset(slf4jLogger);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        Assert.assertFalse(new Logger(slf4jLogger).isErrorEnabled());
        Mockito.verify(slf4jLogger).isErrorEnabled();
    }

    @Test
    public void testCreateError() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).error(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateErrorWithLambda() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(l -> {
            l.setEvent(TEST_EVENT)
                    .setMessage(TEST_MESSAGE)
                    .addData(KEY1, VALUE1)
                    .addContext(KEY2, VALUE2);
        });
        Mockito.verify(slf4jLogger).error(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2));
    }

    @Test
    public void testCreateErrorWithLambdaDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        Mockito.reset(slf4jLogger);
        new Logger(slf4jLogger).error(l -> {
            l.setMessage(TEST_MESSAGE);
        });
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testCreateErrorWithException() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error()
                .setEvent(TEST_EVENT)
                .setMessage(TEST_MESSAGE)
                .setThrowable(TEST_EXCEPTION)
                .addData(KEY1, VALUE1)
                .addContext(KEY2, VALUE2)
                .log();
        Mockito.verify(slf4jLogger).error(
                StenoMarker.LISTS_MARKER,
                TEST_EVENT,
                Arrays.asList(MESSAGE_KEY, KEY1),
                Arrays.asList(TEST_MESSAGE, VALUE1),
                Collections.singletonList(KEY2),
                Collections.singletonList(VALUE2),
                TEST_EXCEPTION);
    }

    @Test
    public void testCreateErrorDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        final LogBuilder logBuilder = new Logger(slf4jLogger).error();
        Mockito.reset(slf4jLogger);
        logBuilder.log();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testError() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_MESSAGE);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testErrorWithThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorWithEvent() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testErrorWithEventAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorWithMap() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testErrorWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorWithExtraction() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testErrorWithExtractionAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorWithOneExplicitExtra() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1});
    }

    @Test
    public void testErrorWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorWithTwoExplicitExtras() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2});
    }

    @Test
    public void testErrorWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1, KEY2},
                new Object[]{TEST_MESSAGE, VALUE1, VALUE2},
                TEST_EXCEPTION);
    }

    @Test
    public void testDisabledTraceWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledTraceWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledTraceWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledTraceWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledDebugWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledDebugWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledDebugWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledDebugWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledInfoWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledInfoWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledInfoWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledInfoWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledWarnWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledWarnWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledWarnWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledWarnWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledErrorWithArraysAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledErrorWithMapAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, MAP_KEY1_VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledErrorWithOneExplicitExtraAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, KEY1, VALUE1, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testDisabledErrorWithTwoExplicitExtrasAndThrowable() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, KEY1, KEY2, VALUE1, VALUE2, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testTraceMessageNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace((String) null);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{null});
    }

    @Test
    public void testTraceThrowableNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_MESSAGE, (Throwable) null);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testTraceEventNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(null, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testTraceMapNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, (Map<String, Object>) null);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testTraceExtraNameArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, (String[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceExtraValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceExtraValueArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, (Object[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testTraceExplicitNameValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        new Logger(slf4jLogger).trace(TEST_EVENT, TEST_MESSAGE, (String) null, (Object) null);
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, null},
                new Object[]{TEST_MESSAGE, null});
    }

    @Test
    public void testDebugMessageNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug((String) null);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{null});
    }

    @Test
    public void testDebugThrowableNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_MESSAGE, (Throwable) null);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testDebugEventNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(null, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testDebugMapNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, (Map<String, Object>) null);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testDebugExtraNameArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, (String[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugExtraValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugExtraValueArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, (Object[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testDebugExplicitNameValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        new Logger(slf4jLogger).debug(TEST_EVENT, TEST_MESSAGE, (String) null, (Object) null);
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, null},
                new Object[]{TEST_MESSAGE, null});
    }

    @Test
    public void testInfoMapNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, (Map<String, Object>) null);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testInfoExtraNameArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, (String[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoExtraValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoExtraValueArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, (Object[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testInfoExplicitNameValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        new Logger(slf4jLogger).info(TEST_EVENT, TEST_MESSAGE, (String) null, (Object) null);
        Mockito.verify(slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, null},
                new Object[]{TEST_MESSAGE, null});
    }

    @Test
    public void testWarnMapNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, (Map<String, Object>) null);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testWarnExtraNameArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, (String[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnExtraValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnExtraValueArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, (Object[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testWarnExplicitNameValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        new Logger(slf4jLogger).warn(TEST_EVENT, TEST_MESSAGE, (String) null, (Object) null);
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, null},
                new Object[]{TEST_MESSAGE, null});
    }

    @Test
    public void testErrorMessageNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error((String) null);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{null});
    }

    @Test
    public void testErrorThrowableNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_MESSAGE, (Throwable) null);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testErrorEventNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(null, TEST_MESSAGE);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testErrorMapNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, (Map<String, Object>) null);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testErrorExtraNameArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, (String[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorExtraValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorExtraValueArrayNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, (Object[]) null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testErrorExplicitNameValueNull() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, (String) null, (Object) null);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, null},
                new Object[]{TEST_MESSAGE, null});
    }

    @Test
    public void testMapEmpty() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, Collections.<String, Object>emptyMap());
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE});
    }

    @Test
    public void testExtraNameArrayEmpty() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testExtraValueNameArrayEmptyWithVarArgs() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{}, null, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testExtraValueArrayEmpty() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{KEY1}, new Object[]{}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY, KEY1},
                new Object[]{TEST_MESSAGE},
                TEST_EXCEPTION);
    }

    @Test
    public void testExtraValueNameArrayEmpty() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        new Logger(slf4jLogger).error(TEST_EVENT, TEST_MESSAGE, new String[]{}, new Object[]{VALUE1}, TEST_EXCEPTION);
        Mockito.verify(slf4jLogger).error(
                StenoMarker.ARRAY_MARKER,
                TEST_EVENT,
                new String[]{MESSAGE_KEY},
                new Object[]{TEST_MESSAGE, VALUE1},
                TEST_EXCEPTION);
    }

    @Test
    public void testCreateKeysFromCollection() {
        Assert.assertNull(Logger.createKeysFromCollection(null));
        Assert.assertArrayEquals(new String[0], Logger.createKeysFromCollection(Collections.emptyList()));

        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromCollection(Collections.singletonList("Foo"), new String[]{})));
        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromCollection(Collections.singletonList("Foo"), (String[]) null)));

        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromCollection(null, "Foo")));
        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromCollection(Collections.emptyList(), "Foo")));

        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo", "Bar"},
                Logger.createKeysFromCollection(Collections.singletonList("Bar"), "Foo")));
    }

    @Test
    public void testCreateValuesFromCollection() {
        Assert.assertNull(Logger.createValuesFromCollection(null));
        Assert.assertArrayEquals(new String[0], Logger.createValuesFromCollection(Collections.emptyList()));

        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromCollection(Collections.singletonList("Foo"), new Object[]{})));
        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromCollection(Collections.singletonList("Foo"), (Object[]) null)));

        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromCollection(null, "Foo")));
        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromCollection(Collections.emptyList(), "Foo")));

        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo", "Bar"},
                Logger.createValuesFromCollection(Collections.singletonList("Bar"), "Foo")));
    }

    @Test
    public void testCreateKeysFromArray() {
        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromArray(new String[]{"Foo"}, new String[]{})));
        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromArray(new String[]{"Foo"}, (String[]) null)));

        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromArray(null, "Foo")));
        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo"},
                Logger.createKeysFromArray(new String[]{}, "Foo")));

        Assert.assertTrue(Arrays.equals(
                new String[]{"Foo", "Bar"},
                Logger.createKeysFromArray(new String[]{"Bar"}, "Foo")));
    }

    @Test
    public void testCreateValuesFromArray() {
        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromArray(new Object[]{"Foo"}, new Object[]{})));
        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromArray(new Object[]{"Foo"}, (Object[]) null)));

        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromArray(null, "Foo")));
        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo"},
                Logger.createValuesFromArray(new Object[]{}, "Foo")));

        Assert.assertTrue(Arrays.equals(
                new Object[]{"Foo", "Bar"},
                Logger.createValuesFromArray(new Object[]{"Bar"}, "Foo")));
    }

    @Test
    public void testExtractThrowable() {
        Assert.assertEquals(null, Logger.extractThrowable(null, null));
        Assert.assertEquals(null, Logger.extractThrowable(new String[]{}, null));
        Assert.assertEquals(null, Logger.extractThrowable(null, new Object[]{}));
        Assert.assertEquals(null, Logger.extractThrowable(new String[]{}, new Object[]{}));
        Assert.assertEquals(null, Logger.extractThrowable(new String[]{KEY1}, new Object[]{VALUE1}));
        Assert.assertEquals(null, Logger.extractThrowable(new String[]{KEY1}, new Object[]{}));
        Assert.assertEquals(null, Logger.extractThrowable(new String[]{KEY1}, null));

        Assert.assertEquals(null, Logger.extractThrowable(null, new Object[]{VALUE1}));
        Assert.assertEquals(null, Logger.extractThrowable(new String[]{}, new Object[]{VALUE1}));
        Assert.assertEquals(null, Logger.extractThrowable(new String[]{KEY1}, new Object[]{VALUE1, VALUE1}));

        Assert.assertEquals(TEST_EXCEPTION, Logger.extractThrowable(null, new Object[]{TEST_EXCEPTION}));
        Assert.assertEquals(TEST_EXCEPTION, Logger.extractThrowable(new String[]{}, new Object[]{TEST_EXCEPTION}));
        Assert.assertEquals(TEST_EXCEPTION, Logger.extractThrowable(new String[]{KEY1}, new Object[]{VALUE1, TEST_EXCEPTION}));
    }

    @Test
    public void testChompArray() {
        Assert.assertArrayEquals(
                new String[]{"a", "b", "c"},
                Logger.chompArray(
                        new String[]{"a", "b", "c"},
                        0));
        Assert.assertArrayEquals(
                new String[]{"a", "b"},
                Logger.chompArray(
                        new String[]{"a", "b", "c"},
                        1));
        Assert.assertArrayEquals(
                new String[]{},
                Logger.chompArray(
                        new String[]{"a", "b", "c"},
                        3));
        Assert.assertArrayEquals(
                new String[]{"a", "b", "c"},
                Logger.chompArray(
                        new String[]{"a", "b", "c"},
                        0));
        Assert.assertNull(
                Logger.chompArray(
                        null,
                        0));
        Assert.assertNull(
                Logger.chompArray(
                        null,
                        1));
    }

    private static final String TEST_MESSAGE = "test message";
    private static final String TEST_EVENT = "test_event";
    private static final Exception TEST_EXCEPTION = new NullPointerException("NPE!");
    private static final String KEY1 = "key1";
    private static final Object VALUE1 = "value1";
    private static final Map<String, Object> MAP_KEY1_VALUE1;
    private static final String KEY2 = "key2";
    private static final Object VALUE2 = "value2";

    public static final String MESSAGE_KEY = "message";

    static {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        MAP_KEY1_VALUE1 = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        MAP_KEY1_VALUE1.put(KEY1, VALUE1);
    }
}
