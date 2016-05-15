/**
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

import com.arpnetworking.logback.annotations.LogValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Jackson AnnotationIntrospector that:
 * <ul>
 * <li>Forces the RedactionFilter's ID to be placed on all classes.</li>
 * <li>Support LogValue annotation optionally falling back to JsonValue.</li>
 * </ul>
 *
 * @author Gil Markham (gil at groupon dot com)
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.3.3
 */
public class StenoAnnotationIntrospector extends JacksonAnnotationIntrospector {

    /**
     * Public constructor.
     *
     * @param objectMapper Instance of <code>ObjectMapper</code>.
     */
    public StenoAnnotationIntrospector(final ObjectMapper objectMapper) {
        _objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object findFilterId(final Annotated annotated) {
        return RedactionFilter.REDACTION_FILTER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAsValueAnnotation(final AnnotatedMethod annotatedMethod) {
        // The @LogValue annotation if active takes precedence
        final Class<?> clazz = annotatedMethod.getDeclaringClass();
        final AnnotatedClass annotatedClass = AnnotatedClass.construct(
                _objectMapper.constructType(clazz),
                _objectMapper.getSerializationConfig());
        for (final AnnotatedMethod otherAnnotatedMethod : annotatedClass.memberMethods()) {
            final LogValue annotation = _findAnnotation(otherAnnotatedMethod, LogValue.class);
            if (annotation != null) {
                if (annotation.enabled()) {
                    return otherAnnotatedMethod.equals(annotatedMethod);
                } else if (!annotation.fallback()) {
                    return false;
                }
            }
        }

        // Otherwise use default logic (e.g respect @JsonValue)
        return super.hasAsValueAnnotation(annotatedMethod);
    }

    private final ObjectMapper _objectMapper;

    private static final long serialVersionUID = 7623002162557264578L;
}
