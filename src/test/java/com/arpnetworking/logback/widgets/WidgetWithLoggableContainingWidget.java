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

import com.arpnetworking.logback.annotations.Loggable;

/**
 * Class to test object serialization.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
@Loggable
public final class WidgetWithLoggableContainingWidget {

    /**
     * Public constructor.
     *
     * @param value The value of the widget.
     */
    public WidgetWithLoggableContainingWidget(final String value) {
        _value = value;
        _subWidget = new Widget("sub-" + value);
    }

    public String getValue() {
        return _value;
    }

    public Widget getSubWidget() {
        return _subWidget;
    }

    @Override
    public String toString() {
        return "Value=" + _value + ", SubWidget=" + _subWidget;
    }

    private final String _value;
    private final Widget _subWidget;
}
