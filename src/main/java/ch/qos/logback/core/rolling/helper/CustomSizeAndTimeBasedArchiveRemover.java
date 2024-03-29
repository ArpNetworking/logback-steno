/*
 * Copyright 2016 Inscope Metrics Inc.
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
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.FileSize;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;

/**
 * This is a customization of the {@link SizeAndTimeBasedArchiveRemover}
 * to enforce total size on each clean and to count all files not just periods.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 * @since 1.16.1
 */
public class CustomSizeAndTimeBasedArchiveRemover extends SizeAndTimeBasedArchiveRemover {

    /**
     * Public constructor.
     *
     * @param fileNamePattern The {@link FileNamePattern} from the {@link ch.qos.logback.core.rolling.TimeBasedRollingPolicy}.
     * @param rollingCalendar The {@link RollingCalendar} from the {@link ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP}.
     */
    public CustomSizeAndTimeBasedArchiveRemover(
            final FileNamePattern fileNamePattern,
            final RollingCalendar rollingCalendar) {
        super(fileNamePattern, rollingCalendar);
    }

    @Override
    public void clean(final Instant now) {
        super.clean(now);
        if (_totalSizeCap != CoreConstants.UNBOUNDED_TOTAL_SIZE_CAP && _totalSizeCap > 0) {
            capTotalSize(now);
        }
    }

    @Override
    public void setTotalSizeCap(final long totalSizeCap) {
        super.setTotalSizeCap(totalSizeCap);
        _totalSizeCap = totalSizeCap;
    }

    @Override
    public void setMaxHistory(final int maxHistory) {
        super.setMaxHistory(maxHistory);
        _maxHistory = maxHistory;
    }

    @Override
    void capTotalSize(final Instant now) {
        long totalSize = 0;
        int totalFilesRemoved = 0;
        long totalBytesRemoved = 0;
        int fileIndex = 0;
        for (int offset = 0; offset < _maxHistory; ++offset) {
            final Instant date = rc.getEndOfNextNthPeriod(now, -offset);
            final File[] matchingFileArray = getFilesInPeriod(date);
            descendingSortByLastModified(matchingFileArray);
            for (final File file : matchingFileArray) {
                final long fileSizeInBytes = file.length();
                if (totalSize + fileSizeInBytes > _totalSizeCap) {
                    if (fileIndex >= UNTOUCHABLE_ARCHIVE_FILE_COUNT) {
                        addInfo(
                                String.format(
                                        "Deleting [%s] of size %s",
                                        file,
                                        new FileSize(fileSizeInBytes)));
                        totalBytesRemoved += fileSizeInBytes;
                        ++totalFilesRemoved;
                        if (!file.delete()) {
                            addWarn(
                                    String.format(
                                            "Deleting [%s] failed.",
                                            file));
                        }
                    } else {
                        addWarn(
                                String.format(
                                        "Skipping [%s] of size %s as it is one of the two newest log achives.",
                                        file,
                                        new FileSize(fileSizeInBytes)));
                    }
                }
                totalSize += fileSizeInBytes;
                ++fileIndex;
            }
        }
        addInfo(String.format("Removed %d files totalling %s", totalFilesRemoved, new FileSize(totalBytesRemoved)));
    }

    private void descendingSortByLastModified(final File[] matchingFileArray) {
        Arrays.sort(matchingFileArray, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
    }

    private long _totalSizeCap;
    private int _maxHistory;

    private static final int UNTOUCHABLE_ARCHIVE_FILE_COUNT = 2;
}
