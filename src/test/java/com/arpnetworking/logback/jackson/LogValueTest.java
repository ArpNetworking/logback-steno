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

import com.arpnetworking.logback.annotations.LogValue;
import com.fasterxml.jackson.annotation.JsonValue;
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
        final String asString = _objectMapper.writeValueAsString(new LogValueBean());
        Assert.assertEquals(asString, "\"La, La, La\"");
    }

    @Test
    public void testLogValuePrecedence() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new LogValuePrecedenceBean());
        Assert.assertEquals(asString, "\"La, La, La\"");
    }

    @Test
    public void testLogValueDisabled() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new LogValueDisabledBean());
        Assert.assertEquals(asString, "{\"foo\":\"bar\"}");
    }

    @Test
    public void testLogValueFallback() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new LogValueFallbackBean());
        Assert.assertEquals(asString, "\"Di, Di, Di\"");
    }

    @Test
    public void testLogValueNoFallback() throws JsonProcessingException {
        final String asString = _objectMapper.writeValueAsString(new LogValueNoFallbackBean());
        Assert.assertEquals(asString, "{\"foo\":\"bar\"}");
    }

    private ObjectMapper _objectMapper;

    private static final class LogValueBean {

        public String getFoo() {
            return "bar";
        }

        @LogValue
        public String toString() {
            return "La, La, La";
        }
    }

    private static final class LogValuePrecedenceBean {

        public String getFoo() {
            return "bar";
        }

        @JsonValue
        public String toJson() {
            return "Di, Di, Di";
        }

        @LogValue
        public String toString() {
            return "La, La, La";
        }
    }

    private static final class LogValueDisabledBean {

        public String getFoo() {
            return "bar";
        }

        @LogValue(enabled = false)
        public String toString() {
            return "La, La, La";
        }
    }

    private static final class LogValueFallbackBean {

        public String getFoo() {
            return "bar";
        }

        @JsonValue
        public String toJson() {
            return "Di, Di, Di";
        }

        @LogValue(enabled = false)
        public String toString() {
            return "La, La, La";
        }
    }

    private static final class LogValueNoFallbackBean {

        public String getFoo() {
            return "bar";
        }

        @JsonValue
        public String toJson() {
            return "Di, Di, Di";
        }

        @LogValue(enabled = false, fallback = false)
        public String toString() {
            return "La, La, La";
        }
    }
}
