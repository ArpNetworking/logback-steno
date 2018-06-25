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
package com.arpnetworking.logback;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.CustomSizeAndTimeBasedArchiveRemover;
import ch.qos.logback.core.rolling.helper.FileNamePattern;

import java.util.Date;

/**
 * Extends <code>RandomizedTimeBasedFNATP</code> to also support file rolling
 * when a maximum file size is exceeded. The extension leverages existing code
 * in <code>SizeAndTimeBasedFNATP</code>.
 *
 * Unfortunately, the "extension" is done in a rather round-about manner. The
 * <code>SizeAndTimeBasedFNATP</code> class as a part of Logback is released
 * under EPL which is not compatible with AL2. Therefore, we can't just copy
 * that class into this library and change the parent class. Instead the
 * resulting implementation extends <code>SizeAndTimeBasedFNATP</code> and
 * either splits (e.g. calls methods on both parent and encapsulated FNATPs)
 * or delegates (e.g. calls only method on parent or encapsulated FNATP).
 *
 * @param <E> The event type.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.10.0
 */
@NoAutoStart
public class SizeAndRandomizedTimeBasedFNATP<E> extends SizeAndTimeBasedFNATP<E> {

    /**
     * Public constructor.
     */
    public SizeAndRandomizedTimeBasedFNATP() {
        super();
        _randomizedTimeBasedFNATP = new RandomizedTimeBasedFNATP<>();
    }

    /* package private */ SizeAndRandomizedTimeBasedFNATP(final RandomizedTimeBasedFNATP<E> randomizedTimeBasedFNATP) {
        super();
        _randomizedTimeBasedFNATP = randomizedTimeBasedFNATP;
    }

    /**
     * Retrieve the maximum random offset in milliseconds that will be applied. Default is 1 hour.
     *
     * @return max offset in milliseconds
     */
    public int getMaxOffsetInMillis() {
        return _randomizedTimeBasedFNATP.getMaxOffsetInMillis();
    }

    /**
     * Set the maximum random offset in milliseconds that will be applied. Default is 1 hour.
     *
     * @param maxOffsetInMillis - maximum allowed offset in milliseconds
     */
    public void setMaxOffsetInMillis(final int maxOffsetInMillis) {
        _randomizedTimeBasedFNATP.setMaxOffsetInMillis(maxOffsetInMillis);
    }

    @Override
    public void start() {
        _randomizedTimeBasedFNATP.start();
        super.start();
    }

    @Override
    public void stop() {
        _randomizedTimeBasedFNATP.stop();
        super.stop();
    }

    @Override
    public void setContext(final Context context) {
        _randomizedTimeBasedFNATP.setContext(context);
        super.setContext(context);
    }

    @Override
    public void setDateInCurrentPeriod(final Date dateInCurrentPeriod) {
        _randomizedTimeBasedFNATP.setDateInCurrentPeriod(dateInCurrentPeriod);
        super.setDateInCurrentPeriod(dateInCurrentPeriod);
    }

    @Override
    public void setCurrentTime(final long timeInMillis) {
        _randomizedTimeBasedFNATP.setCurrentTime(timeInMillis);
        super.setCurrentTime(timeInMillis);
    }

    @Override
    public long getCurrentTime() {
        return _randomizedTimeBasedFNATP.getCurrentTime();
    }

    @Override
    public void setTimeBasedRollingPolicy(final TimeBasedRollingPolicy<E> tbrp) {
        _randomizedTimeBasedFNATP.setTimeBasedRollingPolicy(tbrp);
        super.setTimeBasedRollingPolicy(tbrp);
    }

    @Override
    protected void computeNextCheck() {
        // This is the important override. It is invoked by isTriggerEvent from SizeAndTimeBasedFNATP
        // to set the next roll over time based on the time part of the rolling policy. It must be
        // delegated entirely and only to the encapsulated randomized time based policy instance.
        _randomizedTimeBasedFNATP.computeNextCheck();
        nextCheck = _randomizedTimeBasedFNATP.getNextCheck();
    }

    @Override
    protected void setDateInCurrentPeriod(final long now) {
        _randomizedTimeBasedFNATP.getDateInCurrentPeriod().setTime(now);
        super.setDateInCurrentPeriod(now);
    }

    @Override
    protected ArchiveRemover createArchiveRemover() {
        return new CustomSizeAndTimeBasedArchiveRemover(
                new FileNamePattern(this.tbrp.getFileNamePattern(), this.context),
                this.rc);
    }

    /* package private */ long getNextCheck() {
        return nextCheck;
    }

    private final RandomizedTimeBasedFNATP<E> _randomizedTimeBasedFNATP;
}
