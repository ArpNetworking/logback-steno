/**
 * Copyright 2014 Groupon.com
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
package com.arpnetworking.logback;

import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface for providing the process identifier.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.1.0
 */
/* package private */ interface ProcessProvider {

    /**
     * Return the process identifier.
     *
     * @return The process identifier.
     */
    String get();

    /**
     * Default instance of <code>ProcessProvider</code>.
     */
    ProcessProvider DEFAULT = new DefaultProcessProvider(Pattern.compile("^([\\d]+)@.*$"));

    /**
     * Default implementation of <code>ProcessProvider</code> using <code>ManagementFactory</code>.
     */
    /* package private static */ final class DefaultProcessProvider implements ProcessProvider {

        /* package private */ DefaultProcessProvider(final Pattern pattern) {
            _pattern = pattern;
        }

        public String get() {
            final String processId = ManagementFactory.getRuntimeMXBean().getName();
            final Matcher matcher = _pattern.matcher(processId);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return processId;
        }

        private final Pattern _pattern;
    }
}
