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

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests for {@link HostConverter}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class HostConverterTest {

    @Test
    public void testHostConvert() throws UnknownHostException {
        final String hostName = "my.host.name";
        final HostProvider provider = Mockito.mock(HostProvider.class);
        Mockito.doReturn(hostName).when(provider).get();
        final HostConverter converter = new HostConverter(provider);
        Assert.assertEquals("my.host.name", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verify(provider).get();
    }

    @Test
    public void testHostConvertCaching() throws UnknownHostException {
        final String hostName = "my.host.name";
        final HostProvider provider = Mockito.mock(HostProvider.class);
        Mockito.doReturn(hostName).when(provider).get();
        final HostConverter converter = new HostConverter(provider);
        Assert.assertEquals("my.host.name", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verify(provider).get();
        Assert.assertEquals("my.host.name", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verifyNoMoreInteractions(provider);
    }

    @Test
    public void testHostConvertException() throws UnknownHostException {
        final HostProvider provider = Mockito.mock(HostProvider.class);
        Mockito.doThrow(new NullPointerException()).when(provider).get();
        final HostConverter converter = new HostConverter(provider);
        Assert.assertEquals("<UNKNOWN>", converter.convert(Mockito.mock(ILoggingEvent.class)));
        Mockito.verify(provider).get();
    }

    @Test
    public void testProcessConvertDefaultProvider() throws UnknownHostException {
        final HostConverter converter = new HostConverter();
        Assert.assertEquals(
                InetAddress.getLocalHost().getCanonicalHostName(),
                converter.convert(Mockito.mock(ILoggingEvent.class)));
    }
}
