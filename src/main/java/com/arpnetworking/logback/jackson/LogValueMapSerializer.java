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

import com.arpnetworking.logback.StenoEncoder;
import com.arpnetworking.steno.LogValueMapFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Custom <code>JsonSerializer</code> for <code>LogValueMap</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.9.0
 */
public class LogValueMapSerializer extends JsonSerializer<LogValueMapFactory.LogValueMap> {

    /**
     * Public constructor.
     *
     * @since 1.9.0
     *
     * @param stenoEncoder The <code>StenoEncoder</code> instance.
     */
    public LogValueMapSerializer(final StenoEncoder stenoEncoder) {
        _stenoEncoder = stenoEncoder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(
            final LogValueMapFactory.LogValueMap value,
            final JsonGenerator gen,
            final SerializerProvider provider)
            throws IOException {

        gen.writeStartObject();
        final Optional<Object> target = value.getTarget();
        if (target.isPresent() && _stenoEncoder.isInjectBeanIdentifier()) {
            gen.writeStringField("_id", Integer.toHexString(System.identityHashCode(target.get())));
            gen.writeStringField("_class", target.get().getClass().getName());
        }
        for (final Map.Entry<String, Object> entry : value.entrySet()) {
            gen.writeObjectField(entry.getKey(), entry.getValue());
        }
        gen.writeEndObject();
    }

    private final StenoEncoder _stenoEncoder;

    private static final long serialVersionUID = -4063754169248471917L;
}
