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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for <code>ProcessProvider</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class ProcessProviderTest {

    @Test
    public void testDefaultProcessProvider() {
        final String process = ProcessProvider.DEFAULT.get();
        Assert.assertNotNull(process);
        Assert.assertFalse(process.isEmpty());
    }
}
