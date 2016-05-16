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

/**
 * Implementation of <code>LogBuilder</code> which does not log a message.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class NoOpLogBuilder implements LogBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setEvent(final String value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setMessage(final String value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setThrowable(final Throwable value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder addData(final String name, final Object value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder addContext(final String name, final Object value) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log() {
        // Nothing to do.
    }
}
