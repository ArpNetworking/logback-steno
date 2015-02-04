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
package com.arpnetworking.steno;

/**
 * Factory class creates instances of <code>Logger</code>. To include the
 * context class or name in the context block of the Steno encoder's
 * output remember to enable context logger injection via:
 *
 * <code>setInjectContextLogger</code>
 *
 * @since 1.3.0
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public final class LoggerFactory {

    /**
     * Return a Steno <code>Logger</code> for a context class.
     *
     * @param clazz The <code>Logger</code> context class.
     * @return Steno <code>Logger</code> instance.
     */
    public static Logger getLogger(final Class<?> clazz) {
        return new Logger(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    /**
     * Return a Steno <code>Logger</code> for a context name.
     *
     * @param name The <code>Logger</code> context name.
     * @return Steno <code>Logger</code> instance.
     */
    public static Logger getLogger(final String name) {
        return new Logger(org.slf4j.LoggerFactory.getLogger(name));
    }

    private LoggerFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
