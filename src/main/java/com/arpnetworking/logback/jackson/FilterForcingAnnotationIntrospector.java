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

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Jackson AnnotationIntrospector that forces the RedactionFilter's ID to be placed on all classes.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.1.0
 */
public class FilterForcingAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private static final long serialVersionUID = 7623002162557264578L;

    @Override
    public Object findFilterId(Annotated a) {
        return RedactionFilter.REDACTION_FILTER_ID;
    }
}
