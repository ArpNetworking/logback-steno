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
package com.arpnetworking.logback.jackson;

import com.arpnetworking.logback.annotations.LogRedact;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link RedactionFilter}.
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public class RedactionFilterTest {

    @Before
    public void setUp() {
        _objectMapper = new ObjectMapper();
        _objectMapper.setAnnotationIntrospector(new StenoAnnotationIntrospector(_objectMapper));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> filterMap = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(false));
        _objectMapper.setFilterProvider(new SimpleFilterProvider(filterMap));

        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> beanMap = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        beanMap.put("foo", "bar");
        _redactedBean = new RedactedBean("string", 1234, 1.0d, new String[] {"string1", "string2"}, true, beanMap);
        _nonRedactedBean = new NonRedactedBean("string", 1234, 1.0d, new String[] {"string1", "string2"}, true, beanMap);
    }

    @Test
    public void testNonRedactedBean() throws Exception {
        final JsonNode actualTree = _objectMapper.valueToTree(_nonRedactedBean);
        final JsonNode expectedTree = readTree("testNonRedactedBean.json");
        Assert.assertEquals(expectedTree, actualTree);
    }

    @Test
    public void testRedactedBean() throws Exception {
        final JsonNode actualTree = _objectMapper.valueToTree(_redactedBean);
        final JsonNode expectedTree = readTree("testRedactedBean.json");
        Assert.assertEquals(expectedTree, actualTree);
    }

    @Test
    public void testRedactedWithNull() throws Exception {
        // Override the filter to be configured with allowing nulls
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> filterMap = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(true));
        _objectMapper.setFilterProvider(new SimpleFilterProvider(filterMap));
        final JsonNode actualTree = _objectMapper.valueToTree(_redactedBean);
        final JsonNode expectedTree = readTree("testRedactedWithNull.json");
        Assert.assertEquals(expectedTree, actualTree);
    }

    /**
     * @deprecated Provided for test compatibility with deprecated method in Jackson.
     */
    @Test
    @Deprecated
    public void testDeprecatedSerializeAsFieldWithNull() throws Exception {
        final RedactionFilter mockFilter = Mockito.mock(RedactionFilter.class);
        Mockito.doAnswer(new SerializeAsFieldAnswer(new RedactionFilter(true)))
                .when(mockFilter).serializeAsField(
                        Mockito.any(),
                        Mockito.any(JsonGenerator.class),
                        Mockito.any(SerializerProvider.class),
                        Mockito.any(PropertyWriter.class));

        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> filterMap = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, mockFilter);
        _objectMapper.setFilters(new SimpleFilterProvider(filterMap));
        final JsonNode actualTree = _objectMapper.valueToTree(_redactedBean);
        final JsonNode expectedTree = readTree("testDeprecatedSerializeAsFieldWithNull.json");
        Assert.assertEquals(expectedTree, actualTree);
    }

    /**
     * @deprecated Provided for test compatibility with deprecated method in Jackson.
     */
    @Test
    @Deprecated
    public void testDeprecatedSerializeAsFieldWithoutNull() throws Exception {
        final RedactionFilter mockFilter = Mockito.mock(RedactionFilter.class);
        Mockito.doAnswer(new SerializeAsFieldAnswer(new RedactionFilter(false)))
                .when(mockFilter).serializeAsField(
                        Mockito.any(),
                        Mockito.any(JsonGenerator.class),
                        Mockito.any(SerializerProvider.class),
                        Mockito.any(PropertyWriter.class));

        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> filterMap = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, mockFilter);
        _objectMapper.setFilters(new SimpleFilterProvider(filterMap));
        final JsonNode actualTree = _objectMapper.valueToTree(_redactedBean);
        final JsonNode expectedTree = readTree("testDeprecatedSerializeAsFieldWithoutNull.json");
        Assert.assertEquals(expectedTree, actualTree);
    }

    private JsonNode readTree(final String resourceSuffix) {
        try {
            return _objectMapper.readTree(getClass().getClassLoader().getResource(
                    "com/arpnetworking/logback/jackson/" + CLASS_NAME + "." + resourceSuffix));
        } catch (final IOException e) {
            Assert.fail("Failed with exception: " + e);
            return null;
        }
    }

    private ObjectMapper _objectMapper;
    private RedactedBean _redactedBean;
    private NonRedactedBean _nonRedactedBean;

    private static final String CLASS_NAME = RedactionFilterTest.class.getSimpleName();

    private static final class NonRedactedBean {

        private NonRedactedBean(
                final String stringValue,
                final Integer intValue,
                final double doubleValue,
                final String[] stringArrayValue,
                final boolean booleanValue,
                final Map<String, Object> objectMap) {
            _stringValue = stringValue;
            _intValue = intValue;
            _doubleValue = doubleValue;
            _stringArrayValue = Arrays.copyOf(stringArrayValue, stringArrayValue.length);
            _booleanValue = booleanValue;
            _objectMap = objectMap;
        }

        public void setNullValue(final String nullValue) {
            _nullValue = nullValue;
        }

        public String getNullValue() {
            return _nullValue;
        }

        public String getStringValue() {
            return _stringValue;
        }

        public Integer getIntValue() {
            return _intValue;
        }

        public double getDoubleValue() {
            return _doubleValue;
        }

        public String[] getStringArrayValue() {
            return Arrays.copyOf(_stringArrayValue, _stringArrayValue.length);
        }

        public boolean isBooleanValue() {
            return _booleanValue;
        }

        public Map<String, Object> getObjectMap() {
            return _objectMap;
        }

        private String _nullValue = null;
        private final String _stringValue;
        private final Integer _intValue;
        private final double _doubleValue;
        private final String[] _stringArrayValue;
        private final boolean _booleanValue;
        private final Map<String, Object> _objectMap;
    }

    // CHECKSTYLE.OFF: MemberName - Testing field annotations requires same name as getter.
    // CHECKSTYLE.OFF: HiddenField - Testing field annotations requires same name as getter.
    private static final class RedactedBean {

        private RedactedBean(
                final String stringValue,
                final Integer intValue,
                final double doubleValue,
                final String[] stringArrayValue,
                final boolean booleanValue,
                final Map<String, Object> objectMap) {
            this.stringValue = stringValue;
            this.intValue = intValue;
            this.doubleValue = doubleValue;
            this.stringArrayValue = Arrays.copyOf(stringArrayValue, stringArrayValue.length);
            this.booleanValue = booleanValue;
            this.objectMap = objectMap;
        }

        public String getNullValue() {
            return nullValue;
        }

        @LogRedact
        @JsonProperty("string")
        public String getStringValue() {
            return stringValue;
        }

        public Integer getIntValue() {
            return intValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public String[] getStringArrayValue() {
            return Arrays.copyOf(stringArrayValue, stringArrayValue.length);
        }

        @LogRedact
        public boolean isBooleanValue() {
            return booleanValue;
        }

        public Map<String, Object> getObjectMap() {
            return objectMap;
        }

        public void setIgnoredField(final String ignoredField) {
            this.ignoredField = ignoredField;
        }

        public String getIgnoredField() {
            return ignoredField;
        }

        @LogRedact
        private final String nullValue = null;
        private final String stringValue;
        @LogRedact
        private final Integer intValue;
        private final double doubleValue;
        @LogRedact
        private final String[] stringArrayValue;
        private final boolean booleanValue;
        private final Map<String, Object> objectMap;
        @LogRedact
        @JsonIgnore
        private String ignoredField = "ignore";
    }
    // CHECKSTYLE.ON: HiddenField
    // CHECKSTYLE.ON: MemberName

    /**
     * @deprecated Provided for test compatibility with deprecated method in Jackson.
     */
    @Deprecated
    private static final class SerializeAsFieldAnswer implements Answer<Void> {

        private SerializeAsFieldAnswer(final RedactionFilter redactionFilter) {
            _redactionFilter = redactionFilter;
        }

        // CHECKSTYLE.OFF: IllegalThrows - Declared on external interface.
        @Override
        public Void answer(final InvocationOnMock invocation) throws Throwable {
            // CHECKSTYLE.ON: IllegalThrows
            if (invocation.getArguments()[3] instanceof BeanPropertyWriter) {
                _redactionFilter.serializeAsField(invocation.getArguments()[0], (JsonGenerator) invocation.getArguments()[1],
                        (SerializerProvider) invocation.getArguments()[2], (BeanPropertyWriter) invocation.getArguments()[3]);
            } else {
                _redactionFilter.serializeAsField(invocation.getArguments()[0],
                        (JsonGenerator) invocation.getArguments()[1],
                        (SerializerProvider) invocation.getArguments()[2],
                        (PropertyWriter) invocation.getArguments()[3]);
            }
            return null;
        }

        private final RedactionFilter _redactionFilter;
    }
}

