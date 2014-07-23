/**
 * Copyright 2014 Groupon.com
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;

/**
 * An alternative triggering policy that adds a random offset (calculated once on startup) to the rolling time of a
 * TimeBasedRollingPolicy.  This allows each node in a cluster of servers to have randomized rolling times so that
 * any performance impact associated by log rolling doesn't hit all nodes at the same time.
 * <p/>
 * The default maximum offset is 1 hour.  If you have a rolling period lower than an hour then this value will need
 * to be modified.  If the rolling period is hourly then there is the possibility that the first log roll will
 * occur up to 2 hours after startup.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
@NoAutoStart
public class RandomizedTimeBasedFNATP<E> extends DefaultTimeBasedFileNamingAndTriggeringPolicy<E> {
    private static final int DEFAULT_MAX_OFFSET = 3600000; // 1 hour
    private int randomOffsetInMillis;
    private int maxOffsetInMillis = DEFAULT_MAX_OFFSET;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        SecureRandom random;
        try {
            final String seed = InetAddress.getLocalHost().getHostName();
            random = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            // This shouldn't happen, not sure if it's okay to log from within a component of the logging library
            random = new SecureRandom();
        }

        this.randomOffsetInMillis = random.nextInt(this.maxOffsetInMillis);
        super.start();
    }

    /**
     * Retrieve the maximum random offset in milliseconds that will be applied. Default is 1 hour.
     *
     * @return max offset in milliseconds
     */
    public int getMaxOffsetInMillis() {
        return this.maxOffsetInMillis;
    }

    /**
     * Set the maximum random offset in milliseconds that will be applied. Default is 1 hour.
     *
     * @param maxOffsetInMillis - maximum allowed offset in milliseconds
     */
    public void setMaxOffsetInMillis(final int maxOffsetInMillis) {
        this.maxOffsetInMillis = maxOffsetInMillis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void computeNextCheck() {
        nextCheck = rc.getNextTriggeringMillis(dateInCurrentPeriod) + this.randomOffsetInMillis;
    }
}
