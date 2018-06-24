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

import java.util.Collections;

/**
 * Tests for <code>LogLevel</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class LogLevelTest {

    @Test
    public void testLogTrace() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        LogLevel.TRACE.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.verify(slf4jLogger).trace(
                StenoMarker.LISTS_MARKER,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogTraceDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isTraceEnabled();
        LogLevel.TRACE.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isTraceEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogDebug() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        LogLevel.DEBUG.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verify(slf4jLogger).debug(
                StenoMarker.LISTS_MARKER,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogDebugDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isDebugEnabled();
        LogLevel.DEBUG.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isDebugEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogInfo() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        LogLevel.INFO.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verify(slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogInfoDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isInfoEnabled();
        LogLevel.INFO.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogWarn() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        LogLevel.WARN.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verify(slf4jLogger).warn(
                StenoMarker.LISTS_MARKER,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogWarnDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isWarnEnabled();
        LogLevel.WARN.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isWarnEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogError() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        LogLevel.ERROR.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verify(slf4jLogger).error(
                StenoMarker.LISTS_MARKER,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testLogErrorDisabled() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.FALSE).when(slf4jLogger).isErrorEnabled();
        LogLevel.ERROR.log(
                slf4jLogger,
                "EVENT",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        Mockito.verify(slf4jLogger).isErrorEnabled();
        Mockito.verifyNoMoreInteractions(slf4jLogger);
    }

    @Test
    public void testIsEnabledTrace() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isTraceEnabled();
        Assert.assertTrue(LogLevel.TRACE.isEnabled(slf4jLogger));
    }

    @Test
    public void testIsEnabledDebug() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isDebugEnabled();
        Assert.assertTrue(LogLevel.DEBUG.isEnabled(slf4jLogger));
    }

    @Test
    public void testIsEnabledInfo() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isInfoEnabled();
        Assert.assertTrue(LogLevel.INFO.isEnabled(slf4jLogger));
    }

    @Test
    public void testIsEnabledWarn() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isWarnEnabled();
        Assert.assertTrue(LogLevel.WARN.isEnabled(slf4jLogger));
    }

    @Test
    public void testIsEnabledError() {
        final org.slf4j.Logger slf4jLogger = Mockito.mock(org.slf4j.Logger.class);
        Mockito.doReturn(Boolean.TRUE).when(slf4jLogger).isErrorEnabled();
        Assert.assertTrue(LogLevel.ERROR.isEnabled(slf4jLogger));
    }

    @Test
    public void testLogLevel() {
        for (final LogLevel expectedLevel : LogLevel.values()) {
            final LogLevel actualLevel = LogLevel.valueOf(expectedLevel.name());
            Assert.assertSame(expectedLevel, actualLevel);
        }
    }
}
