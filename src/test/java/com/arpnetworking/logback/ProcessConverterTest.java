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

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.regex.Pattern;

/**
 * Tests for <code>ProcessConverter</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class ProcessConverterTest {

    @Test
    public void testProcessConvert() {
        final String processId = "1234";
        final ProcessProvider provider = Mockito.mock(ProcessProvider.class);
        Mockito.doReturn(processId).when(provider).get();
        final ProcessConverter converter = new ProcessConverter(provider);
        Assert.assertEquals("1234", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verify(provider).get();
    }

    @Test
    public void testProcessConvertCaching() {
        final String processId = "1234";
        final ProcessProvider provider = Mockito.mock(ProcessProvider.class);
        Mockito.doReturn(processId).when(provider).get();
        final ProcessConverter converter = new ProcessConverter(provider);
        Assert.assertEquals("1234", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verify(provider).get();
        Assert.assertEquals("1234", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verifyNoMoreInteractions(provider);
    }

    @Test
    public void testProcessConvertException() {
        final ProcessProvider provider = Mockito.mock(ProcessProvider.class);
        Mockito.doThrow(new NullPointerException()).when(provider).get();
        final ProcessConverter converter = new ProcessConverter(provider);
        Assert.assertEquals("<UNKNOWN>", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verify(provider).get();
    }

    @Test
    public void testProcessConvertRegexMatch() {
        final ProcessProvider provider = new ProcessProvider.DefaultProcessProvider(Pattern.compile("^([\\d]+)@.*$"));
        final ProcessConverter converter = new ProcessConverter(provider);
        Assert.assertTrue(converter.convert(Mockito.mock(ILoggingEvent.class)).matches("^[\\d]+$"));
    }

    @Test
    public void testProcessConvertNoRegexMatch() {
        final ProcessProvider provider = new ProcessProvider.DefaultProcessProvider(Pattern.compile("^([\\d]+)@@.*$"));
        final ProcessConverter converter = new ProcessConverter(provider);
        Assert.assertTrue(converter.convert(Mockito.mock(ILoggingEvent.class)).matches("^[\\d]+@.*$"));
    }
}
