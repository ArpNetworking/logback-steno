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

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Tests for {@link SecureRandomProvider}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class SecureRandomProviderTest {

    @Test
    public void testDefaultSecureRandomProvider() {
        final SecureRandom secureRandom = SecureRandomProvider.DEFAULT.get();
        Assert.assertNotNull(secureRandom);
    }

    @Test
    public void testDefaultSecureRandomProviderWithSeed() {
        final SecureRandom secureRandom = SecureRandomProvider.DEFAULT.get(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull(secureRandom);
    }
}
