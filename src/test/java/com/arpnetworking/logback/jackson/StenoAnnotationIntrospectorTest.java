/*
 * Copyright 2017 Inscope Metrics, Inc.
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

package com.arpnetworking.logback.jackson;

import com.arpnetworking.logback.widgets.WidgetWithLogValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabled;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledNoFallbackAndJsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for <code>StenoAnnotationIntrospector</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class StenoAnnotationIntrospectorTest {

    @Before
    public void setUp() {
        _stenoAnnotationIntrospector = new StenoAnnotationIntrospector(OBJECT_MAPPER);
    }

    @Test
    public void testHasAsValueAnnotatedEnabledLogValueFound() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertTrue(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toLogValue", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedEnabledLogValueNotFound() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertNull(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toString", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledLogValueNoFallback() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabled.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertNull(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toLogValue", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledLogValueNoFallbackNotFound() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabled.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertNull(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toString", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledLogValueFallbackToJsonValue() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabledAndJsonValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertNull(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toLogValue", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledJsonValueFallback() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabledAndJsonValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertTrue(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toJsonValue", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledLogValueNotFound() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabledAndJsonValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertNull(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toString", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledLogValueDisableFallbackToJsonValue() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabledNoFallbackAndJsonValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertFalse(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toLogValue", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledJsonValueDisableFallback() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabledNoFallbackAndJsonValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertFalse(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toJsonValue", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedDisabledLogValueDisableFallbackNotFound() throws NoSuchMethodException {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(WidgetWithLogValueDisabledNoFallbackAndJsonValue.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertFalse(_stenoAnnotationIntrospector.hasAsValue(
                annotatedClass.findMethod("toString", new Class<?>[0])));
    }

    @Test
    public void testHasAsValueAnnotatedNotSupported() {
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                OBJECT_MAPPER.getDeserializationConfig(),
                OBJECT_MAPPER.getTypeFactory().constructType(String.class),
                OBJECT_MAPPER.getDeserializationConfig());
        Assert.assertNull(_stenoAnnotationIntrospector.hasAsValue(annotatedClass));
    }

    private StenoAnnotationIntrospector _stenoAnnotationIntrospector;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
