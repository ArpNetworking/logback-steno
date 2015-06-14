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
package com.arpnetworking.steno;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating <code>ImmutableMap</code> instances from key-value
 * data which may contain null keys or values. Most <code>Map</code>
 * implementations cannot contain either. This factory discards any such
 * keys or values and adds keys "_nullKeys" and/or "_nullValues" with value
 * true. If no null keys/values are found then no additional data is inserted.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.7.0
 */
public final class LogValueMapFactory {

    /**
     * Create a <code>Builder</code> for a null-safe immutable map. It is strongly recommended
     * that you use the static factory accepting the target instance unless that is not applicable.
     *
     * @since 1.7.0
     *
     * @return New <code>Builder</code> instance.
     */
    public static Builder builder() {
        return new Builder(Optional.empty());
    }

    /**
     * Create a <code>Builder</code> for a null-safe immutable map with a reference to the target
     * instance being logged. This permits injection of bean identifier attributes if so configured.
     *
     * @since 1.9.0
     *
     * @param o Instance of the <code>Object</code> being logged.
     * @return New <code>Builder</code> instance.
     */
    public static Builder builder(final Object o) {
        return new Builder(Optional.ofNullable(o));
    }

    /**
     * Construct an immutable map from one key-value pair. Although this is more convenient than the
     * static factory methods this method does not capture the instance being logged.
     *
     * @since 1.7.0
     *
     * @param k1 Key one.
     * @param v1 Value one.
     * @return New immutable map.
     */
    public static Map<String, Object> of(final String k1, final Object v1) {
        return builder()
                .put(k1, v1)
                .build();
    }

    /**
     * Construct an immutable map from one key-value pair. Although this is more convenient than the
     * static factory methods this method does not capture the instance being logged.
     *
     * @since 1.7.0
     *
     * @param k1 Key one.
     * @param v1 Value one.
     * @param k2 Key two.
     * @param v2 Value two.
     * @return New immutable map.
     */
    public static Map<String, Object> of(
            final String k1, final Object v1,
            final String k2, final Object v2) {
        return builder()
                .put(k1, v1)
                .put(k2, v2)
                .build();
    }

    /**
     * Construct an immutable map from one key-value pair. Although this is more convenient than the
     * static factory methods this method does not capture the instance being logged.
     *
     * @since 1.7.0
     *
     * @param k1 Key one.
     * @param v1 Value one.
     * @param k2 Key two.
     * @param v2 Value two.
     * @param k3 Key three.
     * @param v3 Value three.
     * @return New immutable map.
     */
    public static Map<String, Object> of(
            final String k1, final Object v1,
            final String k2, final Object v2,
            final String k3, final Object v3) {
        return builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .build();
    }

    /**
     * Construct an immutable map from one key-value pair. Although this is more convenient than the
     * static factory methods this method does not capture the instance being logged.
     *
     * @since 1.7.0
     *
     * @param k1 Key one.
     * @param v1 Value one.
     * @param k2 Key two.
     * @param v2 Value two.
     * @param k3 Key three.
     * @param v3 Value three.
     * @param k4 Key four.
     * @param v4 Value four.
     * @return New immutable map.
     */
    // CHECKSTYLE.OFF: ParameterNumber - Provided for compatibility wth ImmutableMap.of
    public static Map<String, Object> of(
            final String k1, final Object v1,
            final String k2, final Object v2,
            final String k3, final Object v3,
            final String k4, final Object v4) {
        return builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .build();
    }
    // CHECKSTYLE.ON: ParameterNumber

    /**
     * Construct an immutable map from one key-value pair. Although this is more convenient than the
     * static factory methods this method does not capture the instance being logged.
     *
     * @since 1.7.0
     *
     * @param k1 Key one.
     * @param v1 Value one.
     * @param k2 Key two.
     * @param v2 Value two.
     * @param k3 Key three.
     * @param v3 Value three.
     * @param k4 Key four.
     * @param v4 Value four.
     * @param k5 Key five.
     * @param v5 Value five.
     * @return New immutable map.
     */
    // CHECKSTYLE.OFF: ParameterNumber - Provided for compatibility wth ImmutableMap.of
    public static Map<String, Object> of(
            final String k1, final Object v1,
            final String k2, final Object v2,
            final String k3, final Object v3,
            final String k4, final Object v4,
            final String k5, final Object v5) {
        return builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .build();
    }
    // CHECKSTYLE.ON: ParameterNumber

    private LogValueMapFactory() {}

    /**
     * Custom <code>Map</code> implementation for custom serialization.
     *
     * @since 1.9.0
     */
    public static final class LogValueMap extends LinkedHashMap<String, Object> {

        /**
         * Public constructor.
         *
         * @param target The instance being represented for logging.
         */
        public LogValueMap(final Optional<Object> target) {
            _target = target;
        }

        public Optional<Object> getTarget() {
            return _target;
        }

        private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            _target = Optional.empty();
        }

        private transient Optional<Object> _target = Optional.empty();

        private static final long serialVersionUID = -2817278417438085513L;
    }

    /**
     * Builder for null-safe log value immutable map.
     *
     * @since 1.7.0
     */
    public static final class Builder {

        /**
         * Public constructor. This permits injection of bean identifier attributes if so configured.
         *
         * @since 1.9.0
         *
         * @param target The target instance to build a log value map for.
         */
        private Builder(final Optional<Object> target) {
            _map = new LogValueMap(target);
        }

        /**
         * Construct the map for the log value.
         *
         * @since 1.7.0
         *
         * @return New map for the log value.
         */
        public Map<String, Object> build() {
            if (_nullKeys) {
                put("_nullKeys", true);
            }
            if (_nullValues) {
                put("_nullValues", true);
            }
            return _map;
        }

        /**
         * Add a key and value in a null-safe manner to the log value map. If either is
         * null the entry is suppressed. If any entries are suppressed one or both of
         * "_nullKeys" and "_nullValues" will be inserted as keys with a value true
         * into the log value map. These indicate the suppression of one or more null
         * keys or values respectively.
         *
         * @since 1.7.0
         *
         * @param key The entry key.
         * @param value The entry value.
         * @return This <code>Builder</code> instance.
         */
        public Builder put(final String key, final Object value) {
            if (key == null) {
                _nullKeys = true;
                return this;
            }
            if (value == null) {
                _nullValues = true;
                return this;
            }
            _map.put(key, value);
            return this;
        }

        private final Map<String, Object> _map;
        private boolean _nullKeys = false;
        private boolean _nullValues = false;
    }
}
