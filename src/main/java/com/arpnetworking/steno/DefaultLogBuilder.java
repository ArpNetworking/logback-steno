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

import java.util.ArrayList;
import java.util.List;

/**
 * Class facilitates type-safe construction of a Steno log message. Instances
 * of this class are not thread safe.
 *
 * @since 1.3.0
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class DefaultLogBuilder implements LogBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setEvent(final String value) {
        _event = value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setMessage(final String value) {
        addData(Logger.MESSAGE_DATA_KEY, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setThrowable(final Throwable value) {
        _throwable = value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultLogBuilder addData(final String name, final Object value) {
        if (_dataKeys == null) {
            _dataKeys = new ArrayList<String>();
            _dataValues = new ArrayList<Object>();
        }
        _dataKeys.add(name);
        _dataValues.add(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultLogBuilder addContext(final String name, final Object value) {
        if (_contextKeys == null) {
            _contextKeys = new ArrayList<String>();
            _contextValues = new ArrayList<Object>();
        }
        _contextKeys.add(name);
        _contextValues.add(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log() {
        _logger.log(
                _level,
                _event,
                _dataKeys,
                _dataValues,
                _contextKeys,
                _contextValues,
                _throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{Logger=" + _logger
                + ", Level=" + _level
                + ", Event=" + _event
                + ", Throwable=" + _throwable
                + ", DataKeys=" + _dataKeys
                + ", DataValues=" + _dataValues
                + ", ContextKeys=" + _contextKeys
                + ", ContextValues=" + _contextValues
                + "}";
    }

    /* package private */ DefaultLogBuilder(final Logger logger, final LogLevel level) {
        _logger = logger;
        _level = level;
    }

    private final Logger _logger;
    private final LogLevel _level;
    private String _event = null;
    private Throwable _throwable = null;
    private List<String> _dataKeys = null;
    private List<Object> _dataValues = null;
    private List<String> _contextKeys = null;
    private List<Object> _contextValues = null;
}
