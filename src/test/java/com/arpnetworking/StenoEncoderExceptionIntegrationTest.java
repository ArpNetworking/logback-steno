/*
 * Copyright 2016 Ville Koskela
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

import com.arpnetworking.logback.widgets.TestException;
import com.arpnetworking.logback.widgets.TestExceptionWithLoggable;
import org.junit.Test;

/**
 * Simple integration test for <code>Exception</code> serialization with the <code>StenoEncoder</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class StenoEncoderExceptionIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        final Exception testException = new TestException("TestException");
        final Exception testLoggableException = new TestExceptionWithLoggable("TestExceptionWithLoggable");

        getLogger().info("1. TestException", testException);
        getLogger().info("2. TestExceptionWithLoggable", testLoggableException);

        getStenoLogger()
                .info()
                .setMessage("3. TestException")
                .setThrowable(testException)
                .log();
        getStenoLogger()
                .info()
                .setMessage("4. TestExceptionWithLoggable")
                .setThrowable(testLoggableException)
                .log();

        assertOutput();
    }
}
