/*
 * Copyright 2016 Ville Koskela
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

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link StenoMethodOfCallerConverter}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class StenoMethodOfCallerConverterTest {

    @Before
    public void setUp() {
        _mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        _mocks.close();
    }

    @Test
    public void testNullCallerData() {
        Mockito.doReturn(null).when(_loggingEvent).getCallerData();
        Assert.assertEquals(CallerData.NA, _converter.convert(_loggingEvent));
    }

    private AutoCloseable _mocks;

    private final ClassicConverter _converter = new StenoMethodOfCallerConverter();

    @Mock private ILoggingEvent _loggingEvent;
}
