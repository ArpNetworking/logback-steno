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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Interface for providing the local host name.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.1.0
 */
/*package private*/ interface HostProvider {

    /**
     * Return the host name.
     *
     * @return The host name.
     * @throws UnknownHostException If the host name cannot be determined.
     */
    String get() throws UnknownHostException;

    /**
     * Default instance of <code>HostProvider</code>.
     */
    HostProvider DEFAULT = new DefaultHostProvider();

    /**
     * Default implementation of <code>HostProvider</code> using <code>InetAddress</code>.
     */
    /* package private static */ final class DefaultHostProvider implements HostProvider {

        public String get() throws UnknownHostException {
            return InetAddress.getLocalHost().getHostName();
        }
    }
}
