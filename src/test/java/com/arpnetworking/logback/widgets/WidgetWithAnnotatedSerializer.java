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
package com.arpnetworking.logback.widgets;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

/**
 * Class to test object serialization.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
@JsonSerialize(using = WidgetWithAnnotatedSerializer.Serializer.class)
public final class WidgetWithAnnotatedSerializer {

    /**
     * Public constructor.
     *
     * @param value The value of the widget.
     */
    public WidgetWithAnnotatedSerializer(final String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Value=" + _value;
    }

    private final String _value;

    /**
     * <code>JsonSerializer</code> implementation for <code>WidgetWithAnnotatedSerializer</code>.
     */
    public static class Serializer extends JsonSerializer<WidgetWithAnnotatedSerializer> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(
                final WidgetWithAnnotatedSerializer value,
                final JsonGenerator generator,
                final SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("serializerValue", value._value);
            generator.writeEndObject();
        }
    }
}
