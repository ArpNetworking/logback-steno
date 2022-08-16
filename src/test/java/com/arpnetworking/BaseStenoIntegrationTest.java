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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Base integration test for {@link com.arpnetworking.logback.StenoEncoder}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public abstract class BaseStenoIntegrationTest extends BaseIntegrationTest {

    @Override
    protected void assertOutput(final String expected, final String actual) {
        // Compare line by line
        final String[] expectedLines = expected.split("\n");
        final String[] actualLines = actual.split("\n");
        if (expectedLines.length != actualLines.length) {
            Assert.assertEquals(expected, actual);
            Assert.fail("Number of lines differed but content was identical?");
        }

        for (int i = 0; i < expectedLines.length; ++i) {
            final String expectedLine = expectedLines[i];
            final String actualLine = actualLines[i];
            final String sanitizedActualLine = sanitizeOutput(actualLine);

            // Validate actual against JSON schema
            try {
                final ObjectNode rootNode = (ObjectNode) OBJECT_MAPPER.readTree(actualLine);
                final ObjectNode contextNode = (ObjectNode) rootNode.get("context");
                if (contextNode != null) {
                    contextNode.remove("logger");
                    contextNode.remove("MDC_KEY1");
                    contextNode.remove("MDC_KEY2");
                }
                final Set<ValidationMessage> report = STENO_SCHEMA.validate(rootNode);
                Assert.assertEquals("Line: " + (i + 1) + " " + report.toString(), 0, report.size());
            } catch (final IOException e) {
                Assert.fail("Failed with exception: " + e);
            }

            // Compare actual and expected as json nodes
            try {
                final JsonNode expectedJsonNode = OBJECT_MAPPER.readTree(expectedLine);
                final JsonNode actualJsonNode = OBJECT_MAPPER.readTree(sanitizedActualLine);
                Assert.assertEquals("Line: " + (i + 1), expectedJsonNode, actualJsonNode);
            } catch (final IOException e) {
                Assert.fail("Failed with exception: " + e);
            }
        }
    }

    @Override
    protected String sanitizeOutput(final String output) {
        return output.replaceAll("\"time\":\"[^\"]+\"", "\"time\":\"<TIME>\"")
                .replaceAll("\"id\":\"[^\"]+\"", "\"id\":\"<ID>\"")
                .replaceAll("\"host\":\"[^\"]+\"", "\"host\":\"<HOST>\"")
                .replaceAll("\"processId\":\"[^\"]+\"", "\"processId\":\"<PROCESS_ID>\"")
                .replaceAll("\"threadId\":\"[^\"]+\"", "\"threadId\":\"<THREAD_ID>\"")
                .replaceAll("\"backtrace\":\\[[^\\]]+\\]", "\"backtrace\":[]")
                .replaceAll("\"_id\":\"[^\"]+\"", "\"_id\":\"<ID>\"");
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonSchema STENO_SCHEMA;

    static {
        try (InputStream schemaStream = StenoMarker.class.getResourceAsStream("/steno.schema.json")) {
            STENO_SCHEMA = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(schemaStream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
