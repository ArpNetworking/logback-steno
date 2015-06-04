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

import com.arpnetworking.logback.StenoEncoder;
import com.arpnetworking.logback.jackson.StenoAnnotationIntrospector;
import com.arpnetworking.logback.widgets.Widget;
import com.arpnetworking.logback.widgets.WidgetWithAnnotatedSerializer;
import com.arpnetworking.logback.widgets.WidgetWithJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledNoFallbackAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLoggable;
import com.arpnetworking.logback.widgets.WidgetWithSerializer;
import com.arpnetworking.steno.LogReferenceOnly;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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
        _objectMapper.setAnnotationIntrospector(new StenoAnnotationIntrospector());
    }

    @Test
    public void testPrepareForSerializationNull() throws IOException {
        Assert.assertNull(prepare(null));
    }

    @Test
    public void testPrepareForSerializationSimpleTypes() throws IOException {
        Assert.assertTrue(prepare(1) instanceof Integer);
        Assert.assertTrue(prepare(1.23d) instanceof Double);
        Assert.assertTrue(prepare(false) instanceof Boolean);
        Assert.assertTrue(prepare("String") instanceof String);
    }

    @Test
    public void testPrepareForSerializationJsonNode() throws IOException {
        Assert.assertTrue(prepare(_objectMapper.createObjectNode()) instanceof JsonNode);
        Assert.assertTrue(prepare(_objectMapper.createArrayNode()) instanceof JsonNode);
        Assert.assertTrue(prepare(BooleanNode.FALSE) instanceof JsonNode);
        Assert.assertTrue(prepare(new IntNode(1)) instanceof JsonNode);
        Assert.assertTrue(prepare(new DoubleNode(1.23d)) instanceof JsonNode);
        Assert.assertTrue(prepare(new TextNode("String")) instanceof JsonNode);
    }

    @Test
    public void testPrepareForSerializationCustomSerializer() throws IOException {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(WidgetWithSerializer.class, new WidgetWithSerializer.Serializer());
        _objectMapper.registerModule(module);
        Assert.assertTrue(prepare(new WidgetWithSerializer("foo")) instanceof WidgetWithSerializer);
        Assert.assertTrue(prepare(new WidgetWithAnnotatedSerializer("foo")) instanceof WidgetWithAnnotatedSerializer);
    }

    @Test
    public void testPrepareForSerializationLoggable() throws IOException {
        Assert.assertTrue(prepare(new WidgetWithLoggable("foo")) instanceof WidgetWithLoggable);
    }

    @Test
    public void testPrepareForSerializationLogValue() throws IOException {
        Assert.assertTrue(prepare(new WidgetWithLogValue("foo")) instanceof WidgetWithLogValue);
        Assert.assertTrue(prepare(new WidgetWithJsonValue("foo")) instanceof WidgetWithJsonValue);
        Assert.assertTrue(prepare(new WidgetWithLogValueAndJsonValue("foo")) instanceof WidgetWithLogValueAndJsonValue);
        Assert.assertTrue(prepare(new WidgetWithLogValueDisabledAndJsonValue("foo")) instanceof WidgetWithLogValueDisabledAndJsonValue);
    }

    @Test
    public void testPrepareForSerializationReferenceOnly() throws IOException {
        Assert.assertTrue(prepare(new Widget("foo")) instanceof LogReferenceOnly);
        Assert.assertTrue(prepare(new WidgetWithLogValueDisabledNoFallbackAndJsonValue("foo")) instanceof LogReferenceOnly);
    }

    @Test
    public void testPrepareForSerializationUnsafe() throws IOException {
        _encoder.setSafe(false);

        // Serialized as-is
        Assert.assertTrue(prepare(new Widget("foo")) instanceof Widget);
        Assert.assertTrue(
                prepare(new WidgetWithLogValueDisabledNoFallbackAndJsonValue("foo"))
                        instanceof WidgetWithLogValueDisabledNoFallbackAndJsonValue);
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

    private Object prepare(final Object o) throws IOException {
        return StenoSerializationHelper.prepareForSerialization(_objectMapper, _encoder, o);
    }

    private ObjectMapper _objectMapper;
    private StenoEncoder _encoder;
}
