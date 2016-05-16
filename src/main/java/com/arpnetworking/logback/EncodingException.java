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
package com.arpnetworking.logback;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown by the encoder when it encounters any failure.
 *
 * @since 1.7.0
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
/* package private */ class EncodingException extends Exception {

    /**
     * Public constructor.
     *
     * @param context The context of the event being processed when the encoding failure was encountered.
     * @param cause The failure encountered during encoding.
     */
    /* package private */ EncodingException(final Map<String, Object> context, final Throwable cause) {
        super("Encoding Exception", cause);
        _context = context;
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(_context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "EncodingException context=" + _context + " cause=" + getCause();
    }

    private final Map<String, Object> _context;

    private static final long serialVersionUID = 1;
}
