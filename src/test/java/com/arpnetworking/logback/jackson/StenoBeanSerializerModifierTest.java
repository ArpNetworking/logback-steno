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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for <code>StenoBeanSerializerModifier</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class StenoBeanSerializerModifierTest {

    @Test
    public void testBeanIdentifierPropertyWriter() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final StenoBeanSerializerModifier.BeanIdentifierPropertyWriter writer =
                new StenoBeanSerializerModifier.BeanIdentifierPropertyWriter(objectMapper.getSerializationConfig());

        Assert.assertNull(writer.value(null, null, null));
        Assert.assertSame(writer, writer.withConfig(null, null, null, null));
        Assert.assertEquals("BeanIdentifierPropertyWriter", writer.toString());
    }

    @Test
    public void testBeanClassPropertyWriter() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final StenoBeanSerializerModifier.BeanClassPropertyWriter writer =
                new StenoBeanSerializerModifier.BeanClassPropertyWriter(objectMapper.getSerializationConfig());

        Assert.assertNull(writer.value(null, null, null));
        Assert.assertSame(writer, writer.withConfig(null, null, null, null));
        Assert.assertEquals("BeanClassPropertyWriter", writer.toString());
    }
}
