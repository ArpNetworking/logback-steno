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

import java.util.Arrays;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/**
 * Logback LoggingEvent wrapper for overriding the message and argumentArray.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public class LoggingEventWrapper extends LoggingEvent {
    private final ILoggingEvent wrappedEvent;
    private final String message;
    private final Object[] argumentArray;
    private transient String formattedMessage;

    /**
     * Public constructor.
     *
     * @param event Instance of <code>ILoggingEvent</code>.
     * @param message The message.
     * @param argumentArray The array of arguments for the message.
     */
    public LoggingEventWrapper(final ILoggingEvent event, final String message, final Object[] argumentArray) {
        this.wrappedEvent = event;
        this.message = message;
        this.argumentArray = argumentArray == null ? null : Arrays.copyOf(argumentArray, argumentArray.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getThreadName() {
        return wrappedEvent.getThreadName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Level getLevel() {
        return wrappedEvent.getLevel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getArgumentArray() {
        return argumentArray == null ? null : Arrays.copyOf(argumentArray, argumentArray.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLoggerName() {
        return wrappedEvent.getLoggerName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoggerContextVO getLoggerContextVO() {
        return wrappedEvent.getLoggerContextVO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IThrowableProxy getThrowableProxy() {
        return wrappedEvent.getThrowableProxy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StackTraceElement[] getCallerData() {
        return wrappedEvent.getCallerData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCallerData() {
        return wrappedEvent.hasCallerData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Marker getMarker() {
        return wrappedEvent.getMarker();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getMDCPropertyMap() {
        return wrappedEvent.getMDCPropertyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public Map<String, String> getMdc() {
        return wrappedEvent.getMdc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimeStamp() {
        return wrappedEvent.getTimeStamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForDeferredProcessing() {
        wrappedEvent.prepareForDeferredProcessing();
    }

    /**
     * Return the the message formatted with arguments. Implemented as suggested in:
     *
     * http://jira.qos.ch/browse/LBCLASSIC-47
     *
     * @return The formatted message.
     */
    public String getFormattedMessage() {
        if (formattedMessage != null) {
            return formattedMessage;
        }
        if (argumentArray != null) {
            formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
        } else {
            formattedMessage = message;
        }

        return formattedMessage;
    }
}
