/*
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

import java.security.SecureRandom;

/**
 * Interface for providing the {@link SecureRandom} instances.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 * @since 1.1.0
 */
/*package private*/ interface SecureRandomProvider {

    /**
     * Return an instance of {@link SecureRandom}.
     *
     * @return An instance of {@link SecureRandom}.
     */
    SecureRandom get();

    /**
     * Return an instance of {@link SecureRandom} seeded with the specified data.
     *
     * @param seed The seed value to use.
     * @return An instance of {@link SecureRandom}.
     */
    SecureRandom get(byte[] seed);

    /**
     * Default instance of {@link SecureRandomProvider}.
     */
    SecureRandomProvider DEFAULT = new DefaultSecureRandomProvider();

    /**
     * Default implementation of {@link SecureRandomProvider}.
     */
    /* package private static */ final class DefaultSecureRandomProvider implements SecureRandomProvider {

        public SecureRandom get() {
            return new SecureRandom();
        }

        public SecureRandom get(final byte[] seed) {
            return new SecureRandom(seed);
        }
    }
}
