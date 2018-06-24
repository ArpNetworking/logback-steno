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
package com.arpnetworking;

import com.arpnetworking.logback.widgets.Widget;
import com.arpnetworking.logback.widgets.WidgetWithLogValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Integration test of logging standard Java Collections.
 *
 * @author Matt Hayter (mhayter at groupon dot com)
 */
public class CollectionsIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final HashMap<String, String> strMap = new HashMap<>();
        final HashMap<String, WidgetWithLogValue> widgetMap = new HashMap<>();
        final HashMap<String, Widget> unloggableWidgetMap = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation

        strMap.put("MAPKEY1", "MAPVALUE1");
        strMap.put("MAPKEY2", "MAPVALUE2");

        widgetMap.put("MAPKEY1", new WidgetWithLogValue("WIDGETVALUE1"));

        unloggableWidgetMap.put("MAPKEY1", new Widget("UNUSED"));

        final ArrayList<String> strList = new ArrayList<>();
        strList.add("LISTVALUE1");
        strList.add("LISTVALUE2");

        final ArrayList<WidgetWithLogValue> widgetList = new ArrayList<>();
        widgetList.add(new WidgetWithLogValue("WIDGETVALUE1"));

        final ArrayList<Widget> unloggableWidgetList = new ArrayList<>();
        unloggableWidgetList.add(new Widget("UNUSED"));

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("1. This is an error")
                .addData("MAP1", strMap)
                .log();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("2. This is an error")
                .addData("MAP1", widgetMap)
                .log();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("3. This is an error")
                .addData("MAP1", unloggableWidgetMap)
                .log();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("4. This is an error")
                .addData("LIST1", strList)
                .log();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("5. This is an error")
                .addData("LIST1", widgetList)
                .log();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("6. This is an error")
                .addData("LIST1", unloggableWidgetList)
                .log();

        assertOutput();
    }
}
