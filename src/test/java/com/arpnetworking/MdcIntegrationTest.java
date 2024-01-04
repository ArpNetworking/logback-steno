/*
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
import org.slf4j.spi.MDCAdapter;

/**
 * Integration test of MDC (Mapped Diagnostic Context) integration.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class MdcIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        final Logger logger = getLogger();
        final MDCAdapter mdcAdapter = getMdcAdapter();
        mdcAdapter.clear();
        mdcAdapter.put("MDC_KEY2", "Easy");
        logger.trace("Trace level events will be suppressed");
        logger.debug("Debug level events will be suppressed");
        logger.info("This is informative");
        mdcAdapter.put("MDC_KEY1", "As");
        mdcAdapter.put("MDC_KEY2", "ABC");
        logger.warn("This is a warning");
        mdcAdapter.put("MDC_KEY1", "123");
        mdcAdapter.remove("MDC_KEY2");
        logger.error("This is an error");
        mdcAdapter.clear();
        assertOutput();
    }
}
