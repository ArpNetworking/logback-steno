/*
 * Copyright 2020 Dropbox
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

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

/**
 * Integration test of logging Guava multimap collection.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public class GuavaMultimapIntegrationTest extends BaseStenoIntegrationTest {

    @Test
    public void test() {
        final ImmutableMultimap<String, String> multimapEmpty = ImmutableMultimap.of();
        final ImmutableMultimap<String, String> multimapSingleValue =
                ImmutableMultimap.of("MAPKEY1", "MAPVALUE1");
        final ImmutableMultimap<String, String> multimapMultipleValues =
                ImmutableMultimap.<String, String>builder()
                        .put("MAPKEY1", "MAPVALUE1")
                        .put("MAPKEY2", "MAPVALUE2")
                        .put("MAPKEY2", "MAPVALUE3")
                        .build();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("1. This is an empty multimap")
                .addData("MAP1", multimapEmpty)
                .log();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("2. This is an single-entry multimap")
                .addData("MAP2", multimapSingleValue)
                .log();

        getStenoLogger().error()
                .setEvent("IT")
                .setMessage("3. This is an multi-entry multimap")
                .addData("MAP3", multimapMultipleValues)
                .log();

        assertOutput();
    }
}
