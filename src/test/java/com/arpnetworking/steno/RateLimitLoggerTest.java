/*
 * Copyright 2016 Groupon.com
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
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.hamcrest.MockitoHamcrest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link RateLimitLogger}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class RateLimitLoggerTest {

    @Before
    public void setUp() {
        _mocks = MockitoAnnotations.openMocks(this);
        Mockito.doReturn(Boolean.FALSE).when(_slf4jLogger).isDebugEnabled();
        Mockito.doReturn(Boolean.TRUE).when(_slf4jLogger).isInfoEnabled();
    }

    @After
    public void tearDown() throws Exception {
        _mocks.close();
    }

    @Test
    public void testLogBuilderFirstTime() {
        final Logger rateLimitLogger = new RateLimitLogger(_slf4jLogger, Duration.ofMinutes(1), Clock.systemUTC());
        rateLimitLogger.info().setMessage("m").log();
        Mockito.verify(_slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                null,
                Arrays.asList("message", "_skipped", "_lastLogTime"),
                Arrays.asList("m", 0, null),
                Collections.emptyList(),
                Collections.emptyList());
    }

    @Test
    public void testLogBuilderSecondTimeWithinDuration() {
        final Logger rateLimitLogger = new RateLimitLogger(_slf4jLogger, Duration.ofMinutes(1), Clock.systemUTC());
        rateLimitLogger.info().setMessage("m1").log();
        Mockito.verify(_slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                null,
                Arrays.asList("message", "_skipped", "_lastLogTime"),
                Arrays.asList("m1", 0, null),
                Collections.emptyList(),
                Collections.emptyList());

        rateLimitLogger.info().setMessage("m2").log();
        Mockito.verify(_slf4jLogger, Mockito.atLeastOnce()).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(_slf4jLogger);
    }

    @Test
    public void testLogBuilderSecondTimeAfterDuration() throws InterruptedException {
        final Logger rateLimitLogger = new RateLimitLogger(_slf4jLogger, Duration.ofSeconds(1), Clock.systemUTC());
        final Instant beforeLastLog = Instant.now();
        rateLimitLogger.info().setMessage("m1").log();
        final Instant afterLastLog = Instant.now();
        Mockito.verify(_slf4jLogger, Mockito.atLeastOnce()).isInfoEnabled();
        Mockito.verify(_slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                null,
                Arrays.asList("message", "_skipped", "_lastLogTime"),
                Arrays.asList("m1", 0, null),
                Collections.emptyList(),
                Collections.emptyList());

        Mockito.verifyNoMoreInteractions(_slf4jLogger);
        Thread.sleep(500);
        rateLimitLogger.info().setMessage("m2").log();  // Rate limited but counted
        rateLimitLogger.debug().setMessage("m3").log(); // Dropped and not counted
        Mockito.reset(_slf4jLogger);
        Mockito.doReturn(Boolean.TRUE).when(_slf4jLogger).isInfoEnabled();
        Thread.sleep(1500);

        rateLimitLogger.info().setMessage("m4").log();
        Mockito.verify(_slf4jLogger, Mockito.atLeastOnce()).isInfoEnabled();
        Mockito.verify(_slf4jLogger).info(
                MockitoHamcrest.argThat(Matchers.sameInstance(StenoMarker.LISTS_MARKER)),
                MockitoHamcrest.argThat(Matchers.nullValue(String.class)),
                MockitoHamcrest.argThat(Matchers.contains("message", "_skipped", "_lastLogTime")),
                // This is brutal; for some reason a capture would not work here and the
                // contains(Matcher...) was getting mapped to contains(E...) even with
                // casting. So the matcher chain below manually asserts that the list
                // contains the three items in question.
                MockitoHamcrest.argThat(
                        Matchers.allOf(
                                Matchers.iterableWithSize(3),
                                Matchers.hasItem("m4"),
                                Matchers.hasItem(1),
                                Matchers.hasItem(isBetween(beforeLastLog, afterLastLog)))),
                MockitoHamcrest.argThat(Matchers.equalTo(Collections.emptyList())),
                MockitoHamcrest.argThat(Matchers.equalTo(Collections.emptyList())));
    }

    @Test
    public void testStandardLogFirstTime() {
        final Logger rateLimitLogger = new RateLimitLogger(_slf4jLogger, Duration.ofMinutes(1), Clock.systemUTC());
        rateLimitLogger.info("m");
        Mockito.verify(_slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{"message", "_skipped", "_lastLogTime"},
                new Object[]{"m", 0, null});
    }

    @Test
    public void testStandardLogSecondTimeWithinDuration() {
        final Logger rateLimitLogger = new RateLimitLogger(_slf4jLogger, Duration.ofMinutes(1), Clock.systemUTC());
        rateLimitLogger.info("m1");
        Mockito.verify(_slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{"message", "_skipped", "_lastLogTime"},
                new Object[]{"m1", 0, null});

        rateLimitLogger.info("m2");
        Mockito.verify(_slf4jLogger, Mockito.atLeastOnce()).isInfoEnabled();
        Mockito.verifyNoMoreInteractions(_slf4jLogger);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testStandardLogSecondTimeAfterDuration() throws InterruptedException {
        final Logger rateLimitLogger = new RateLimitLogger(_slf4jLogger, Duration.ofSeconds(1), Clock.systemUTC());
        final Instant beforeLastLog = Instant.now();
        rateLimitLogger.info("m1");
        final Instant afterLastLog = Instant.now();
        Mockito.verify(_slf4jLogger, Mockito.atLeastOnce()).isInfoEnabled();
        Mockito.verify(_slf4jLogger).info(
                StenoMarker.ARRAY_MARKER,
                null,
                new String[]{"message", "_skipped", "_lastLogTime"},
                new Object[]{"m1", 0, null});

        Mockito.verifyNoMoreInteractions(_slf4jLogger);
        Thread.sleep(500);
        rateLimitLogger.info("m2");  // Rate limited but counted
        rateLimitLogger.debug("m3"); // Dropped and not counted
        Mockito.reset(_slf4jLogger);
        Mockito.doReturn(Boolean.TRUE).when(_slf4jLogger).isInfoEnabled();
        Thread.sleep(1500);

        rateLimitLogger.info("m4");
        Mockito.verify(_slf4jLogger, Mockito.atLeastOnce()).isInfoEnabled();
        Mockito.verify(_slf4jLogger).info(
                MockitoHamcrest.argThat(Matchers.sameInstance(StenoMarker.ARRAY_MARKER)),
                MockitoHamcrest.argThat(Matchers.nullValue(String.class)),
                MockitoHamcrest.argThat(Matchers.arrayContaining("message", "_skipped", "_lastLogTime")),
                MockitoHamcrest.argThat(
                        Matchers.array(new org.hamcrest.Matcher[]{
                                Matchers.<Object>equalTo("m4"),
                                Matchers.<Object>equalTo(1),
                                isBetween(beforeLastLog, afterLastLog)})));
    }

    @Test
    public void testLogBuilderWithEmptyData() {
        final Logger rateLimitLogger = new RateLimitLogger(_slf4jLogger, Duration.ofMinutes(1), Clock.systemUTC());
        rateLimitLogger.info().setEvent("m").log();
        Mockito.verify(_slf4jLogger).info(
                StenoMarker.LISTS_MARKER,
                "m",
                Arrays.asList("_skipped", "_lastLogTime"),
                Arrays.asList(0, null),
                Collections.emptyList(),
                Collections.emptyList());
    }

    private static Matcher<Instant> isBetween(final Instant before, final Instant after) {
        return new TypeSafeMatcher<Instant>() {
            @Override
            protected boolean matchesSafely(final Instant instant) {
                return instant.toEpochMilli() >= before.toEpochMilli()
                        && instant.toEpochMilli() <= after.toEpochMilli();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Instant should be on or after " + before + " but on or before " + after);
            }

            @Override
            protected void describeMismatchSafely(final Instant instant, final Description description) {
                description.appendText("was " + instant);
            }
        };
    }

    @Mock
    private org.slf4j.Logger _slf4jLogger;
    @Captor
    private ArgumentCaptor<List<Object>> _dataValuesArgument;
    @Captor
    private ArgumentCaptor<Instant> _lastLogTimeArgument;
    private AutoCloseable _mocks;
}
