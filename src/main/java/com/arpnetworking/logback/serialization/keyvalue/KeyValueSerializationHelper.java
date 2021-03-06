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
package com.arpnetworking.logback.serialization.keyvalue;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arpnetworking.logback.HostConverter;
import com.arpnetworking.logback.KeyValueEncoder;
import com.arpnetworking.logback.ProcessConverter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Helper functions and functors for Key-Value serialziation.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 * @since 1.9.0
 */
public final class KeyValueSerializationHelper {

    /**
     * Create a context based on the {@link KeyValueEncoder} configuration.
     *
     * @since 1.9.0
     *
     * @param encoder The {@link KeyValueEncoder} instance.
     * @param event The {@link ILoggingEvent} instance.
     * @param contextKeys The additional user provided context keys.
     * @param contextValues The additional user provided context values matching the keys.
     * @return {@link Map} with event context.
     */
    public static Map<String, Object> createContext(
            final KeyValueEncoder encoder,
            final ILoggingEvent event,
            @Nullable final List<String> contextKeys,
            @Nullable final List<Object> contextValues) {
        final Map<String, Object> context = new LinkedHashMap<>();
        context.put("host", KeyValueSerializationHelper.HOST_CONVERTER.convert(event));
        context.put("processId", KeyValueSerializationHelper.PROCESS_CONVERTER.convert(event));
        context.put("threadId", KeyValueSerializationHelper.THREAD_CONVERTER.convert(event));
        if (contextKeys != null) {
            final int contextValuesLength = contextValues == null ? 0 : contextValues.size();
            for (int i = 0; i < contextKeys.size(); ++i) {
                final String key = contextKeys.get(i);
                final Object value = i < contextValuesLength ? contextValues.get(i) : null;
                context.put(
                        key,
                        value);
            }
        }
        return context;
    }

    private static final ClassicConverter HOST_CONVERTER = new HostConverter();
    private static final ClassicConverter PROCESS_CONVERTER = new ProcessConverter();
    private static final ClassicConverter THREAD_CONVERTER = new ThreadConverter();
    private static final ClassicConverter LOGGER_CONVERTER = new LoggerConverter();

    static {
        HOST_CONVERTER.start();
        PROCESS_CONVERTER.start();
        THREAD_CONVERTER.start();
        LOGGER_CONVERTER.start();
    }

    private KeyValueSerializationHelper() {}
}
