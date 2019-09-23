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
package com.arpnetworking;

import com.arpnetworking.logback.StenoMarker;
import org.junit.Test;

/**
 * Simple integration test of the {@link com.arpnetworking.logback.StenoEncoder}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class StenoEncoderIntegrationTest extends BaseStenoIntegrationTest {

    // CHECKSTYLE.OFF: StrictDuplicateCode - Same basic tests as KeyValueEncoderIntegrationTest
    @Test
    public void test() {
        getLogger().trace("1. Trace level events will be suppressed");
        getLogger().debug("2. Debug level events will be suppressed");
        getLogger().info("3. This is informative");
        getLogger().warn("4. This is a warning");
        getLogger().error("5. This is an error");

        getLogger().trace(
                StenoMarker.ARRAY_MARKER,
                "IT",
                new String[] { "message" },
                new Object[] { "6. Trace level events will be suppressed" });
        getLogger().debug(
                StenoMarker.ARRAY_MARKER,
                "IT",
                new String[] { "message" },
                new Object[] { "7. Debug level events will be suppressed" });
        getLogger().info(
                StenoMarker.ARRAY_MARKER,
                "IT", new String[] { "message" },
                new Object[] { "8. This is informative" });
        getLogger().warn(
                StenoMarker.ARRAY_MARKER,
                "IT", new String[] { "message" },
                new Object[] { "9. This is a warning" });
        getLogger().error(
                StenoMarker.ARRAY_MARKER,
                "IT", new String[]{"message"},
                new Object[]{"10. This is an error"});

        getStenoLogger().trace().setEvent("IT").setMessage("11. Trace level events will be suppressed").log();
        getStenoLogger().debug().setEvent("IT").setMessage("12. Debug level events will be suppressed").log();
        getStenoLogger().info().setEvent("IT").setMessage("13. This is informative").log();
        getStenoLogger().warn().setEvent("IT").setMessage("14. This is a warning").log();
        getStenoLogger().error().setEvent("IT").setMessage("15. This is an error").log();

        assertOutput();
    }
    // CHECKSTYLE.ON: StrictDuplicateCode
}
