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

import com.arpnetworking.logback.widgets.WidgetWithJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueProvidingReference;
import org.junit.Test;

/**
 * Integration test of bean identity injection.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class BeanIdentifierInjectionIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        // LogValue or JsonValue without reference
        getStenoLogger().info(
                "test",
                "WidgetWithJsonValue.class",
                "widget",
                new WidgetWithJsonValue("w1"));
        getStenoLogger().info(
                "test",
                "WidgetWithLogValue.class",
                "widget",
                new WidgetWithLogValue("w2"));

        // LogValue with reference
        getStenoLogger().info(
                "test",
                "WidgetWithLogValueProvidingReference.class",
                "widget",
                new WidgetWithLogValueProvidingReference("w3"));

        assertOutput();
    }
}
