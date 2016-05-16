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
package com.arpnetworking.steno;

import com.arpnetworking.logback.widgets.Widget;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for <code>LogValueMapFactory</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class LogValueMapFactoryTest {

    @Test
    public void testBeanIdentifierInjection() {
        final Widget w = new Widget("foo");
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        final LogValueMapFactory.LogValueMap logValueMap = LogValueMapFactory.builder(w).build();
        final Map<String, Object> actualValue = logValueMap.getData();
        Assert.assertEquals(expectedValue, actualValue);
        Assert.assertTrue(logValueMap.getTarget().isPresent());
        Assert.assertSame(w, logValueMap.getTarget().get());
    }

    @Test
    public void testOneKeyValuePair() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        expectedValue.put("k1", "v1");
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final Map<String, Object> actualValue = LogValueMapFactory.of("k1", "v1").getData();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testTwoKeyValuePair() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        expectedValue.put("k1", "v1");
        expectedValue.put("k2", "v2");
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final Map<String, Object> actualValue = LogValueMapFactory.of(
                "k1", "v1",
                "k2", "v2")
                .getData();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testThreeKeyValuePair() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        expectedValue.put("k1", "v1");
        expectedValue.put("k2", "v2");
        expectedValue.put("k3", "v3");
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final Map<String, Object> actualValue = LogValueMapFactory.of(
                "k1", "v1",
                "k2", "v2",
                "k3", "v3")
                .getData();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testFourKeyValuePair() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        expectedValue.put("k1", "v1");
        expectedValue.put("k2", "v2");
        expectedValue.put("k3", "v3");
        expectedValue.put("k4", "v4");
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final Map<String, Object> actualValue = LogValueMapFactory.of(
                "k1", "v1",
                "k2", "v2",
                "k3", "v3",
                "k4", "v4")
                .getData();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testFiveKeyValuePair() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        expectedValue.put("k1", "v1");
        expectedValue.put("k2", "v2");
        expectedValue.put("k3", "v3");
        expectedValue.put("k4", "v4");
        expectedValue.put("k5", "v5");
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final Map<String, Object> actualValue = LogValueMapFactory.of(
                "k1", "v1",
                "k2", "v2",
                "k3", "v3",
                "k4", "v4",
                "k5", "v5")
                .getData();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testBuilderWithNullKey() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        expectedValue.put("k1", "v1");
        expectedValue.put("_nullKeys", true);
        expectedValue.put("k3", "v3");
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final Map<String, Object> actualValue = LogValueMapFactory.builder()
                .put("k1", "v1")
                .put(null, "v2")
                .put("k3", "v3")
                .build()
                .getData();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testBuilderWithNullValue() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava dependency here.
        final Map<String, Object> expectedValue = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        expectedValue.put("k1", "v1");
        expectedValue.put("_nullValues", true);
        expectedValue.put("k3", "v3");
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final Map<String, Object> actualValue = LogValueMapFactory.builder()
                .put("k1", "v1")
                .put("k2", null)
                .put("k3", "v3")
                .build()
                .getData();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testSerialization() {
        final Widget w = new Widget("foo");
        final LogValueMapFactory.LogValueMap mapWithReference = LogValueMapFactory.builder(w).build();
        Assert.assertTrue(mapWithReference.getTarget().isPresent());
        Assert.assertSame(w, mapWithReference.getTarget().get());

        final byte[] serializedMap = SerializationUtils.serialize(mapWithReference);
        final LogValueMapFactory.LogValueMap deserializedMap = SerializationUtils.deserialize(serializedMap);

        Assert.assertFalse(deserializedMap.getTarget().isPresent());
    }

    @Test
    public void testToString() {
        // CHECKSTYLE.OFF: RegexpSingleline - Allow suboptimal construction for testing purposes.
        final String asString = LogValueMapFactory.builder().build().toString();
        // CHECKSTYLE.ON: RegexpSingleline
        Assert.assertNotNull(asString);
        Assert.assertFalse(asString.isEmpty());

        final String asStringWithReference = LogValueMapFactory.builder(new Widget("foo")).build().toString();
        Assert.assertNotNull(asStringWithReference);
        Assert.assertFalse(asStringWithReference.isEmpty());
        Assert.assertTrue(asStringWithReference.contains("_id="));
        Assert.assertTrue(asStringWithReference.contains("_class=com.arpnetworking.logback.widgets.Widget"));
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        final Constructor<LogValueMapFactory> constructor =
                LogValueMapFactory.class.getDeclaredConstructor();
        Assert.assertNotNull(constructor);
        try {
            constructor.newInstance();
            Assert.fail("Static helper class should have private no-args constructor");
        } catch (final IllegalAccessException e) {
            constructor.setAccessible(true);
            final LogValueMapFactory logValueMapFactory = constructor.newInstance();
            Assert.assertNotNull(logValueMapFactory);
        }
    }
}
