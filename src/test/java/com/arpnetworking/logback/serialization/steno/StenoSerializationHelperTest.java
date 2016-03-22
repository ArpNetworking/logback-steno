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
package com.arpnetworking.logback.serialization.steno;

import com.arpnetworking.logback.StenoEncoder;
import com.arpnetworking.logback.jackson.StenoAnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.math.BigInteger;

/**
 * Tests for <code>StenoSerializationHelper</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class StenoSerializationHelperTest {

    @Before
    public void setUp() {
        _objectMapper = new ObjectMapper();
        _encoder = new StenoEncoder();
        _objectMapper.setAnnotationIntrospector(new StenoAnnotationIntrospector(_objectMapper));
    }

    @Test
    public void testIsSimpleType() {
        Assert.assertTrue(StenoSerializationHelper.isSimpleType(null));
        Assert.assertTrue(StenoSerializationHelper.isSimpleType("This is a String"));
        Assert.assertTrue(StenoSerializationHelper.isSimpleType(Long.valueOf(1)));
        Assert.assertTrue(StenoSerializationHelper.isSimpleType(Double.valueOf(1.23f)));
        Assert.assertTrue(StenoSerializationHelper.isSimpleType(BigInteger.ONE));
        Assert.assertTrue(StenoSerializationHelper.isSimpleType(Boolean.TRUE));
        Assert.assertFalse(StenoSerializationHelper.isSimpleType(new Object()));
        Assert.assertFalse(StenoSerializationHelper.isSimpleType(new long[]{}));
        Assert.assertFalse(StenoSerializationHelper.isSimpleType(new double[]{}));
    }

    @Test
    public void testStenoLevel() {
        for (final StenoSerializationHelper.StenoLevel level : StenoSerializationHelper.StenoLevel.values()) {
            Assert.assertSame(level, StenoSerializationHelper.StenoLevel.valueOf(level.toString()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStenoLevelDoesNotExist() {
        StenoSerializationHelper.StenoLevel.valueOf("does_not_exist");
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        final Constructor<StenoSerializationHelper> constructor =
                StenoSerializationHelper.class.getDeclaredConstructor();
        Assert.assertNotNull(constructor);
        try {
            constructor.newInstance();
            Assert.fail("Static helper class should have private no-args constructor");
        } catch (final IllegalAccessException e) {
            constructor.setAccessible(true);
            final StenoSerializationHelper stenoSerializationHelper = constructor.newInstance();
            Assert.assertNotNull(stenoSerializationHelper);
        }
    }

    private ObjectMapper _objectMapper;
    private StenoEncoder _encoder;
}
