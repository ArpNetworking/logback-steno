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

import com.arpnetworking.logback.annotations.LogRedact;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import java.util.Collections;
import java.util.Set;

/**
 * Jackson property filter that replaces values of fields annotated with @LogRedact with a string value of
 * "&lt;REDACTED&gt;".  If constructed with 'allowNull' as true then fields whose value is null will be output
 * as 'null' rather than a filtered string.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.1.0
 */
public class RedactionFilter extends SimpleBeanPropertyFilter.SerializeExceptFilter {

    /**
     * Public constructor.
     *
     * @param allowNull Whether to allow null field values to pass as-is even
     * if those fields are annotated as redacted.
     */
    public RedactionFilter(final boolean allowNull) {
        this(allowNull, Collections.<String>emptySet());
    }

    /**
     * Public constructor.
     *
     * @param allowNull Whether to allow null field values to pass as-is even
     * if those fields are annotated as redacted.
     * @param properties Additional properties for <code>SimpleBeanPropertyFilter</code>.
     */
    public RedactionFilter(final boolean allowNull, final Set<String> properties) {
        super(properties);
        _allowNull = allowNull;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Provided for compatibility with deprecated method in Jackson.
     */
    @Override
    @Deprecated
    public void serializeAsField(
            final Object pojo,
            final JsonGenerator jgen,
            final SerializerProvider prov,
            final BeanPropertyWriter writer) throws Exception {
        if (writer.getAnnotation(LogRedact.class) == null) {
            super.serializeAsField(pojo, jgen, prov, writer);
        } else { // since 2.3
            if (_allowNull && writer.get(pojo) == null) {
                super.serializeAsField(pojo, jgen, prov, writer);
            } else {
                jgen.writeStringField(writer.getSerializedName().getValue(), REDACTION_STRING);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serializeAsField(
            final Object pojo,
            final JsonGenerator jgen,
            final SerializerProvider prov,
            final PropertyWriter writer) throws Exception {
        if (writer instanceof BeanPropertyWriter) {
            final BeanPropertyWriter beanPropertyWriter = (BeanPropertyWriter) writer;
            if (beanPropertyWriter.getAnnotation(LogRedact.class) == null) {
                super.serializeAsField(pojo, jgen, prov, writer);
            } else {
                if (_allowNull && beanPropertyWriter.get(pojo) == null) {
                    super.serializeAsField(pojo, jgen, prov, writer);
                } else {
                    jgen.writeStringField(beanPropertyWriter.getSerializedName().getValue(), REDACTION_STRING);
                }
            }
        } else {
            super.serializeAsField(pojo, jgen, prov, writer);
        }
    }

    private final boolean _allowNull;

    private static final long serialVersionUID = -3087655661573890897L;

    /**
     * The identifier of the <code>RedactionFilter</code> filter.
     */
    public static final String REDACTION_FILTER_ID = "com.arpnetworking.logback.jackson.RedactionFilterId";

    /**
     * The <code>String</code> to replace field values annotated as redacted.
     */
    public static final String REDACTION_STRING = "<REDACTED>";
}
