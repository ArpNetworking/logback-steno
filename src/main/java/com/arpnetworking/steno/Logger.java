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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Logger designed for use particularly with Steno encoder. Although not
 * interface compatible with the SLF4J <code>Logger</code> this class
 * attempts to provide some common methods to ease the transition. However,
 * its purpose is to provide more concrete methods of data and context
 * injection for Steno versus the general marker methods in the SFL4J
 * implementation.
 *
 * @since 1.3.0
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @author Gil Markham (gil at groupon dot com)
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class Logger {

    /**
     * Create a new log event at the trace level. Do not pre-build and cache
     * <code>LogBuilder</code> instances.
     *
     * @return Instance of <code>LogBuilder</code>.
     */
    public LogBuilder trace() {
        if (_slf4jLogger.isTraceEnabled()) {
            return new DefaultLogBuilder(this, LogLevel.TRACE);
        } else {
            return NO_OP_LOG_BUILDER;
        }
    }

    /**
     * Log a message at the trace level. Default values are used for all other
     * parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     */
    public void trace(final String message) {
        log(LogLevel.TRACE, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message with a <code>Throwable</code> at the trace level. Default
     * values are used for all other parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void trace(final String message, final Throwable throwable) {
        log(LogLevel.TRACE, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event at the trace level. Default values
     * are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     */
    public void trace(final String event, final String message) {
        log(LogLevel.TRACE, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with a <code>Throwable</code> at the
     * trace level. Default values are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void trace(final String event, final String message, final Throwable throwable) {
        log(LogLevel.TRACE, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the trace level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     */
    public void trace(
            final String event,
            final String message,
            final Map<String, Object> data) {
        trace(event, message, data, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the trace level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void trace(
            final String event,
            final String message,
            final Map<String, Object> data,
            final Throwable throwable) {
        if (_slf4jLogger.isTraceEnabled()) {
            LogLevel.TRACE.log(
                    _slf4jLogger,
                    event,
                    createKeysFromCollection(
                            data == null ? Collections.emptyList() : data.keySet(),
                            MESSAGE_DATA_KEY),
                    createValuesFromCollection(
                            data == null ? Collections.emptyList() : data.values(),
                            message),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the trace level.
     *
     * The number of elements in the <code>dataNames</code> array should
     * match the number of arguments provided as <code>data</code>
     * unless a <code>Throwable</code> is specified as the final argument in
     * <code>data</code> in which case there would be one fewer element
     * in the <code>dataNames</code> array compared to the number of
     * arguments provided as <code>data</code>.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues data values matching data keys by index. The
     * final data argument may be a <code>Throwable</code> which does
     * not require a matching data name.
     */
    public void trace(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object... dataValues) {
        final Throwable throwable = extractThrowable(dataKeys, dataValues);
        log(LogLevel.TRACE, event, message, dataKeys, chompArray(dataValues, throwable == null ? 0 : 1), throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the trace level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues Array of data values.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void trace(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object[] dataValues,
            final Throwable throwable) {
        log(
                LogLevel.TRACE,
                event,
                message,
                dataKeys,
                dataValues,
                throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the trace level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     */
    public void trace(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1) {
        trace(event, message, dataKey1, dataValue1, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the trace level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void trace(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1,
            final Throwable throwable) {
        if (_slf4jLogger.isTraceEnabled()) {
            LogLevel.TRACE.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1),
                    createValuesFromArgs(message, dataValue1),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the trace level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     */
    public void trace(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2) {
        trace(event, message, dataKey1, dataKey2, dataValue1, dataValue2, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the trace level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void trace(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2,
            final Throwable throwable) {
        if (_slf4jLogger.isTraceEnabled()) {
            LogLevel.TRACE.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1, dataKey2),
                    createValuesFromArgs(message, dataValue1, dataValue2),
                    throwable);
        }
    }

    /**
     * Create a new log event at the debug level. Do not pre-build and cache
     * <code>LogBuilder</code> instances.
     *
     * @return Instance of <code>LogBuilder</code>.
     */
    public LogBuilder debug() {
        if (_slf4jLogger.isDebugEnabled()) {
            return new DefaultLogBuilder(this, LogLevel.DEBUG);
        } else {
            return NO_OP_LOG_BUILDER;
        }
    }

    /**
     * Log a message at the debug level. Default values are used for all other
     * parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     */
    public void debug(final String message) {
        log(LogLevel.DEBUG, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message with a <code>Throwable</code> at the debug level. Default
     * values are used for all other parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void debug(final String message, final Throwable throwable) {
        log(LogLevel.DEBUG, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event at the debug level. Default values
     * are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     */
    public void debug(final String event, final String message) {
        log(LogLevel.DEBUG, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with a <code>Throwable</code> at the
     * debug level. Default values are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void debug(final String event, final String message, final Throwable throwable) {
        log(LogLevel.DEBUG, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the debug level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     */
    public void debug(
            final String event,
            final String message,
            final Map<String, Object> data) {
        debug(event, message, data, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the debug level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void debug(
            final String event,
            final String message,
            final Map<String, Object> data,
            final Throwable throwable) {
        if (_slf4jLogger.isDebugEnabled()) {
            LogLevel.DEBUG.log(
                    _slf4jLogger,
                    event,
                    createKeysFromCollection(
                            data == null ? Collections.emptyList() : data.keySet(),
                            MESSAGE_DATA_KEY),
                    createValuesFromCollection(
                            data == null ? Collections.emptyList() : data.values(),
                            message),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the debug level.
     *
     * The number of elements in the <code>dataNames</code> array should
     * match the number of arguments provided as <code>data</code>
     * unless a <code>Throwable</code> is specified as the final argument in
     * <code>data</code> in which case there would be one fewer element
     * in the <code>dataNames</code> array compared to the number of
     * arguments provided as <code>data</code>.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues data values matching data keys by index. The
     * final data argument may be a <code>Throwable</code> which does
     * not require a matching data name.
     */
    public void debug(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object... dataValues) {
        final Throwable throwable = extractThrowable(dataKeys, dataValues);
        log(LogLevel.DEBUG, event, message, dataKeys, chompArray(dataValues, throwable == null ? 0 : 1), throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the debug level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues Array of data values.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void debug(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object[] dataValues,
            final Throwable throwable) {
        log(
                LogLevel.DEBUG,
                event,
                message,
                dataKeys,
                dataValues,
                throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the debug level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     */
    public void debug(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1) {
        debug(event, message, dataKey1, dataValue1, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the debug level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void debug(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1,
            final Throwable throwable) {
        if (_slf4jLogger.isDebugEnabled()) {
            LogLevel.DEBUG.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1),
                    createValuesFromArgs(message, dataValue1),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the debug level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     */
    public void debug(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2) {
        debug(event, message, dataKey1, dataKey2, dataValue1, dataValue2, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the debug level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void debug(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2,
            final Throwable throwable) {
        if (_slf4jLogger.isDebugEnabled()) {
            LogLevel.DEBUG.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1, dataKey2),
                    createValuesFromArgs(message, dataValue1, dataValue2),
                    throwable);
        }
    }

    /**
     * Create a new log event at the info level. Do not pre-build and cache
     * <code>LogBuilder</code> instances.
     *
     * @return Instance of <code>LogBuilder</code>.
     */
    public LogBuilder info() {
        if (_slf4jLogger.isInfoEnabled()) {
            return new DefaultLogBuilder(this, LogLevel.INFO);
        } else {
            return NO_OP_LOG_BUILDER;
        }
    }

    /**
     * Log a message at the info level. Default values are used for all other
     * parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     */
    public void info(final String message) {
        log(LogLevel.INFO, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message with a <code>Throwable</code> at the info level. Default
     * values are used for all other parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void info(final String message, final Throwable throwable) {
        log(LogLevel.INFO, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event at the info level. Default values
     * are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     */
    public void info(final String event, final String message) {
        log(LogLevel.INFO, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with a <code>Throwable</code> at the
     * info level. Default values are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void info(final String event, final String message, final Throwable throwable) {
        log(LogLevel.INFO, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the info level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     */
    public void info(
            final String event,
            final String message,
            final Map<String, Object> data) {
        info(event, message, data, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the info level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void info(
            final String event,
            final String message,
            final Map<String, Object> data,
            final Throwable throwable) {
        if (_slf4jLogger.isInfoEnabled()) {
            LogLevel.INFO.log(
                    _slf4jLogger,
                    event,
                    createKeysFromCollection(
                            data == null ? Collections.emptyList() : data.keySet(),
                            MESSAGE_DATA_KEY),
                    createValuesFromCollection(
                            data == null ? Collections.emptyList() : data.values(),
                            message),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the info level.
     *
     * The number of elements in the <code>dataNames</code> array should
     * match the number of arguments provided as <code>data</code>
     * unless a <code>Throwable</code> is specified as the final argument in
     * <code>data</code> in which case there would be one fewer element
     * in the <code>dataNames</code> array compared to the number of
     * arguments provided as <code>data</code>.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues data values matching data keys by index. The
     * final data argument may be a <code>Throwable</code> which does
     * not require a matching data name.
     */
    public void info(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object... dataValues) {
        final Throwable throwable = extractThrowable(dataKeys, dataValues);
        log(LogLevel.INFO, event, message, dataKeys, chompArray(dataValues, throwable == null ? 0 : 1), throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the info level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues Array of data values.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void info(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object[] dataValues,
            final Throwable throwable) {
        log(
                LogLevel.INFO,
                event,
                message,
                dataKeys,
                dataValues,
                throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the info level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     */
    public void info(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1) {
        info(event, message, dataKey1, dataValue1, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the info level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void info(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1,
            final Throwable throwable) {
        if (_slf4jLogger.isInfoEnabled()) {
            LogLevel.INFO.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1),
                    createValuesFromArgs(message, dataValue1),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the info level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     */
    public void info(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2) {
        info(event, message, dataKey1, dataKey2, dataValue1, dataValue2, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the info level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void info(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2,
            final Throwable throwable) {
        if (_slf4jLogger.isInfoEnabled()) {
            LogLevel.INFO.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1, dataKey2),
                    createValuesFromArgs(message, dataValue1, dataValue2),
                    throwable);
        }
    }

    /**
     * Create a new log event at the warn level. Do not pre-build and cache
     * <code>LogBuilder</code> instances.
     *
     * @return Instance of <code>LogBuilder</code>.
     */
    public LogBuilder warn() {
        if (_slf4jLogger.isWarnEnabled()) {
            return new DefaultLogBuilder(this, LogLevel.WARN);
        } else {
            return NO_OP_LOG_BUILDER;
        }
    }

    /**
     * Log a message at the warn level. Default values are used for all other
     * parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     */
    public void warn(final String message) {
        log(LogLevel.WARN, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message with a <code>Throwable</code> at the warn level. Default
     * values are used for all other parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void warn(final String message, final Throwable throwable) {
        log(LogLevel.WARN, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event at the warn level. Default values
     * are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     */
    public void warn(final String event, final String message) {
        log(LogLevel.WARN, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with a <code>Throwable</code> at the
     * warn level. Default values are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void warn(final String event, final String message, final Throwable throwable) {
        log(LogLevel.WARN, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the warn level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     */
    public void warn(
            final String event,
            final String message,
            final Map<String, Object> data) {
        warn(event, message, data, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the warn level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void warn(
            final String event,
            final String message,
            final Map<String, Object> data,
            final Throwable throwable) {
        if (_slf4jLogger.isWarnEnabled()) {
            LogLevel.WARN.log(
                    _slf4jLogger,
                    event,
                    createKeysFromCollection(
                            data == null ? Collections.emptyList() : data.keySet(),
                            MESSAGE_DATA_KEY),
                    createValuesFromCollection(
                            data == null ? Collections.emptyList() : data.values(),
                            message),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the warn level.
     *
     * The number of elements in the <code>dataNames</code> array should
     * match the number of arguments provided as <code>data</code>
     * unless a <code>Throwable</code> is specified as the final argument in
     * <code>data</code> in which case there would be one fewer element
     * in the <code>dataNames</code> array compared to the number of
     * arguments provided as <code>data</code>.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues data values matching data keys by index. The
     * final data argument may be a <code>Throwable</code> which does
     * not require a matching data name.
     */
    public void warn(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object... dataValues) {
        final Throwable throwable = extractThrowable(dataKeys, dataValues);
        log(LogLevel.WARN, event, message, dataKeys, chompArray(dataValues, throwable == null ? 0 : 1), throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the warn level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues Array of data values.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void warn(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object[] dataValues,
            final Throwable throwable) {
        log(
                LogLevel.WARN,
                event,
                message,
                dataKeys,
                dataValues,
                throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the warn level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     */
    public void warn(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1) {
        warn(event, message, dataKey1, dataValue1, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the warn level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void warn(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1,
            final Throwable throwable) {
        if (_slf4jLogger.isWarnEnabled()) {
            LogLevel.WARN.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1),
                    createValuesFromArgs(message, dataValue1),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the warn level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     */
    public void warn(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2) {
        warn(event, message, dataKey1, dataKey2, dataValue1, dataValue2, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the warn level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void warn(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2,
            final Throwable throwable) {
        if (_slf4jLogger.isWarnEnabled()) {
            LogLevel.WARN.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1, dataKey2),
                    createValuesFromArgs(message, dataValue1, dataValue2),
                    throwable);
        }
    }

    /**
     * Create a new log event at the error level. Do not pre-build and cache
     * <code>LogBuilder</code> instances.
     *
     * @return Instance of <code>LogBuilder</code>.
     */
    public LogBuilder error() {
        if (_slf4jLogger.isErrorEnabled()) {
            return new DefaultLogBuilder(this, LogLevel.ERROR);
        } else {
            return NO_OP_LOG_BUILDER;
        }
    }

    /**
     * Log a message at the error level. Default values are used for all other
     * parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     */
    public void error(final String message) {
        log(LogLevel.ERROR, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message with a <code>Throwable</code> at the error level. Default
     * values are used for all other parameters.
     *
     * This method is also found in the SLF4J <code>Logger</code>. It is
     * intended to simplify migration; however, it's use is not recommended
     * as it lacks an identifying event and typically includes serialized
     * as opposed to structured data.
     *
     * @since 1.3.0
     *
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void error(final String message, final Throwable throwable) {
        log(LogLevel.ERROR, DEFAULT_EVENT, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event at the error level. Default values
     * are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     */
    public void error(final String event, final String message) {
        log(LogLevel.ERROR, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with a <code>Throwable</code> at the
     * error level. Default values are used for all other parameters.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void error(final String event, final String message, final Throwable throwable) {
        log(LogLevel.ERROR, event, message, EMPTY_STRING_ARRAY, EMPTY_OBJECT_ARRAY, throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the error level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     */
    public void error(
            final String event,
            final String message,
            final Map<String, Object> data) {
        error(event, message, data, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the error level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param data Map of data key-value pairs.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void error(
            final String event,
            final String message,
            final Map<String, Object> data,
            final Throwable throwable) {
        if (_slf4jLogger.isErrorEnabled()) {
            LogLevel.ERROR.log(
                    _slf4jLogger,
                    event,
                    createKeysFromCollection(
                            data == null ? Collections.emptyList() : data.keySet(),
                            MESSAGE_DATA_KEY),
                    createValuesFromCollection(
                            data == null ? Collections.emptyList() : data.values(),
                            message),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the error level.
     *
     * The number of elements in the <code>dataNames</code> array should
     * match the number of arguments provided as <code>data</code>
     * unless a <code>Throwable</code> is specified as the final argument in
     * <code>data</code> in which case there would be one fewer element
     * in the <code>dataNames</code> array compared to the number of
     * arguments provided as <code>data</code>.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues data values matching data keys by index. The
     * final data argument may be a <code>Throwable</code> which does
     * not require a matching data name.
     */
    public void error(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object... dataValues) {
        final Throwable throwable = extractThrowable(dataKeys, dataValues);
        log(LogLevel.ERROR, event, message, dataKeys, chompArray(dataValues, throwable == null ? 0 : 1), throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the error level.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKeys Array of data keys.
     * @param dataValues Array of data values.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void error(
            final String event,
            final String message,
            final String[] dataKeys,
            final Object[] dataValues,
            final Throwable throwable) {
        log(
                LogLevel.ERROR,
                event,
                message,
                dataKeys,
                dataValues,
                throwable);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the error level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     */
    public void error(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1) {
        error(event, message, dataKey1, dataValue1, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the error level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataValue1 First data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void error(
            final String event,
            final String message,
            final String dataKey1,
            final Object dataValue1,
            final Throwable throwable) {
        if (_slf4jLogger.isErrorEnabled()) {
            LogLevel.ERROR.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1),
                    createValuesFromArgs(message, dataValue1),
                    throwable);
        }
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs at
     * the error level. This method is provided only for efficiency over the
     * var-args method above as it avoids an array creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     */
    public void error(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2) {
        error(event, message, dataKey1, dataKey2, dataValue1, dataValue2, DEFAULT_THROWABLE);
    }

    /**
     * Log a message for a canonical event with supporting key-value pairs and
     * a <code>Throwable</code> at the error level. This method is provided
     * only for efficiency over the var-args method above as it avoids an array
     * creation during invocation.
     *
     * @since 1.3.0
     *
     * @param event The canonical event that occurred.
     * @param message The message to be logged.
     * @param dataKey1 First data name.
     * @param dataKey2 Second data name.
     * @param dataValue1 First data value.
     * @param dataValue2 Second data value.
     * @param throwable The exception (<code>Throwable</code>) to be logged.
     */
    public void error(
            final String event,
            final String message,
            final String dataKey1,
            final String dataKey2,
            final Object dataValue1,
            final Object dataValue2,
            final Throwable throwable) {
        if (_slf4jLogger.isErrorEnabled()) {
            LogLevel.ERROR.log(
                    _slf4jLogger,
                    event,
                    createKeysFromArgs(MESSAGE_DATA_KEY, dataKey1, dataKey2),
                    createValuesFromArgs(message, dataValue1, dataValue2),
                    throwable);
        }
    }

    /* package private */ void log(
            final LogLevel level,
            final String event,
            final String message,
            final String[] dataKeys,
            final Object[] dataValues,
            final Throwable throwable) {
        level.log(
                _slf4jLogger,
                event,
                createKeysFromArray(dataKeys, MESSAGE_DATA_KEY),
                createValuesFromArray(dataValues, message),
                throwable);
    }

    /* package private */ void log(
            final LogLevel level,
            final String event,
            final List<String> dataKeys,
            final List<Object> dataValues,
            final List<String> contextKeys,
            final List<Object> contextValues,
            final Throwable throwable) {
        level.log(
                _slf4jLogger,
                event,
                dataKeys,
                dataValues,
                contextKeys,
                contextValues,
                throwable);
    }

    /* package private */ static String[] createKeysFromCollection(final Collection<String> collection, final String... keys) {
        if (isNullOrEmpty(keys)) {
            return collection.toArray(new String[collection.size()]);
        }
        if (isNullOrEmpty(collection)) {
            return keys;
        }
        final String[] combined = new String[collection.size() + keys.length];
        for (int i = 0; i < keys.length; ++i) {
            combined[i] = keys[i];
        }
        int i = 0;
        for (final String item : collection) {
            combined[keys.length + i++] = item;
        }
        return combined;
    }

    /* package private */ static Object[] createValuesFromCollection(final Collection<Object> collection, final Object... values) {
        if (isNullOrEmpty(values)) {
            return collection.toArray(new Object[collection.size()]);
        }
        if (isNullOrEmpty(collection)) {
            return values;
        }
        final Object[] combined = new Object[collection.size() + values.length];
        for (int i = 0; i < values.length; ++i) {
            combined[i] = values[i];
        }
        int i = 0;
        for (final Object item : collection) {
            combined[values.length + i++] = item;
        }
        return combined;
    }

    /* package private */ static String[] createKeysFromArray(final String[] array, final String... keys) {
        if (isNullOrEmpty(keys)) {
            return array;
        }
        if (isNullOrEmpty(array)) {
            return keys;
        }
        final String[] combined = Arrays.copyOf(keys, array.length + keys.length);
        for (int i = 0; i < array.length; ++i) {
            combined[keys.length + i] = array[i];
        }

        return combined;
    }

    /* package private */ static Object[] createValuesFromArray(final Object[] array, final Object... values) {
        if (isNullOrEmpty(values)) {
            return array;
        }
        if (isNullOrEmpty(array)) {
            return values;
        }
        final Object[] combined = Arrays.copyOf(values, array.length + values.length);
        for (int i = 0; i < array.length; ++i) {
            combined[values.length + i] = array[i];
        }
        return combined;
    }

    /* package private */ static Throwable extractThrowable(final String[] keys, final Object[] values) {
        final int keyLength = keys == null ? 0 : keys.length;
        if (values != null && values.length == keyLength + 1) {
            final int throwableIndex = values.length - 1;
            if (values[throwableIndex] instanceof Throwable) {
                return (Throwable) values[throwableIndex];
            }
        }
        return null;
    }

    /* package private */ org.slf4j.Logger getSlf4jLogger() {
        return _slf4jLogger;
    }

    private static String[] createKeysFromArgs(final String... keys) {
        return keys;
    }

    private static Object[] createValuesFromArgs(final Object... values) {
        return values;
    }

    private static boolean isNullOrEmpty(final String[] array) {
        if (array == null) {
            return true;
        }
        if (array.length == 0) {
            return true;
        }
        return false;
    }

    private static boolean isNullOrEmpty(final Object[] array) {
        if (array == null) {
            return true;
        }
        if (array.length == 0) {
            return true;
        }
        return false;
    }

    private static boolean isNullOrEmpty(final Collection<? extends Object> collection) {
        if (collection == null) {
            return true;
        }
        if (collection.size() == 0) {
            return true;
        }
        return false;
    }

    private static Object[] chompArray(final Object[] in, final int count) {
        if (count == 0) {
            return in;
        }
        return Arrays.copyOf(in, in.length - count);
    }

    /* package private */ Logger(final org.slf4j.Logger slf4jLogger) {
        _slf4jLogger = slf4jLogger;
    }

    private org.slf4j.Logger _slf4jLogger;

    private static final String DEFAULT_EVENT = null;
    private static final Throwable DEFAULT_THROWABLE = null;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final LogBuilder NO_OP_LOG_BUILDER = new NoOpLogBuilder();

    /* package private */ static final String MESSAGE_DATA_KEY = "message";
}
