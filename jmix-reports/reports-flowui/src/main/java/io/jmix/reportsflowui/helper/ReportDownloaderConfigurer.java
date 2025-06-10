/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reportsflowui.helper;

import io.jmix.flowui.download.Downloader;
import io.jmix.reports.ReportsProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("report_ReportDownloaderConfigurer")
public class ReportDownloaderConfigurer {
    @Autowired
    protected ReportsProperties reportsProperties;

    public void configureDownloader(Downloader downloader) {
        downloader.setViewFilePredicate((fileExtension) -> {
            if (StringUtils.isEmpty(fileExtension)) {
                return false;
            }

            return reportsProperties.getViewFileExtensions().contains(StringUtils.lowerCase(fileExtension));
        });
    }
}
