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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;

/**
 * Tests for <code>RandomizedTimeBasedFNATP</code>.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class RandomizedTimeBasedFNATPTest {

    @Test
    public void testComputeNextCheck() throws Exception {
        final ZonedDateTime dateTime = ZonedDateTime.parse("2014-05-05T00:00:00Z");
        final RollingFileAppender<LoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setFile("application.log");

        final TimeBasedRollingPolicy<LoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        final RandomizedTimeBasedFNATP<LoggingEvent> triggeringPolicy = new RandomizedTimeBasedFNATP<>();
        rollingPolicy.setContext(new LoggerContext());
        rollingPolicy.setFileNamePattern("application-%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        triggeringPolicy.setCurrentTime(dateTime.toInstant().toEpochMilli());
        rollingPolicy.start();

        // This should set the nextCheck to 2014-0505T01:00:00Z + random offset
        triggeringPolicy.computeNextCheck();
        final LoggingEvent event = new LoggingEvent();
        triggeringPolicy.setCurrentTime(ZonedDateTime.parse("2014-05-05T00:59:59Z").toInstant().toEpochMilli());
        Assert.assertFalse(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
        triggeringPolicy.setCurrentTime(ZonedDateTime.parse("2014-05-05T02:00:00Z").toInstant().toEpochMilli());
        Assert.assertTrue(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
    }

    @Test
    public void testSetMaxOffset() throws Exception {
        final ZonedDateTime dateTime = ZonedDateTime.parse("2014-05-05T00:00:00Z");
        final RollingFileAppender<LoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setFile("application.log");
        final SecureRandom secureRandom = Mockito.mock(SecureRandom.class);
        Mockito.doReturn(Double.valueOf(0.1)).when(secureRandom).nextDouble();
        final SecureRandomProvider secureRandomProvider = Mockito.mock(SecureRandomProvider.class);
        Mockito.when(secureRandomProvider.get(Mockito.any(byte[].class))).thenReturn(secureRandom);

        final TimeBasedRollingPolicy<LoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        final RandomizedTimeBasedFNATP<LoggingEvent> triggeringPolicy = new RandomizedTimeBasedFNATP<>(
                secureRandomProvider,
                HostProvider.DEFAULT);
        rollingPolicy.setContext(new LoggerContext());
        rollingPolicy.setFileNamePattern("application-%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        triggeringPolicy.setCurrentTime(dateTime.toInstant().toEpochMilli());
        triggeringPolicy.setMaxOffsetInMillis(30000);
        Assert.assertEquals(30000, triggeringPolicy.getMaxOffsetInMillis());
        rollingPolicy.start();

        Mockito.verify(secureRandomProvider).get(Mockito.any(byte[].class));
        Mockito.verify(secureRandom).nextDouble();

        // This should set the nextCheck to 2014-0505T01:00:00Z + random offset
        triggeringPolicy.computeNextCheck();
        final LoggingEvent event = new LoggingEvent();
        triggeringPolicy.setCurrentTime(ZonedDateTime.parse("2014-05-05T01:00:02Z").toInstant().toEpochMilli());
        Assert.assertFalse(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
        triggeringPolicy.setCurrentTime(ZonedDateTime.parse("2014-05-05T01:00:03Z").toInstant().toEpochMilli());
        Assert.assertTrue(triggeringPolicy.isTriggeringEvent(new File("application.log"), event));
    }

    @Test
    public void testUnknownHost() throws Exception {
        final ZonedDateTime dateTime = ZonedDateTime.parse("2014-05-05T00:00:00Z");
        final RollingFileAppender<LoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setFile("application.log");
        final SecureRandom secureRandom = Mockito.mock(SecureRandom.class);
        Mockito.doReturn(Double.valueOf(0.1)).when(secureRandom).nextDouble();
        final SecureRandomProvider secureRandomProvider = Mockito.mock(SecureRandomProvider.class);
        Mockito.when(secureRandomProvider.get()).thenReturn(secureRandom);
        final HostProvider hostProvider = Mockito.mock(HostProvider.class);
        Mockito.when(hostProvider.get()).thenThrow(new UnknownHostException());

        final RandomizedTimeBasedFNATP<LoggingEvent> triggeringPolicy = new RandomizedTimeBasedFNATP<>(
                secureRandomProvider,
                hostProvider);
        triggeringPolicy.setMaxOffsetInMillis(30000);
        Assert.assertEquals(30000, triggeringPolicy.getMaxOffsetInMillis());

        final TimeBasedRollingPolicy<LoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(new LoggerContext());
        rollingPolicy.setFileNamePattern("application-%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        triggeringPolicy.setCurrentTime(dateTime.toInstant().toEpochMilli());
        rollingPolicy.start();

        Mockito.verify(secureRandomProvider).get();
        Mockito.verify(secureRandom).nextDouble();
    }
}
