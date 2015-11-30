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
package ch.qos.logback.core.rolling.helper;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

/**
 * This is a customization of the <code>DefaultArchiveRemover</code> to treat
 * individual files as units of history instead of time as the only unit of
 * history. This effectively allows users to limit the disk space used by
 * each log (via a size based FNATP such as SizeAndRandomizedTimeBasedFNATP)
 * while also limiting the total disk spaced used (via maxHistory and this
 * ArchiveRemover) by all log files.
 *
 * The lack of this feature is captured in LOGBACK-747 and a pull request to
 * modify the existing SizeAndTimeBasedArchiveRemover with this functionality
 * was submitted in https://github.com/qos-ch/logback/pull/287. Once the code
 * is incorporated into a release of Logback this class will be removed from
 * Logback-Steno.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 * @since 1.11.0
 */
public class CustomSizeAndTimeBasedArchiveRemover extends DefaultArchiveRemover {

    /**
     * Public constructor.
     *
     * @param fileNamePattern The <code>FileNamePattern</code> from the <code>TimeBasedRollingPolicy</code>.
     * @param rollingCalendar The <code>RollingCalendar</code> from the <code>SizeAndTimeBasedFNATP</code>.
     */
    public CustomSizeAndTimeBasedArchiveRemover(
            final FileNamePattern fileNamePattern,
            final RollingCalendar rollingCalendar) {
        super(fileNamePattern, rollingCalendar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanByPeriodOffset(final Date cleanFrom, final int periodOffset) {
        final Date cleanTo = this.rc.getRelativeDate(cleanFrom, periodOffset);
        final int maxFilesToRetain = -periodOffset - 1;

        int nextPeriodOffset = 0;
        int filesToRetain = maxFilesToRetain;
        Date dateOfPeriodToClean = this.rc.getRelativeDate(cleanFrom, nextPeriodOffset);
        while (dateOfPeriodToClean.after(cleanTo) || dateOfPeriodToClean.equals(cleanTo)) {
            // Find all the files for the period to clean
            final File parentDir = new File(this.fileNamePattern.convertMultipleArguments(dateOfPeriodToClean, 0))
                    .getAbsoluteFile()
                    .getParentFile();
            final String stemRegex = createStemRegex(dateOfPeriodToClean);
            final File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(
                    parentDir, stemRegex);

            // Sort the files to delete the oldest first (smallest last modified time)
            Arrays.sort(matchingFileArray, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

            // Delete files from this period if there are more than should be retained
            // NOTE: The first file deleted is always the oldest (smallest last modified date/time)
            for (int i = 0; i <= matchingFileArray.length - filesToRetain - 1; ++i) {
                final File file = matchingFileArray[i];
                addInfo("deleting " + file);
                if (!file.delete()) {
                    addWarn("failed to delete " + file);
                }
            }

            // Update remaining files to retain and move back a time period
            filesToRetain = Math.max(0, filesToRetain - matchingFileArray.length);
            dateOfPeriodToClean = this.rc.getRelativeDate(cleanFrom, --nextPeriodOffset);
        }
    }

    private String createStemRegex(final Date dateOfPeriodToClean) {
        final String regex = fileNamePattern.toRegexForFixedDate(dateOfPeriodToClean);
        return FileFilterUtil.afterLastSlash(regex);
    }
}
