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
package com.arpnetworking.logback.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks types for serialization as-is by Jackson when logged. The annotation is not required if:
 * <ul>
 *     <li>The type is a <code>Number</code>, <code>Boolean</code> and <code>String</code>.</li>
 *     <li>The type is a <code>JsonNode</code>.</li>
 *     <li>Custom serializer is registered with @JsonSerialize.</li>
 *     <li>Custom serializer is registered with a Module.</li>
 *     <li>The type has a valid @JsonValue or @LogValue annotated method.</li>
 * </ul>on
 *
 * Otherwise values of the type are serialized as <code>LogReferenceOnly</code> which outputs the id of the instance in
 * hexadecimal as well as the full class name.
 *
 * Alternatively, all types may be serialized as-is by configuring the <code>StenoEncoder</code> with <code>safe</code>
 * set to <code>false</code>. However, this is not recommended because types in many third-party libraries can cause
 * the serializer to fail if they are included in the log message or transitively in any types included in the log
 * message.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @see com.arpnetworking.logback.jackson.RedactionFilter
 * @since 1.8.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
}

