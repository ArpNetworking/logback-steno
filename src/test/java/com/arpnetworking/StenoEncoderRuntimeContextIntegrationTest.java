/*
 * Copyright 2016 Ville Koskela
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
package com.arpnetworking;

import com.arpnetworking.logback.StenoMarker;
import org.junit.Test;

import java.util.Collections;

/**
 * Integration test between <code>StenoEncoder</code> and runtime context injection.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class StenoEncoderRuntimeContextIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        getLogger().info("Slf4j");

        getLogger().info(
                StenoMarker.ARRAY_MARKER,
                "IT", new String[] { "message" },
                new Object[] { "Marker" });

        getStenoLogger().info("Steno.Log");

        getStenoLogger().info("Steno.Log", new IllegalArgumentException("IAE"));

        getStenoLogger().info().setEvent("IT").setMessage("Steno.LogBuilder").log();

        getStenoLogger().info(l -> {
            l.setEvent("IT").setMessage("Steno.LambdaLogBuilder");
        });

        getStenoLogger().info("IT", "Steno.LogMessage");

        getStenoLogger().info("IT", "Steno.LogMessage", new IllegalArgumentException("IAE"));

        getStenoLogger().info("IT", "Steno.LogMap", Collections.singletonMap("Foo", "Bar"));

        getStenoLogger().info(
                "IT",
                "Steno.LogMap",
                Collections.singletonMap("Foo", "Bar"),
                new IllegalArgumentException("IAE"));

        getStenoLogger().info("IT", "Steno.LogArray", new String[]{"Foo"}, "Bar");

        getStenoLogger().info(
                "IT",
                "Steno.LogArray",
                new String[]{"Foo"},
                new String[]{"Bar"},
                new IllegalArgumentException("IAE"));

        getStenoLogger().info("IT", "Steno.LogDirect", "Foo", "Bar");

        getStenoLogger().info("IT", "Steno.LogDirect", "Foo", "Bar", new IllegalArgumentException("IAE"));

        getStenoLogger().info("IT", "Steno.LogDirect", "Foo", "Hi", "Bar", "Ho");

        getStenoLogger().info("IT", "Steno.LogDirect", "Foo", "Hi", "Bar", "Ho", new IllegalArgumentException("IAE"));

        assertOutput();
    }
}
