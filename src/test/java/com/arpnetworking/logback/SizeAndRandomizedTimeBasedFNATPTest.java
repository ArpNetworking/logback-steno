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
package com.arpnetworking.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Date;

/**
 * Tests for <code>SizeAndRandomizedTimeBasedFNATP</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class SizeAndRandomizedTimeBasedFNATPTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        final RollingFileAppender<LoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setFile("application.log");

        final TimeBasedRollingPolicy<LoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        _context = new LoggerContext();

        _triggeringPolicy = new SizeAndRandomizedTimeBasedFNATP<>(_wrappedPolicy);
        rollingPolicy.setContext(_context);
        rollingPolicy.setFileNamePattern("application-%d{yyyy-MM-dd_HH}.log");
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(_triggeringPolicy);
        rollingPolicy.start();
    }

    @Test
    public void testConstructor() {
        final SizeAndRandomizedTimeBasedFNATP<LoggingEvent> triggeringPolicy = new SizeAndRandomizedTimeBasedFNATP<>();
        Assert.assertNotNull(triggeringPolicy);
    }

    @Test
    public void testDelegateSetMaxOffsetInMillis() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.setMaxOffsetInMillis(123);
        Mockito.verify(_wrappedPolicy).setMaxOffsetInMillis(Matchers.eq(123));
    }

    @Test
    public void testDelegateGetMaxOffsetInMillis() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.getMaxOffsetInMillis();
        Mockito.verify(_wrappedPolicy).getMaxOffsetInMillis();
    }

    @Test
    public void testDelegateStart() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.start();
        Mockito.verify(_wrappedPolicy).start();
    }

    @Test
    public void testDelegateStop() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.stop();
        Mockito.verify(_wrappedPolicy).stop();
    }

    @Test
    public void testDelegateSetContext() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.setContext(_context);
        Mockito.verify(_wrappedPolicy).setContext(Mockito.same(_context));
    }

    @Test
    public void testDelegateSetDateInCurrentPeriod() {
        resetMock(_wrappedPolicy);

        final Date date = new Date();
        _triggeringPolicy.setDateInCurrentPeriod(date);
        Mockito.verify(_wrappedPolicy).setDateInCurrentPeriod(Mockito.same(date));
    }

    @Test
    public void testDelegateSetDateInCurrentPeriodAsTime() {
        resetMock(_wrappedPolicy);

        final Date date = new Date();
        Mockito.doReturn(date).when(_wrappedPolicy).getDateInCurrentPeriod();

        final long dateAsTime = 1234;
        _triggeringPolicy.setDateInCurrentPeriod(dateAsTime);
        Mockito.verify(_wrappedPolicy).getDateInCurrentPeriod();
        Assert.assertEquals(dateAsTime, date.getTime());
    }

    @Test
    public void testDelegateSetCurrentTime() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.setCurrentTime(1234L);
        Mockito.verify(_wrappedPolicy).setCurrentTime(Mockito.eq(1234L));
    }

    @Test
    public void testDelegateGetCurrentTime() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.getCurrentTime();
        Mockito.verify(_wrappedPolicy).getCurrentTime();
    }

    @Test
    public void testDelegateSetTimeBasedRollingPolicy() {
        resetMock(_wrappedPolicy);

        _triggeringPolicy.setTimeBasedRollingPolicy(_tbrp);
        Mockito.verify(_wrappedPolicy).setTimeBasedRollingPolicy(Mockito.same(_tbrp));
    }

    @Test
    public void testDelegateComputeNextCheck() {
        resetMock(_wrappedPolicy);
        Mockito.doReturn(123L).when(_wrappedPolicy).getNextCheck();

        _triggeringPolicy.computeNextCheck();
        Mockito.verify(_wrappedPolicy).computeNextCheck();
        Mockito.verify(_wrappedPolicy).getNextCheck();
        Assert.assertEquals(123L, _triggeringPolicy.getNextCheck());
    }

    private void resetMock(final Object mock) {
        Mockito.reset(mock);
    }

    @Mock
    private TimeBasedRollingPolicy<LoggingEvent> _tbrp;
    @Mock
    private RandomizedTimeBasedFNATP<LoggingEvent> _wrappedPolicy;
    private LoggerContext _context;
    private SizeAndRandomizedTimeBasedFNATP<LoggingEvent> _triggeringPolicy;
}
