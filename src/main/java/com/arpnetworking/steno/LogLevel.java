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
package com.arpnetworking.steno;

import com.arpnetworking.logback.StenoMarker;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Enumeration of log levels.
 *
 * @since 1.3.0
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
/* package private */ enum LogLevel {
    TRACE {
        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                @Nullable final String[] keys,
                @Nullable final Object[] values,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.trace(StenoMarker.ARRAY_MARKER, event, keys, values, throwable);
                } else {
                    logger.trace(StenoMarker.ARRAY_MARKER, event, keys, values);
                }
            }
        }

        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                final List<String> dataKeys,
                final List<Object> dataValues,
                final List<String> contextKeys,
                final List<Object> contextValues,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.trace(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues, throwable);
                } else {
                    logger.trace(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues);
                }
            }
        }

        @Override
        public boolean isEnabled(final org.slf4j.Logger logger) {
            return logger.isTraceEnabled();
        }
    },
    DEBUG {
        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                @Nullable final String[] keys,
                @Nullable final Object[] values,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.debug(StenoMarker.ARRAY_MARKER, event, keys, values, throwable);
                } else {
                    logger.debug(StenoMarker.ARRAY_MARKER, event, keys, values);
                }
            }
        }

        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                final List<String> dataKeys,
                final List<Object> dataValues,
                final List<String> contextKeys,
                final List<Object> contextValues,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.debug(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues, throwable);
                } else {
                    logger.debug(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues);
                }
            }
        }

        @Override
        public boolean isEnabled(final org.slf4j.Logger logger) {
            return logger.isDebugEnabled();
        }
    },
    INFO {
        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                @Nullable final String[] keys,
                @Nullable final Object[] values,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.info(StenoMarker.ARRAY_MARKER, event, keys, values, throwable);
                } else {
                    logger.info(StenoMarker.ARRAY_MARKER, event, keys, values);
                }
            }
        }

        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                final List<String> dataKeys,
                final List<Object> dataValues,
                final List<String> contextKeys,
                final List<Object> contextValues,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.info(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues, throwable);
                } else {
                    logger.info(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues);
                }
            }
        }

        @Override
        public boolean isEnabled(final org.slf4j.Logger logger) {
            return logger.isInfoEnabled();
        }
    },
    WARN {
        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                @Nullable final String[] keys,
                @Nullable final Object[] values,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.warn(StenoMarker.ARRAY_MARKER, event, keys, values, throwable);
                } else {
                    logger.warn(StenoMarker.ARRAY_MARKER, event, keys, values);
                }
            }
        }

        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                final List<String> dataKeys,
                final List<Object> dataValues,
                final List<String> contextKeys,
                final List<Object> contextValues,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.warn(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues, throwable);
                } else {
                    logger.warn(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues);
                }
            }
        }

        @Override
        public boolean isEnabled(final org.slf4j.Logger logger) {
            return logger.isWarnEnabled();
        }
    },
    ERROR {
        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                @Nullable final String[] keys,
                @Nullable final Object[] values,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.error(StenoMarker.ARRAY_MARKER, event, keys, values, throwable);
                } else {
                    logger.error(StenoMarker.ARRAY_MARKER, event, keys, values);
                }
            }
        }

        @Override
        public void log(
                final org.slf4j.Logger logger,
                @Nullable final String event,
                final List<String> dataKeys,
                final List<Object> dataValues,
                final List<String> contextKeys,
                final List<Object> contextValues,
                @Nullable final Throwable throwable) {
            if (isEnabled(logger)) {
                if (throwable != null) {
                    logger.error(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues, throwable);
                } else {
                    logger.error(StenoMarker.LISTS_MARKER, event, dataKeys, dataValues, contextKeys, contextValues);
                }
            }
        }

        @Override
        public boolean isEnabled(final org.slf4j.Logger logger) {
            return logger.isErrorEnabled();
        }
    };

    public abstract void log(
            org.slf4j.Logger logger,
            @Nullable String event,
            @Nullable String[] keys,
            @Nullable Object[] values,
            @Nullable Throwable throwable);

    public abstract void log(
            org.slf4j.Logger logger,
            @Nullable String event,
            List<String> dataKeys,
            List<Object> dataValues,
            List<String> contextKeys,
            List<Object> contextValues,
            @Nullable Throwable throwable);

    public abstract boolean isEnabled(org.slf4j.Logger logger);
}
