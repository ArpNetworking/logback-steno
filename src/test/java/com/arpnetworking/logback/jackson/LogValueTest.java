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
package com.arpnetworking.logback.jackson;

import com.arpnetworking.logback.widgets.WidgetWithLogValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabled;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledAndJsonValue;
import com.arpnetworking.logback.widgets.WidgetWithLogValueDisabledNoFallbackAndJsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for <code>LogValue</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class LogValueTest {

    @Before
    public void setUp() {
        _objectMapper = new ObjectMapper();
        _objectMapper.setAnnotationIntrospector(new StenoAnnotationIntrospector());
        final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(false));
        _objectMapper.setFilters(simpleFilterProvider);
    }

    @Test
    public void testLogValue() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValue("foo"));
        Assert.assertEquals(asString, "{\"logValue\":\"foo\"}");
    }

    @Test
    public void testLogValuePrecedence() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueAndJsonValue("foo"));
        Assert.assertEquals(asString, "{\"logValue\":\"foo\"}");
    }

    @Test
    public void testLogValueDisabled() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueDisabled("foo"));
        Assert.assertEquals(asString, "{\"value\":\"foo\"}");
    }

    @Test
    public void testLogValueFallback() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueDisabledAndJsonValue("foo"));
        Assert.assertEquals(asString, "{\"jsonValue\":\"foo\"}");
    }

    @Test
    public void testLogValueNoFallback() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new WidgetWithLogValueDisabledNoFallbackAndJsonValue("foo"));
        Assert.assertEquals(asString, "{\"value\":\"foo\"}");
    }

    private ObjectMapper _objectMapper;
}
