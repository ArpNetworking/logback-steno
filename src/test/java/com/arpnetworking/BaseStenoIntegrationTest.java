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

/**
 * Base integration test for <code>StenoEncoder</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public abstract class BaseStenoIntegrationTest extends BaseIntegrationTest {

    @Override
    protected String sanitizeOutput(final String output) {
        return output.replaceAll("\"time\":\"[^\"]+\"", "\"time\":\"<TIME>\"")
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"<ID>\"")
                .replaceAll("\"host\":\"[^\"]+\"", "\"host\":\"<HOST>\"")
                .replaceAll("\"processId\":\"[^\"]+\"", "\"processId\":\"<PROCESS_ID>\"")
                .replaceAll("\"threadId\":\"[^\"]+\"", "\"threadId\":\"<THREAD_ID>\"")
                .replaceAll("\"backtrace\":\\[[^\\]]+\\]", "\"backtrace\":[]");
    }
}
