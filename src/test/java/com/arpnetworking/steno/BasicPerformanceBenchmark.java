/**
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
package com.arpnetworking.steno;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.UUID;

/**
 * Basic performance test used for comparative purposes across implementations.
 *
 * Execute this using the command:
 * <pre>
 *     mvn -DskipCoverage=true -Dtest=BasicPerformanceBenchmark test > /dev/null
 * </pre>
 *
 * The test is not named with the conventional "Test" suffix to avoid running it
 * during normal test execution.
 *
 * @author Ville Koskela (ville at koskelafamily dot com)
 */
public final class BasicPerformanceBenchmark {

    /**
     * Setup the performance benchmark.
     */
    @Before
    public void setUp() {
        final URL configuration = getClass().getResource("BasicPerformanceBenchmark.xml");
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

    /**
     * Cleanup after the performance benchmark.
     */
    @After
    public void tearDown() {
        _loggerContext.stop();
        _loggerContext = null;
    }

    /**
     * Execute the performance benchmark.
     */
    @Test
    public void test() {
        final Logger logger = getLogger();
        final Exception e = new Exception("This is an error");
        final String uuid1 = UUID.randomUUID().toString();
        final String uuid2 = UUID.randomUUID().toString();

        final long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; ++i) {
            logger.info()
                    .setEvent("performance_event")
                    .setMessage("This is a message from the steno logger")
                    .setThrowable(e)
                    .addContext("requestId", uuid1)
                    .addData("userId", uuid2)
                    .log();
        }
        final long end = System.nanoTime();
        final double elapsed = (end - start) / 1000000000.0;
        System.err.printf("Elapsed %f seconds%n", elapsed);
    }

    private Logger getLogger() {
        return new Logger(_loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME));
    }

    private LoggerContext _loggerContext;

    private static final int ITERATIONS = 100000;
}
