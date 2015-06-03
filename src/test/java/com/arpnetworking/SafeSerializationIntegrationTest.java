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
import com.arpnetworking.logback.widgets.WidgetWithAnnotatedSerializer;
import com.arpnetworking.logback.widgets.WidgetWithJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabled;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledNoFallbackAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLoggable;
import com.arpnetworking.logback.widgets.WidgetWithSerializer;
import org.junit.Test;

/**
 * Integration test of safe <code>Object</code> logging.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class SafeSerializationIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        // Plain widget; logged with reference only
        getStenoLogger().info(
                "test",
                "Widget.class",
                "widget",
                new Widget("w1"));

        // Loggable widget; logged as-is.
        getStenoLogger().info(
                "test",
                "WidgetWithLoggable.class",
                "widget",
                new WidgetWithLoggable("w2"));

        // Custom serializer widgets; logged with specified serializer
        getStenoLogger().info(
                "test",
                "WidgetWithAnnotatedSerializer.class",
                "widget",
                new WidgetWithAnnotatedSerializer("w3"));
        getStenoLogger().info(
                "test",
                "WidgetWithSerializer.class",
                "widget",
                new WidgetWithSerializer("w4"));

        // Custom representation widgets; logged as custom representation
        getStenoLogger().info(
                "test",
                "WidgetWithJsonValue.class",
                "widget",
                new WidgetWithJsonValue("w5"));
        getStenoLogger().info(
                "test",
                "WidgetWithLogValue.class",
                "widget",
                new WidgetWithLogValue("w6"));
        getStenoLogger().info(
                "test",
                "WidgetWithLogValueAndJsonValue.class",
                "widget",
                new WidgetWithLogValueAndJsonValue("w7"));
        getStenoLogger().info(
                "test",
                "WidgetWithLogValueDisabled.class",
                "widget",
                new WidgetWithLogValueDisabled("w8"));
        getStenoLogger().info(
                "test",
                "WidgetWithLogValueDisabledAndJsonValue.class",
                "widget",
                new WidgetWithLogValueDisabledAndJsonValue("w9"));
        getStenoLogger().info(
                "test",
                "WidgetWithLogValueDisabledNoFallbackAndJsonValue.class",
                "widget",
                new WidgetWithLogValueDisabledNoFallbackAndJsonValue("w10"));

        assertOutput();
    }
}
