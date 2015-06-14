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
package com.arpnetworking.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arpnetworking.logback.serialization.keyvalue.KeyValueSerializationHelper;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Encoder to take a Steno log event {@link com.arpnetworking.logback.StenoMarker} and convert it to an
 * easier to read key=value output format when running locally.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public class KeyValueEncoder extends BaseLoggingEncoder {

    /**
     * Sets the default log event name.
     *
     * @param logEventName The log event name.
     *
     * @since 1.8.1
     */
    public void setLogEventName(final String logEventName) {
        _logEventName = logEventName;
    }

    /**
     * Retrieve the default log event name.
     *
     * @return The default log event name.
     *
     * @since 1.8.1
     */
    public String getLogEventName() {
        return _logEventName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String encodeAsString(final ILoggingEvent event, final EncodingException ee) {
        return ee.toString() + " originalMessage=" + event.getMessage() + "\n";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildArrayMessage(
            final ILoggingEvent event,
            final String eventName,
            final String[] keys,
            final Object[] values)
            throws EncodingException {

        try {
            return createMessage(event, eventName, keys, values);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildArrayJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final String[] keys,
            final String[] jsonValues)
            throws EncodingException {

        try {
            final Object[] escapedJsonValues = jsonValues == null ? null : escapeStringValues(jsonValues);
            return createMessage(event, eventName, keys, escapedJsonValues);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMapMessage(
            final ILoggingEvent event,
            final String eventName,
            final Map<String, ? extends Object> map)
            throws EncodingException {

        try {
            final String[] keys = map == null ? null : new String[map.size()];
            final Object[] values = map == null ? null : new Object[map.size()];
            if (map != null) {
                int index = 0;
                for (final Map.Entry<String, ? extends Object> entry : map.entrySet()) {
                    keys[index] = entry.getKey();
                    values[index] = entry.getValue();
                    ++index;
                }
            }

            return createMessage(
                    event,
                    eventName,
                    keys,
                    values);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMapJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final Map<String, String> map)
            throws EncodingException {

        try {
            final String[] keys = map == null ? null : new String[map.size()];
            final Object[] values = map == null ? null : new Object[map.size()];
            if (map != null) {
                int index = 0;
                for (final Map.Entry<String, ? extends Object> entry : map.entrySet()) {
                    keys[index] = entry.getKey();
                    values[index] = entry.getValue();
                    ++index;
                }
            }

            return createMessage(
                    event,
                    eventName,
                    keys,
                    values == null ? null : escapeStringValues(values));
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectMessage(
            final ILoggingEvent event,
            final String eventName,
            final Object data)
            throws EncodingException {

        try {
            return createMessage(
                    event,
                    eventName,
                    new String[]{"data"},
                    escapeStringValues(new Object[]{data == null ? null : data.toString()}));
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildObjectJsonMessage(
            final ILoggingEvent event,
            final String eventName,
            final String jsonData)
            throws EncodingException {

        try {
            return createMessage(
                    event,
                    eventName,
                    new String[]{"data"},
                    escapeStringValues(new Object[]{jsonData}));
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildListsMessage(
            final ILoggingEvent event,
            final String eventName,
            final List<String> dataKeys,
            final List<Object> dataValues,
            final List<String> contextKeys,
            final List<Object> contextValues)
            throws EncodingException {

        try {
            final int dataKeysSize = dataKeys == null ? 0 : dataKeys.size();
            final int contextKeysSize = contextKeys == null ? 0 : contextKeys.size();
            final int dataValuesSize = dataValues == null ? 0 : dataValues.size();
            final int contextValuesSize = contextValues == null ? 0 : contextValues.size();

            final int size = dataKeysSize + contextKeysSize;

            final String[] keys = new String[size];
            final Object[] values = new Object[size];

            int index = 0;
            if (contextKeys != null) {
                for (final String key : contextKeys) {
                    keys[index++] = key;
                }
            }
            if (dataKeys != null) {
                for (final String key : dataKeys) {
                    keys[index++] = key;
                }
            }
            index = 0;
            int valueCount = 0;
            for (int i = 0; i < contextKeysSize; ++i) {
                if (valueCount < contextValuesSize) {
                    values[index] = contextValues.get(i);
                } else {
                    values[index] = null;
                }
                ++index;
                ++valueCount;
            }
            valueCount = 0;
            for (int i = 0; i < dataKeysSize; ++i) {
                if (valueCount < dataValuesSize) {
                    values[index] = dataValues.get(i);
                } else {
                    values[index] = null;
                }
                ++index;
                ++valueCount;
            }

            return createMessage(
                    event,
                    eventName,
                    keys,
                    escapeStringValues(values));
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event, contextKeys, contextValues), t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildStandardMessage(final ILoggingEvent event) throws EncodingException {
        try {
            return getLayout().doLayout(event);
            // CHECKSTYLE.OFF: IllegalCatch: Ensure any exception or error is caught to prevent Appender death.
        } catch (final Throwable t) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new EncodingException(createSafeContext(event), t);
        }
    }

    /**
     * Create a format <code>String</code> compatible with <code>MessageFormatter</code>.
     *
     * @param name The name.
     * @param keys The argument keys.
     * @return The format <code>String</code>.
     */
    protected String buildFormatString(final String name, final String[] keys) {
        final String effectiveName = name == null ? _logEventName : name;
        final StringWriter stringWriter = new StringWriter();
        stringWriter.append("name=\"").append(effectiveName).append("\"");
        if (keys != null && keys.length > 0) {
            for (final String key : keys) {
                stringWriter.append(", ").append(key).append("=\"{}\"");
            }
        }
        return stringWriter.toString();
    }

    /**
     * Escape all <code>String</code> instances.
     *
     * @param values Arguments to encode.
     * @return Encoded arguments.
     */
    protected Object[] escapeStringValues(final Object[] values) {
        final Object[] escapedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            // Instance of check implies value is not null
            if (value instanceof String) {
                value = ((String) value).replaceAll("\"", "\\\\\"");
            }
            escapedValues[i] = value;
        }
        return escapedValues;
    }

    /* package private */ String createMessage(
            final ILoggingEvent event,
            final String eventName,
            final String[] keys,
            final Object[] values) {

        final String formatString = buildFormatString(eventName, keys);
        final LoggingEventWrapper eventWrapper = new LoggingEventWrapper(event, formatString, values);
        return layout.doLayout(eventWrapper);
    }

    /* package private */ Map<String, Object> createSafeContext(final ILoggingEvent event) {
        return createSafeContext(event, Collections.emptyList(), Collections.emptyList());
    }

    /* package private */ Map<String, Object> createSafeContext(
            final ILoggingEvent event,
            final List<String> contextKeys,
            final List<Object> contextValues) {
        return KeyValueSerializationHelper.createContext(this, event, contextKeys, contextValues);
    }

    private String _logEventName = STANDARD_LOG_EVENT_NAME;
    private static final String STANDARD_LOG_EVENT_NAME = "log";
}
