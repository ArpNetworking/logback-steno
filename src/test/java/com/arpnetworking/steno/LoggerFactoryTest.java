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

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Tests for <code>LoggerFactory</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public class LoggerFactoryTest {

    @Test
    public void testGetLoggerWithClass() {
        final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
        final org.slf4j.Logger slf4jLogger = logger.getSlf4jLogger();
        Assert.assertTrue(slf4jLogger instanceof ch.qos.logback.classic.Logger);
        final ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) slf4jLogger;
        Assert.assertEquals("com.arpnetworking.steno.LoggerFactoryTest", logbackLogger.getName());
    }

    @Test
    public void testGetLoggerWithName() {
        final Logger logger = LoggerFactory.getLogger("MyLogger");
        final org.slf4j.Logger slf4jLogger = logger.getSlf4jLogger();
        Assert.assertTrue(slf4jLogger instanceof ch.qos.logback.classic.Logger);
        final ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) slf4jLogger;
        Assert.assertEquals("MyLogger", logbackLogger.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    // CHECKSTYLE.OFF: IllegalThrows - InvocationTargetException target is Throwable
    public void testPrivateConstructor() throws Throwable {
        // CHECKSTYLE.ON: IllegalThrows
        final Constructor<LoggerFactory> constructor = LoggerFactory.class.getDeclaredConstructor();
        try {
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (final InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }
}