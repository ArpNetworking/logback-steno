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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Gil Markham (gil at groupon dot com)
 */
@PrepareForTest({ FileAppender.class, SecureRandom.class, RandomizedTimeBasedFNATP.class })
@RunWith(PowerMockRunner.class)
public class RandomizedTimeBasedFNATPTest {

    @Test
    public void testComputeNextCheck() throws Exception {
        final DateTime dateTime = DateTime.parse("2014-05-05T00:00:00Z");
        @SuppressWarnings("unchecked")
        final RollingFileAppender<LoggingEvent> fileAppender = mock(RollingFileAppender.class);
        when(fileAppender.rawFileProperty()).thenReturn("application.log");

        final SecureRandom mockRandom = mock(SecureRandom.class);
        final InetAddress mockLocalhost = mock(InetAddress.class);
        when(mockLocalhost.getHostName()).thenReturn("localhost");
        mockStatic(InetAddress.class);
        when(InetAddress.getLocalHost()).thenReturn(mockLocalhost);
        whenNew(SecureRandom.class).withArguments("localhost".getBytes(StandardCharsets.UTF_8.name())).thenReturn(mockRandom);
        when(mockRandom.nextInt(3600000)).thenReturn(360000); // 6 minutes

        final TimeBasedRollingPolicy<LoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        final RandomizedTimeBasedFNATP<LoggingEvent> triggeringPolicy = new RandomizedTimeBasedFNATP<>();
        rollingPolicy.setContext(new LoggerContext());
        rollingPolicy.setFileNamePattern("application-%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setParent(fileAppender);
        // Strange bidirectional reference
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        triggeringPolicy.setCurrentTime(dateTime.getMillis());
        rollingPolicy.start();
        verify(mockRandom, times(1)).nextInt(3600000);
        verifyNew(SecureRandom.class).withArguments("localhost".getBytes(StandardCharsets.UTF_8.name()));
        // This should set the nextCheck to 2014-0505T01:00:00Z + random offset
        triggeringPolicy.computeNextCheck();
        final LoggingEvent event = new LoggingEvent();
        triggeringPolicy.setCurrentTime(DateTime.parse("2014-05-05T01:00:00Z").getMillis());
        assertFalse(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
        triggeringPolicy.setCurrentTime(DateTime.parse("2014-05-05T01:06:01Z").getMillis());
        assertTrue(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
    }

    @Test
    public void testSetMaxOffset() throws Exception {
        final DateTime dateTime = DateTime.parse("2014-05-05T00:00:00Z");
        @SuppressWarnings("unchecked")
        final RollingFileAppender<LoggingEvent> fileAppender = mock(RollingFileAppender.class);
        when(fileAppender.rawFileProperty()).thenReturn("application.log");

        final SecureRandom mockRandom = mock(SecureRandom.class);
        final InetAddress mockLocalhost = mock(InetAddress.class);
        when(mockLocalhost.getHostName()).thenReturn("localhost");
        mockStatic(InetAddress.class);
        when(InetAddress.getLocalHost()).thenReturn(mockLocalhost);
        whenNew(SecureRandom.class).withArguments("localhost".getBytes(StandardCharsets.UTF_8.name())).thenReturn(mockRandom);
        when(mockRandom.nextInt(10000)).thenReturn(3000); // 3 seconds

        final TimeBasedRollingPolicy<LoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        final RandomizedTimeBasedFNATP<LoggingEvent> triggeringPolicy = new RandomizedTimeBasedFNATP<>();
        rollingPolicy.setContext(new LoggerContext());
        rollingPolicy.setFileNamePattern("application-%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setParent(fileAppender);
        triggeringPolicy.setMaxOffsetInMillis(10000);
        assertEquals(10000, triggeringPolicy.getMaxOffsetInMillis());
        // Strange bidirectional reference
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        triggeringPolicy.setCurrentTime(dateTime.getMillis());
        rollingPolicy.start();
        verify(mockRandom, times(1)).nextInt(10000);
        // This should set the nextCheck to 2014-0505T01:00:00Z + random offset
        triggeringPolicy.computeNextCheck();
        final LoggingEvent event = new LoggingEvent();
        triggeringPolicy.setCurrentTime(DateTime.parse("2014-05-05T01:00:02Z").getMillis());
        assertFalse(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
        triggeringPolicy.setCurrentTime(DateTime.parse("2014-05-05T01:00:03Z").getMillis());
        assertTrue(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
    }

    @Test
    public void testUnknownHost() throws Exception {
        final DateTime dateTime = DateTime.parse("2014-05-05T00:00:00Z");
        @SuppressWarnings("unchecked")
        final RollingFileAppender<LoggingEvent> fileAppender = mock(RollingFileAppender.class);
        when(fileAppender.rawFileProperty()).thenReturn("application.log");

        final SecureRandom mockRandom = mock(SecureRandom.class);
        mockStatic(InetAddress.class);
        when(InetAddress.getLocalHost()).thenThrow(new UnknownHostException());
        whenNew(SecureRandom.class).withNoArguments().thenReturn(mockRandom);
        when(mockRandom.nextInt(10000)).thenReturn(3000); // 3 seconds

        final TimeBasedRollingPolicy<LoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        final RandomizedTimeBasedFNATP<LoggingEvent> triggeringPolicy = new RandomizedTimeBasedFNATP<>();
        rollingPolicy.setContext(new LoggerContext());
        rollingPolicy.setFileNamePattern("application-%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setParent(fileAppender);
        triggeringPolicy.setMaxOffsetInMillis(10000);
        assertEquals(10000, triggeringPolicy.getMaxOffsetInMillis());
        // Strange bidirectional reference
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        triggeringPolicy.setCurrentTime(dateTime.getMillis());
        rollingPolicy.start();
        verify(mockRandom, times(1)).nextInt(10000);
        // This should set the nextCheck to 2014-0505T01:00:00Z + random offset
        triggeringPolicy.computeNextCheck();
        final LoggingEvent event = new LoggingEvent();
        triggeringPolicy.setCurrentTime(DateTime.parse("2014-05-05T01:00:02Z").getMillis());
        assertFalse(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
        triggeringPolicy.setCurrentTime(DateTime.parse("2014-05-05T01:00:03Z").getMillis());
        assertTrue(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
    }
}
