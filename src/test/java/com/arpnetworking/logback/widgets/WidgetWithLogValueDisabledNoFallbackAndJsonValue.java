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

import com.arpnetworking.logback.annotations.LogValue;
import com.arpnetworking.steno.LogValueMapFactory;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Class to test object serialization.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public final class WidgetWithLogValueDisabledNoFallbackAndJsonValue {

    /**
     * Public constructor.
     *
     * @param value The value of the widget.
     */
    public WidgetWithLogValueDisabledNoFallbackAndJsonValue(final String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    /**
     * Create log representation.
     *
     * @return Log representation.
     */
    @LogValue(enabled = false, fallback = false)
    public Object toLogValue() {
        return LogValueMapFactory.of("logValue", _value);
    }

    /**
     * Create json representation.
     *
     * @return Json representation.
     */
    @JsonValue
    public Object toJsonValue() {
        return LogValueMapFactory.of("jsonValue", _value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Value=" + _value;
    }

    private final String _value;
}
