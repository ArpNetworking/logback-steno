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

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * An alternative triggering policy that adds a random offset (calculated once on startup) to the rolling time of a
 * TimeBasedRollingPolicy.  This allows each node in a cluster of servers to have randomized rolling times so that
 * any performance impact associated by log rolling doesn't hit all nodes at the same time.
 * <br>
 * The default maximum offset is 1 hour.  If you have a rolling period lower than an hour then this value will need
 * to be modified.  If the rolling period is hourly then there is the possibility that the first log roll will
 * occur up to 2 hours after startup.
 *
 * @param <E> The event type.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
@NoAutoStart
public class RandomizedTimeBasedFNATP<E> extends DefaultTimeBasedFileNamingAndTriggeringPolicy<E> {

    /**
     * Public constructor.
     */
    public RandomizedTimeBasedFNATP() {
        this(SecureRandomProvider.DEFAULT, HostProvider.DEFAULT);
    }

    /*package private*/ RandomizedTimeBasedFNATP(
            final SecureRandomProvider secureRandomProvider,
            final HostProvider hostProvider) {

        SecureRandom random;
        try {
            final String seed = hostProvider.get();
            random = secureRandomProvider.get(seed.getBytes(StandardCharsets.UTF_8));
        } catch (final UnknownHostException uhe) {
            random = secureRandomProvider.get();
        }
        _randomNumber = random.nextDouble();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * Retrieve the maximum random offset in milliseconds that will be applied. Default is 1 hour.
     *
     * @return max offset in milliseconds
     */
    public int getMaxOffsetInMillis() {
        return _maxOffsetInMillis;
    }

    /**
     * Set the maximum random offset in milliseconds that will be applied. Default is 1 hour.
     *
     * @param maxOffsetInMillis - maximum allowed offset in milliseconds
     */
    public void setMaxOffsetInMillis(final int maxOffsetInMillis) {
        _maxOffsetInMillis = maxOffsetInMillis;
        _randomOffsetInMillis = (int) (_randomNumber * _maxOffsetInMillis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void computeNextCheck() {
        nextCheck = rc.getNextTriggeringMillis(dateInCurrentPeriod) + _randomOffsetInMillis;
    }

    private final double _randomNumber;
    private int _randomOffsetInMillis;
    private int _maxOffsetInMillis = DEFAULT_MAX_OFFSET;

    private static final int DEFAULT_MAX_OFFSET = 3600000; // 1 hour
}
