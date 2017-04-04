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
import com.arpnetworking.logback.annotations.Loggable;
import com.arpnetworking.steno.LogValueMapFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modified for Jackson's BeanSerializer.
 *
 * First, allows injection of bean identifying properties, namely its instance and class, either when configured to
 * do so by the <code>StenoEncoder</code> or when the bean is not annotated with <code>Loggable</code>.
 *
 * Second, if the bean is not annotated with <code>Loggable</code> any fields discovered by Jackson should be
 * suppressed. Since this acts only on the <code>BeanSerializer</code> it is already ensured that:
 *
 * <ul>
 *     <li>The value is not null which would bypass all serializers.</li>
 *     <li>The type is not a simple type (e.g. String, Number or Boolean).</li>
 *     <li>The type is not a JsonNode which has its own serializer.</li>
 *     <li>The type does not have a @LogValue or @JsonValue annotation.</li>
 *     <li>The type does not have a custom serializer registered.</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.9.0
 */
public final class StenoBeanSerializerModifier extends BeanSerializerModifier {

    /**
     * Public constructor.
     *
     * @since 1.9.0
     *
     * @param stenoEncoder The instance of <code>StenoEncoder</code>.
     */
    public StenoBeanSerializerModifier(final StenoEncoder stenoEncoder) {
        _stenoEncoder = stenoEncoder;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(
            final SerializationConfig config,
            final BeanDescription beanDesc,
            final List<BeanPropertyWriter> beanProperties) {
        final List<BeanPropertyWriter> beanPropertyWriters = super.changeProperties(config, beanDesc, beanProperties);

        // Determine if the bean is annotated with @Loggable
        Boolean isLoggable = LOGGABLE_CLASSES.get(beanDesc.getBeanClass());
        if (isLoggable == null) {
            isLoggable = beanDesc.getBeanClass().getAnnotation(Loggable.class) != null;
            LOGGABLE_CLASSES.put(beanDesc.getBeanClass(), isLoggable);
        }

        // Non-loggable beans under safe encoding should not log properties
        final boolean clearProperties = !isLoggable && _stenoEncoder.isSafe();
        if (clearProperties) {
            beanProperties.clear();
        }

        // Beans with cleared properties or with bean identifier injection enabled should include identifiers
        if (clearProperties || _stenoEncoder.isInjectBeanIdentifier()) {
            beanPropertyWriters.add(0, new BeanIdentifierPropertyWriter(config));
            beanPropertyWriters.add(1, new BeanClassPropertyWriter(config));
        }

        return beanPropertyWriters;
    }

    private final StenoEncoder _stenoEncoder;

    private static final Map<Class<?>, Boolean> LOGGABLE_CLASSES = new ConcurrentHashMap<>();
    private static final Annotations EMPTY_ANNOTATION_MAP = new AnnotationMap();
    private static final JavaType STRING_JAVA_TYPE = TypeFactory.defaultInstance().constructType(String.class);

    /* package private */ static class BeanIdentifierPropertyWriter extends VirtualBeanPropertyWriter {

        /* package private */ BeanIdentifierPropertyWriter(final SerializationConfig config) {
            super(
                    SimpleBeanPropertyDefinition.construct(
                            config,
                            null,
                            PROPERTY_NAME),
                    EMPTY_ANNOTATION_MAP,
                    STRING_JAVA_TYPE);
        }

        @Override
        public void serializeAsField(final Object bean, final JsonGenerator gen, final SerializerProvider prov) throws Exception {
            if (bean instanceof LogValueMapFactory.LogValueMap) {
                // LogValueMap representations are identified by the target
                final LogValueMapFactory.LogValueMap logValueMap = (LogValueMapFactory.LogValueMap) bean;
                if (logValueMap.getTarget().isPresent()) {
                    gen.writeStringField("_id", Integer.toHexString(System.identityHashCode(logValueMap.getTarget().get())));
                } else {
                    gen.writeNullField("_id");
                }
            } else {
                // Standard beans are identified by the instance
                gen.writeStringField("_id", Integer.toHexString(System.identityHashCode(this)));
            }
        }

        /**
         * {@inheritDoc}
         *
         * @deprecated See <code>BeanPropertyWriter</code>.
         */
        @Deprecated
        @Override
        public Class<?> getPropertyType() {
            return String.class;
        }

        @Override
        protected Object value(final Object bean, final JsonGenerator jgen, final SerializerProvider prov) throws Exception {
            return null;
        }

        @Override
        public VirtualBeanPropertyWriter withConfig(
                final MapperConfig<?> config,
                final AnnotatedClass declaringClass,
                final BeanPropertyDefinition propDef,
                final JavaType type) {
            return this;
        }

        @Override
        public String toString() {
            // NOTE: The toString implementation in grandparent class BeanPropertyWriter throws an NPE for subclasses
            // of VirtualBeanPropertyWriter because they do not set the name field. Thus, we override the toString method
            // here with something completely useless.
            return "BeanIdentifierPropertyWriter";
        }

        @Override
        public void fixAccess(final SerializationConfig config) {
            // No need to fix access, the member is synthetic
        }

        private static final PropertyName PROPERTY_NAME = new PropertyName("_id");
        private static final long serialVersionUID = 1031451570210101221L;
    }

    /* package private */ static class BeanClassPropertyWriter extends VirtualBeanPropertyWriter {

        /* package private */ BeanClassPropertyWriter(final SerializationConfig config) {
            super(
                    SimpleBeanPropertyDefinition.construct(
                            config,
                            null,
                            PROPERTY_NAME),
                    EMPTY_ANNOTATION_MAP,
                    STRING_JAVA_TYPE);
        }

        @Override
        public void serializeAsField(final Object bean, final JsonGenerator gen, final SerializerProvider prov) throws Exception {
            if (bean instanceof LogValueMapFactory.LogValueMap) {
                // LogValueMap representations are identified by the target
                final LogValueMapFactory.LogValueMap logValueMap = (LogValueMapFactory.LogValueMap) bean;
                if (logValueMap.getTarget().isPresent()) {
                    gen.writeStringField("_class", logValueMap.getTarget().get().getClass().getName());
                } else {
                    gen.writeNullField("_class");
                }
            } else {
                // Standard beans are identified by the instance
                gen.writeStringField("_class", bean.getClass().getName());
            }
        }

        /**
         * {@inheritDoc}
         *
         * @deprecated See <code>BeanPropertyWriter</code>.
         */
        @Deprecated
        @Override
        public Class<?> getPropertyType() {
            return String.class;
        }

        @Override
        protected Object value(final Object bean, final JsonGenerator jgen, final SerializerProvider prov) throws Exception {
            return null;
        }

        @Override
        public VirtualBeanPropertyWriter withConfig(
                final MapperConfig<?> config,
                final AnnotatedClass declaringClass,
                final BeanPropertyDefinition propDef, final JavaType type) {
            return this;
        }

        @Override
        public String toString() {
            // NOTE: The toString implementation in grandparent class BeanPropertyWriter throws an NPE for subclasses
            // of VirtualBeanPropertyWriter because they do not set the name field. Thus, we override the toString method
            // here with something completely useless.
            return "BeanClassPropertyWriter";
        }

        @Override
        public void fixAccess(final SerializationConfig config) {
            // No need to fix access, the member is synthetic
        }

        private static final PropertyName PROPERTY_NAME = new PropertyName("_class");
        private static final long serialVersionUID = -3408022300214322460L;
    }
}
