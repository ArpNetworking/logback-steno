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

import com.arpnetworking.logback.widgets.WidgetWithSerializer;
import com.arpnetworking.steno.Logger;
import org.junit.Test;

/**
 * Integration test of module integration.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class ModuleIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        final Logger logger = getStenoLogger();
        logger.info()
                .setMessage("Contains a Widget")
                .addData("widget", new WidgetWithSerializer("This is my widget"))
                .log();
        assertOutput();
    }
}
