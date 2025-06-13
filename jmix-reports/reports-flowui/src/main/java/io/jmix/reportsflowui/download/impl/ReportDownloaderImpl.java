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

package io.jmix.reportsflowui.download.impl;

import io.jmix.core.FileRef;
import io.jmix.flowui.download.DownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.reports.ReportsProperties;
import io.jmix.reportsflowui.download.ReportDownloader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Shows report data in the web browser or downloads it.
 */
@Component("report_ReportDownloader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportDownloaderImpl implements ReportDownloader {
    protected final Downloader downloader;
    protected final ReportsProperties reportsProperties;

    @Autowired
    public ReportDownloaderImpl(Downloader downloader, ReportsProperties reportsProperties) {
        this.downloader = downloader;
        this.reportsProperties = reportsProperties;

        configureDownloader();
    }

    protected void configureDownloader() {
        downloader.setViewFilePredicate((fileExtension) -> {
            if (StringUtils.isEmpty(fileExtension)) {
                return false;
            }

            return reportsProperties.getViewFileExtensions().contains(StringUtils.lowerCase(fileExtension));
        });
    }

    @Override
    public void download(DownloadDataProvider dataProvider, String resourceName, @Nullable DownloadFormat format) {
        downloader.download(dataProvider, resourceName, format);
    }

    @Override
    public void download(DownloadDataProvider dataProvider, String resourceName) {
        downloader.download(dataProvider, resourceName);
    }

    @Override
    public void download(FileRef fileReference) {
        downloader.download(fileReference);
    }

    @Override
    public void download(FileRef fileReference, @Nullable DownloadFormat format) {
        downloader.download(fileReference, format);
    }

    @Override
    public void download(byte[] data, String resourceName) {
        downloader.download(data, resourceName);
    }

    @Override
    public void download(byte[] data, String resourceName, @Nullable DownloadFormat format) {
        downloader.download(data, resourceName, format);
    }
}
