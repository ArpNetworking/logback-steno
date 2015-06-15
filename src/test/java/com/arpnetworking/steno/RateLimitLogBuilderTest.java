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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Tests for <code>RateLimitLogBuilder</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class RateLimitLogBuilderTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        setupLogBuilder(_logBuilder);
    }

    @Test
    public void testSetters() {
        final RateLimitLogBuilder rateLimitLogBuilder = new RateLimitLogBuilder(_logBuilder, Duration.ofMinutes(1));

        Assert.assertSame(rateLimitLogBuilder, rateLimitLogBuilder.addContext("foo", "bar"));
        Mockito.verify(_logBuilder).addContext("foo", "bar");

        Assert.assertSame(rateLimitLogBuilder, rateLimitLogBuilder.addData("hello", "world"));
        Mockito.verify(_logBuilder).addData("hello", "world");

        Assert.assertSame(rateLimitLogBuilder, rateLimitLogBuilder.setEvent("e"));
        Mockito.verify(_logBuilder).setEvent("e");

        Assert.assertSame(rateLimitLogBuilder, rateLimitLogBuilder.setMessage("m"));
        Mockito.verify(_logBuilder).setMessage("m");

        final Throwable t = new NullPointerException("Test");
        Assert.assertSame(rateLimitLogBuilder, rateLimitLogBuilder.setThrowable(t));
        Mockito.verify(_logBuilder).setThrowable(t);
    }

    @Test
    public void testLogFirstTime() {
        final RateLimitLogBuilder rateLimitLogBuilder = new RateLimitLogBuilder(_logBuilder, Duration.ofMinutes(1));
        rateLimitLogBuilder.setMessage("m").log();
        Mockito.verify(_logBuilder).setMessage("m");
        Mockito.verify(_logBuilder).addData("_skipped", 0);
        Mockito.verify(_logBuilder).addData("_lastLogTime", Optional.empty());
        Mockito.verify(_logBuilder).log();
    }

    @Test
    public void testLogSecondTimeWithinDuration() {
        final RateLimitLogBuilder rateLimitLogBuilder = new RateLimitLogBuilder(_logBuilder, Duration.ofMinutes(1));
        rateLimitLogBuilder.setMessage("m").log();
        Mockito.verify(_logBuilder).setMessage("m");
        Mockito.verify(_logBuilder).addData("_skipped", 0);
        Mockito.verify(_logBuilder).addData("_lastLogTime", Optional.empty());
        Mockito.verify(_logBuilder).log();

        rateLimitLogBuilder.log();
        Mockito.verifyNoMoreInteractions(_logBuilder);
    }

    @Test
    public void testLogSecondTimeAfterDuration() throws InterruptedException {
        final RateLimitLogBuilder rateLimitLogBuilder = new RateLimitLogBuilder(_logBuilder, Duration.ofSeconds(1));
        final Instant beforeLastLog = Instant.now();
        rateLimitLogBuilder.setMessage("m").log();
        final Instant afterLastLog = Instant.now();

        Mockito.verify(_logBuilder).setMessage("m");
        Mockito.verify(_logBuilder).addData("_skipped", 0);
        Mockito.verify(_logBuilder).addData("_lastLogTime", Optional.empty());
        Mockito.verify(_logBuilder).log();

        rateLimitLogBuilder.log();
        Mockito.verifyNoMoreInteractions(_logBuilder);
        Mockito.reset(_logBuilder);
        setupLogBuilder(_logBuilder);
        Thread.sleep(2000);

        rateLimitLogBuilder.log();

        Mockito.verify(_logBuilder).addData("_skipped", 1);
        Mockito.verify(_logBuilder).addData(Mockito.eq("_lastLogTime"), _dataArgument.capture());
        Mockito.verify(_logBuilder).log();
        Mockito.verifyNoMoreInteractions(_logBuilder);

        final Optional<Instant> lastLogTime = _dataArgument.getValue();
        Assert.assertTrue(lastLogTime.isPresent());
        Assert.assertTrue(lastLogTime.get().toEpochMilli() >= beforeLastLog.toEpochMilli());
        Assert.assertTrue(lastLogTime.get().toEpochMilli() <= afterLastLog.toEpochMilli());
    }

    @Test
    public void testToString() {
        final RateLimitLogBuilder rateLimitLogBuilder = new RateLimitLogBuilder(_logBuilder, Duration.ofMinutes(1));
        final String asString = rateLimitLogBuilder.toString();
        Assert.assertNotNull(asString);
        Assert.assertFalse(asString.isEmpty());
    }

    private static void setupLogBuilder(final LogBuilder logBuilder) {
        Mockito.doReturn(logBuilder).when(logBuilder).addContext(Mockito.any(), Mockito.any());
        Mockito.doReturn(logBuilder).when(logBuilder).addData(Mockito.any(), Mockito.any());
        Mockito.doReturn(logBuilder).when(logBuilder).setEvent(Mockito.any());
        Mockito.doReturn(logBuilder).when(logBuilder).setMessage(Mockito.any());
        Mockito.doReturn(logBuilder).when(logBuilder).setThrowable(Mockito.any());
    }

    @Mock
    private LogBuilder _logBuilder;
    @Captor
    private ArgumentCaptor<Optional<Instant>> _dataArgument;
}
