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

import com.arpnetworking.logback.annotations.LogValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import javax.annotation.Nullable;

/**
 * Jackson AnnotationIntrospector that:
 * <ul>
 * <li>Forces the RedactionFilter's ID to be placed on all classes.</li>
 * <li>Support LogValue annotation optionally falling back to JsonValue.</li>
 * </ul>
 *
 * @author Gil Markham (gil at groupon dot com)
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 * @since 1.3.3
 */
public class StenoAnnotationIntrospector extends JacksonAnnotationIntrospector {

    /**
     * Public constructor.
     *
     * @param objectMapper Instance of {@link ObjectMapper}.
     */
    public StenoAnnotationIntrospector(final ObjectMapper objectMapper) {
        _objectMapper = objectMapper;
    }

    @Override
    public Object findFilterId(final Annotated annotated) {
        return RedactionFilter.REDACTION_FILTER_ID;
    }

    @Override
    public @Nullable Boolean hasAsValue(final Annotated annotated) {
        // IMPORTANT: The @JsonValue only applies to members and fields
        // IMPORTANT: The @LogValue only applies to members
        // The @LogValue annotation if active takes precedence
        if (annotated instanceof AnnotatedMethod) {
            final LogValue annotation = _findAnnotation(annotated, LogValue.class);
            if (annotation != null) {
                if (annotation.enabled()) {
                    return true;
                } else if (!annotation.fallback()) {
                    return false;
                } else {
                    return super.hasAsValue(annotated);
                }
            }
        }
        // Otherwise check if the @JsonValue annotation should be suppressed
        final Class<?> clazz;
        if (annotated instanceof AnnotatedField) {
            clazz = ((AnnotatedField) annotated).getDeclaringClass();
        } else if (annotated instanceof AnnotatedMethod) {
            clazz = ((AnnotatedMethod) annotated).getDeclaringClass();
        } else {
            return super.hasAsValue(annotated);
        }
        final AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(
                _objectMapper.getSerializationConfig(),
                _objectMapper.constructType(clazz),
                _objectMapper.getSerializationConfig());
        for (final AnnotatedMethod otherAnnotatedMethod : annotatedClass.memberMethods()) {
            final LogValue annotation = _findAnnotation(otherAnnotatedMethod, LogValue.class);
            if (annotation != null) {
                if (annotation.enabled()) {
                    return null;
                } else if (!annotation.fallback()) {
                    return false;
                }
            }
        }

        // Otherwise use default logic (e.g respect @JsonValue)
        return super.hasAsValue(annotated);
    }

    private final ObjectMapper _objectMapper;

    private static final long serialVersionUID = 7623002162557264578L;
}
