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
package com.arpnetworking.logback.serialization;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Tests for <code>BaseSerializationStrategy</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class BaseSerializationStrategyTest {

    @Test
    public void testIsSimpleType() {
        Assert.assertTrue(BaseSerializationStrategy.isSimpleType(null));
        Assert.assertTrue(BaseSerializationStrategy.isSimpleType("This is a String"));
        Assert.assertTrue(BaseSerializationStrategy.isSimpleType(Long.valueOf(1)));
        Assert.assertTrue(BaseSerializationStrategy.isSimpleType(Double.valueOf(3.14f)));
        Assert.assertTrue(BaseSerializationStrategy.isSimpleType(BigInteger.ONE));
        Assert.assertTrue(BaseSerializationStrategy.isSimpleType(Boolean.TRUE));
        Assert.assertFalse(BaseSerializationStrategy.isSimpleType(new Object()));
        Assert.assertFalse(BaseSerializationStrategy.isSimpleType(new long[]{}));
        Assert.assertFalse(BaseSerializationStrategy.isSimpleType(new double[]{}));
    }

    @Test
    public void testStenoLevel() {
        for (final BaseSerializationStrategy.StenoLevel level : BaseSerializationStrategy.StenoLevel.values()) {
            Assert.assertSame(level, BaseSerializationStrategy.StenoLevel.valueOf(level.toString()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStenoLevelDoesNotExist() {
        BaseSerializationStrategy.StenoLevel.valueOf("does_not_exist");
    }
}
