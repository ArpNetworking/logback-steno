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
package com.arpnetworking;

import com.arpnetworking.logback.widgets.Widget;
import org.junit.Test;

/**
 * Integration test of unsafe <code>Object</code> logging.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class UnsafeSerializationIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        // Plain widget; logged as-is with safe set to false
        getStenoLogger().info(
                "test",
                "Widget.class",
                "widget",
                new Widget("w1"));

        assertOutput();
    }
}
