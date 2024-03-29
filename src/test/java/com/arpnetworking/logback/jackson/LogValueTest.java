/*
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
package com.arpnetworking.logback.jackson;

import com.arpnetworking.logback.StenoEncoder;
import com.arpnetworking.logback.annotations.LogValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabled;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledNoFallbackAndJsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link LogValue}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class LogValueTest {

    @Before
    public void setUp() {
        _mocks = MockitoAnnotations.openMocks(this);
        Mockito.doReturn(Boolean.FALSE).when(_encoder).isInjectBeanIdentifier();
        _objectMapper = new ObjectMapper();
        _objectMapper.setAnnotationIntrospector(new StenoAnnotationIntrospector(_objectMapper));
        final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(false));
        _objectMapper.setFilterProvider(simpleFilterProvider);
        final SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new StenoBeanSerializerModifier(_encoder));
        _objectMapper.registerModule(module);
    }

    @After
    public void tearDown() throws Exception {
        _mocks.close();
    }

    @Test
    public void testLogValue() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValue("foo"));
        Assert.assertEquals("{\"logValue\":\"foo\"}", asString);
    }

    @Test
    public void testLogValuePrecedence() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueAndJsonValue("foo"));
        Assert.assertEquals("{\"logValue\":\"foo\"}", asString);
    }

    @Test
    public void testLogValueDisabled() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueDisabled("foo"));
        Assert.assertEquals("{\"value\":\"foo\"}", asString);
    }

    @Test
    public void testLogValueFallback() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueDisabledAndJsonValue("foo"));
        Assert.assertEquals("{\"jsonValue\":\"foo\"}", asString);
    }

    @Test
    public void testLogValueNoFallback() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueDisabledNoFallbackAndJsonValue("foo"));
        Assert.assertEquals("{\"value\":\"foo\"}", asString);
    }

    private ObjectMapper _objectMapper;
    @Mock
    private StenoEncoder _encoder;
    private AutoCloseable _mocks;
}
