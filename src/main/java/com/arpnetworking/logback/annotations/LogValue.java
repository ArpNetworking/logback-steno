/*
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
package com.arpnetworking.logback.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method which returns as its value the instance to be serialized
 * as the representation of the class under serialization. For example, add
 * this method to the {@link #toString} of a {@link Runnable}
 * implementation in order to serialize instances of that {@link Runnable}
 * using the result of {@link #toString} instead of Jackson's standard
 * bean serialization.
 *
 * This annotation when active takes precedence over @JsonValue. When inactive
 * the annotation introspector {@link com.arpnetworking.logback.jackson.StenoAnnotationIntrospector}
 * will by default fallback to @JsonValue if present. Adding @LogValue settings
 * its {@code disabled} and {@code fallback} properties to {@code false}
 * causes the introspector to ignore both @LogValue and @JsonValue and perform
 * standard bean serialization.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 * @since 1.3.3
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogValue {

    // CHECKSTYLE.OFF: JavadocMethod - Mismatch between Javadoc annotation spec (correct) and Checkstyle (incorrect).
    /**
     * Optional argument defines whether the annotation is active or not. The only use for a value of {@code false}
     * is when overriding a method with this annotation.
     *
     * @return {@code True} if and only if the annotation is active.
     */
    boolean enabled() default true;

    /**
     * Optional argument defines whether to fallback to @JsonValue if this annotation is not active.
     *
     * @return {@code True} if and only if fallback is enabled.
     */
    boolean fallback() default true;
    // CHECKSTYLE.ON: JavadocMethod
}
