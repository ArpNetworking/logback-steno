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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;

/**
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class LoggingEventWrapperTest {

    @Test
    @SuppressWarnings("deprecation")
    public void test() {
        final ILoggingEvent wrapped = Mockito.mock(ILoggingEvent.class);
        final LoggerContextVO loggerContext = Mockito.mock(LoggerContextVO.class);
        final IThrowableProxy throwableProxy = Mockito.mock(IThrowableProxy.class);
        final StackTraceElement[] callerData = new StackTraceElement[]{};
        final Marker marker = Mockito.mock(Marker.class);
        final Map<String, String> mdcPropertyMap = new HashMap<>();
        mdcPropertyMap.put("foo1", "bar1");
        final Map<String, String> mdc = new HashMap<>();
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
        Mockito.doReturn(Long.valueOf(1L)).when(wrapped).getTimeStamp();

        final LoggingEventWrapper wrapper = new LoggingEventWrapper(wrapped, message, arguments);
        assertEquals("threadName", wrapper.getThreadName());
        assertEquals(Level.WARN, wrapper.getLevel());
        assertEquals("loggerName", wrapper.getLoggerName());
        assertSame(loggerContext, wrapper.getLoggerContextVO());
        assertSame(throwableProxy, wrapper.getThrowableProxy());
        assertSame(callerData, wrapper.getCallerData());
        assertTrue(wrapper.hasCallerData());
        assertSame(marker, wrapper.getMarker());
        assertEquals(mdcPropertyMap, wrapper.getMDCPropertyMap());
        assertEquals(mdc, wrapper.getMdc());
        assertEquals(1L, wrapped.getTimeStamp());

        assertEquals(message, wrapper.getMessage());
        assertTrue(Arrays.equals(arguments, wrapper.getArgumentArray()));
    }

    @Test
    public void testNullArgumentArray() {
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(null, null, null);
        assertNull(wrapper.getArgumentArray());
    }

    @Test
    public void testPrepareForDeferredProcessing() {
        final ILoggingEvent wrapped = Mockito.mock(ILoggingEvent.class);
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(wrapped, null, null);
        wrapper.prepareForDeferredProcessing();
        Mockito.verify(wrapped).prepareForDeferredProcessing();
    }

    @Test
    public void testGetFormattedMessage() {
        final String message = "The message";
        final Object[] arguments = new Object[] {};
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(null, message, arguments);
        final String formattedMessage1 = wrapper.getFormattedMessage();
        final String formattedMessage2 = wrapper.getFormattedMessage();
        assertEquals(message, formattedMessage2);
        assertEquals(formattedMessage1, formattedMessage2);
    }

    @Test
    public void testGetFormattedMessageWithNullArguments() {
        final String message = "The message";
        final LoggingEventWrapper wrapper = new LoggingEventWrapper(null, message, null);
        final String formattedMessage1 = wrapper.getFormattedMessage();
        final String formattedMessage2 = wrapper.getFormattedMessage();
        assertEquals(message, formattedMessage2);
        assertEquals(formattedMessage1, formattedMessage2);
    }
}
