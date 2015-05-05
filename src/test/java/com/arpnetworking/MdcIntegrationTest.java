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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Integration test of MDC (Mapped Diagnostic Context) integration.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class MdcIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        final Logger logger = getLogger();
        MDC.clear();
        MDC.put("Bar", "Easy");
        logger.trace("Trace level events will be supressed");
        logger.debug("Debug level events will be suppressed");
        logger.info("This is informative");
        MDC.put("Foo", "As");
        MDC.put("Bar", "ABC");
        logger.warn("This is a warning");
        MDC.put("Foo", "123");
        MDC.remove("Bar");
        logger.error("This is an error");
        MDC.clear();
        assertOutput();
    }
}
