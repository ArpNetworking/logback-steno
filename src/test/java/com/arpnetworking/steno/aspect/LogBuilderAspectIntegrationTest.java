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
package com.arpnetworking.steno.aspect;

import com.arpnetworking.BaseStenoIntegrationTest;
import com.arpnetworking.steno.Logger;
import org.junit.Test;

/**
 * Integration test of {@link LogBuilderAspect}a spect weaving.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class LogBuilderAspectIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        final Logger logger = getStenoLogger();
        logger.trace().setMessage("Trace level events will be supressed").log();
        logger.debug().setMessage("Debug level events will be suppressed").log();
        logger.info().setMessage("This is informative").log();
        logger.warn().setMessage("This is a warning").log();
        logger.error().setMessage("This is an error").log();
        assertOutput();
    }
}
