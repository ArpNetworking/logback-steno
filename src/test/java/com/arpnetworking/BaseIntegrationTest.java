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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.arpnetworking.steno.TestLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Base integration test.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public abstract class BaseIntegrationTest {

    @Before
    public void setUp() {
        final URL configuration = getClass().getResource(
                this.getClass().getSimpleName() + ".xml");
        _loggerContext = new LoggerContext();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(_loggerContext);
        _loggerContext.reset();
        try {
            configurator.doConfigure(configuration);
        } catch (final JoranException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        _loggerContext.stop();
        _loggerContext = null;
    }

    protected org.slf4j.Logger getLogger() {
        return _loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    }

    protected com.arpnetworking.steno.Logger getStenoLogger() {
        return TestLoggerFactory.getLogger(_loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME));
    }

    protected void assertOutput() {
        final URL expectedResource = getClass().getResource(
                getClass().getSimpleName() + ".expected");
        final File actualFile = new File("target/integration-test-logs/" + this.getClass().getSimpleName() + ".log");
        final String actualOutput;
        final String redactedOutput;
        try {
            // CHECKSTYLE.OFF: IllegalInstantiation - This is valid case.
            actualOutput = new String(Files.readAllBytes(actualFile.toPath()), StandardCharsets.UTF_8);
            // CHECKSTYLE.ON: IllegalInstantiation
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        try {
            // CHECKSTYLE.OFF: IllegalInstantiation - This is how you do it.
            assertOutput(new String(Files.readAllBytes(Paths.get(expectedResource.toURI())), StandardCharsets.UTF_8), actualOutput);
            // CHECKSTYLE.ON: IllegalInstantiation
        } catch (final IOException | URISyntaxException e) {
            Assert.fail("Failed with exception: " + e);
        }
    }

    protected void assertOutput(final String expected, final String actual) {
        final String redacted = sanitizeOutput(actual);
        Assert.assertEquals(expected, redacted);
    }

    protected String sanitizeOutput(final String output) {
        return output;
    }

    private LoggerContext _loggerContext;
}
