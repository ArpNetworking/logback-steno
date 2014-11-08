package com.arpnetworking.logback.jackson;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.arpnetworking.logback.annotations.LogRedact;

public class RedactionFilterTest {
    private static final String CLASS_NAME = RedactionFilterTest.class.getSimpleName();

    private ObjectMapper objectMapper;
    private RedactedBean redactedBean;
    private NonRedactedBean nonRedactedBean;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.setAnnotationIntrospector(new FilterForcingAnnotationIntrospector());
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(false));
        objectMapper.setFilters(new SimpleFilterProvider(filterMap));

        Map<String, Object> beanMap = new HashMap<>();
        beanMap.put("foo", "bar");
        redactedBean = new RedactedBean("string", 1234, 1.0f, new String[] {"string1", "string2"}, true, beanMap);
        nonRedactedBean = new NonRedactedBean("string", 1234, 1.0f, new String[] {"string1", "string2"}, true, beanMap);
    }

    @Test
    public void testNonRedactedBean() throws Exception {
        JsonNode actualTree = objectMapper.valueToTree(nonRedactedBean);
        JsonNode expectedTree;
        try (InputStream resourceStream = this.getClass().getResourceAsStream(CLASS_NAME + ".testNonRedactedBean.json")) {
            expectedTree = objectMapper.readTree(resourceStream);
        }
        Assert.assertEquals(expectedTree, actualTree);
    }

    @Test
    public void testRedactedBean() throws Exception {
        JsonNode actualTree = objectMapper.valueToTree(redactedBean);
        JsonNode expectedTree;
        try (InputStream resourceStream = this.getClass().getResourceAsStream(CLASS_NAME + ".testRedactedBean.json")) {
            expectedTree = objectMapper.readTree(resourceStream);
        }
        Assert.assertEquals(expectedTree, actualTree);
    }

    @Test
    public void testRedactedWithNull() throws Exception {
        // Override the filter to be configured with allowing nulls
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, new RedactionFilter(true));
        objectMapper.setFilters(new SimpleFilterProvider(filterMap));
        JsonNode actualTree = objectMapper.valueToTree(redactedBean);
        JsonNode expectedTree;
        try (InputStream resourceStream = this.getClass().getResourceAsStream(CLASS_NAME + ".testRedactedWithNull.json")) {
            expectedTree = objectMapper.readTree(resourceStream);
        }
        Assert.assertEquals(expectedTree, actualTree);
    }

    @Test
    @Deprecated
    public void testDeprecatedSerializeAsFieldWithNull() throws Exception {
        RedactionFilter mockFilter = Mockito.mock(RedactionFilter.class);
        Mockito.doAnswer(new SerializeAsFieldAnswer(new RedactionFilter(true))).when(mockFilter).serializeAsField(Matchers.any(), Matchers.any(JsonGenerator.class),
                Matchers.any(SerializerProvider.class),
                Matchers.any(PropertyWriter.class));

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, mockFilter);
        objectMapper.setFilters(new SimpleFilterProvider(filterMap));
        JsonNode actualTree = objectMapper.valueToTree(redactedBean);
        JsonNode expectedTree;
        try (InputStream resourceStream = this.getClass().getResourceAsStream(CLASS_NAME + ".testDeprecatedSerializeAsFieldWithNull.json")) {
            expectedTree = objectMapper.readTree(resourceStream);
        }
        Assert.assertEquals(expectedTree, actualTree);
    }

    @Test
    @Deprecated
    public void testDeprecatedSerializeAsFieldWithoutNull() throws Exception {
        RedactionFilter mockFilter = Mockito.mock(RedactionFilter.class);
        Mockito.doAnswer(new SerializeAsFieldAnswer(new RedactionFilter(false))).when(mockFilter).serializeAsField(Matchers.any(), Matchers.any(JsonGenerator.class),
                Matchers.any(SerializerProvider.class),
                Matchers.any(PropertyWriter.class));

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put(RedactionFilter.REDACTION_FILTER_ID, mockFilter);
        objectMapper.setFilters(new SimpleFilterProvider(filterMap));
        JsonNode actualTree = objectMapper.valueToTree(redactedBean);
        JsonNode expectedTree;
        try (InputStream resourceStream = this.getClass().getResourceAsStream(CLASS_NAME + ".testDeprecatedSerializeAsFieldWithoutNull.json")) {
            expectedTree = objectMapper.readTree(resourceStream);
        }
        Assert.assertEquals(expectedTree, actualTree);
    }

    public static class NonRedactedBean {
        private String nullValue = null;
        private final String stringValue;
        private final Integer intValue;
        private final float floatValue;
        private final String[] stringArrayValue;
        private final boolean booleanValue;
        private final Map<String, Object> objectMap;



        public NonRedactedBean(String stringValue, Integer intValue, float floatValue,
                String[] stringArrayValue, boolean booleanValue, Map<String, Object> objectMap) {
            this.stringValue = stringValue;
            this.intValue = intValue;
            this.floatValue = floatValue;
            this.stringArrayValue = Arrays.copyOf(stringArrayValue, stringArrayValue.length);
            this.booleanValue = booleanValue;
            this.objectMap = objectMap;
        }

        public void setNullValue(String nullValue) {
            this.nullValue = nullValue;
        }

        public String getNullValue() {
            return nullValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public Integer getIntValue() {
            return intValue;
        }

        public float getFloatValue() {
            return floatValue;
        }

        public String[] getStringArrayValue() {
            return Arrays.copyOf(stringArrayValue, stringArrayValue.length);
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public Map<String, Object> getObjectMap() {
            return objectMap;
        }
    }

    public static class RedactedBean {
        @LogRedact
        private final String nullValue = null;
        private final String stringValue;
        @LogRedact
        private final Integer intValue;
        private final float floatValue;
        @LogRedact
        private final String[] stringArrayValue;
        private final boolean booleanValue;
        private final Map<String, Object> objectMap;

        @LogRedact
        @JsonIgnore
        private String ignoredField = "ignore";

        public RedactedBean(String stringValue, Integer intValue, float floatValue,
                String[] stringArrayValue, boolean booleanValue, Map<String, Object> objectMap) {
            this.stringValue = stringValue;
            this.intValue = intValue;
            this.floatValue = floatValue;
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

        public float getFloatValue() {
            return floatValue;
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

        public void setIgnoredField(String ignoredField) {
            this.ignoredField = ignoredField;
        }

        public String getIgnoredField() {
            return ignoredField;
        }
    }

    @Deprecated
    private static class SerializeAsFieldAnswer implements Answer<Void> {
        private final RedactionFilter redactionFilter;

        public SerializeAsFieldAnswer(RedactionFilter redactionFilter) {
            this.redactionFilter = redactionFilter;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            if (invocation.getArguments()[3] instanceof BeanPropertyWriter) {
                redactionFilter.serializeAsField(invocation.getArguments()[0], (JsonGenerator) invocation.getArguments()[1],
                        (SerializerProvider) invocation.getArguments()[2], (BeanPropertyWriter) invocation.getArguments()[3]);
            } else {
                redactionFilter.serializeAsField(invocation.getArguments()[0],
                        (JsonGenerator) invocation.getArguments()[1],
                        (SerializerProvider) invocation.getArguments()[2],
                        (PropertyWriter) invocation.getArguments()[3]);
            }
            return null;
        }
    }
}

