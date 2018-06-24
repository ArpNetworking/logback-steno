/*
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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for <code>LoggingEventWrapper</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class LoggingEventWrapperTest {

    /**
     * @deprecated Invokes deprecated methods on Logback classes.
     */
    @Deprecated
    @Test
    public void test() {
        final ILoggingEvent wrapped = Mockito.mock(ILoggingEvent.class);
        final LoggerContextVO loggerContext = Mockito.mock(LoggerContextVO.class);
        final IThrowableProxy throwableProxy = Mockito.mock(IThrowableProxy.class);
        final StackTraceElement[] callerData = new StackTraceElement[]{};
        final Marker marker = Mockito.mock(Marker.class);

        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, String> mdcPropertyMap = new HashMap<>();
        final Map<String, String> mdc = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        mdcPropertyMap.put("foo1", "bar1");
        mdc.put("foo2", "bar2");
        final String message = "The message";
        final Object[] arguments = new Object[] {};

        Mockito.doReturn("threadName").when(wrapped).getThreadName();
        Mockito.doReturn(Level.WARN).when(wrapped).getLevel();
        Mockito.doReturn("loggerName").when(wrapped).getLoggerName();
        Mockito.doReturn(loggerContext).when(wrapped).getLoggerContextVO();
        Mockito.doReturn(throwableProxy).when(wrapped).getThrowableProxy();
        Mockito.doReturn(callerData).when(wrapped).getCallerData();
        Mockito.doReturn(Boolean.TRUE).when(wrapped).hasCallerData();
        Mockito.doReturn(marker).when(wrapped).getMarker();
        Mockito.doReturn(mdcPropertyMap).when(wrapped).getMDCPropertyMap();
        Mockito.doReturn(mdc).when(wrapped).getMdc();
        Mockito.doReturn(1L).when(wrapped).getTimeStamp();

        final LoggingEventWrapper wrapper = new LoggingEventWrapper(wrapped, message, arguments);
        Assert.assertEquals("threadName", wrapper.getThreadName());
        Assert.assertEquals(Level.WARN, wrapper.getLevel());
        Assert.assertEquals("loggerName", wrapper.getLoggerName());
        Assert.assertSame(loggerContext, wrapper.getLoggerContextVO());
        Assert.assertSame(throwableProxy, wrapper.getThrowableProxy());
        Assert.assertSame(callerData, wrapper.getCallerData());
        Assert.assertTrue(wrapper.hasCallerData());
        Assert.assertSame(marker, wrapper.getMarker());
        Assert.assertEquals(mdcPropertyMap, wrapper.getMDCPropertyMap());
        Assert.assertEquals(mdc, wrapper.getMdc());
        Assert.assertEquals(1L, wrapped.getTimeStamp());

        Assert.assertEquals(message, wrapper.getMessage());
        Assert.assertTrue(Arrays.equals(arguments, wrapper.getArgumentArray()));
    }

    @Test
    public void testNullArgumentArray() {
        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(event, "The event", null);
        Assert.assertNull(wrapper.getArgumentArray());
    }

    @Test
    public void testPrepareForDeferredProcessing() {
        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(event, "The event", null);
        wrapper.prepareForDeferredProcessing();
        Mockito.verify(event).prepareForDeferredProcessing();
    }

    @Test
    public void testGetFormattedMessage() {
        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        final String message = "The message";
        final Object[] arguments = new Object[] {};
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(event, message, arguments);
        final String formattedMessage1 = wrapper.getFormattedMessage();
        final String formattedMessage2 = wrapper.getFormattedMessage();
        Assert.assertEquals(message, formattedMessage2);
        Assert.assertEquals(formattedMessage1, formattedMessage2);
    }

    @Test
    public void testGetFormattedMessageWithNullArguments() {
        final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        final String message = "The message";
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(event, message, null);
        final String formattedMessage1 = wrapper.getFormattedMessage();
        final String formattedMessage2 = wrapper.getFormattedMessage();
        Assert.assertEquals(message, formattedMessage2);
        Assert.assertEquals(formattedMessage1, formattedMessage2);
    }
}
