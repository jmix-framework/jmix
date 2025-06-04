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

package io.jmix.reports.download;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.flowui.download.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component("report_Downloader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportDownloader implements Downloader {
    @Autowired
    @Qualifier("flowui_Downloader")
    protected Downloader downloader;

    protected ReportDownloaderUiProperties reportDownloaderUiProperties;

    @Autowired
    public void setReportDownloaderUiProperties(ReportDownloaderUiProperties reportDownloaderUiProperties) {
        this.reportDownloaderUiProperties = reportDownloaderUiProperties;
        downloader.
    }

    public ReportDownloaderUiProperties getReportDownloaderUiProperties() {
        return reportDownloaderUiProperties;
    }

    @Override
    public void setFileStorage(FileStorage fileStorage) {
        downloader.setFileStorage(fileStorage);
    }

    @Override
    public boolean isShowNewWindow() {
        return downloader.isShowNewWindow();
    }

    @Override
    public void setShowNewWindow(boolean showNewWindow) {
        downloader.setShowNewWindow(showNewWindow);
    }

    // uiExtentions using

//    public void download(DownloadDataProvider dataProvider, String resourceName, DownloadFormat downloadFormat)
    /**
     * Show/Download resource at client side
     *
     * @param dataProvider   DownloadDataProvider
     * @param resourceName   ResourceName for client side
     * @param downloadFormat DownloadFormat
     * @see FileRefDownloadDataProvider
     * @see ByteArrayDownloadDataProvider
     */
    @Override
    public void download(DownloadDataProvider dataProvider, String resourceName, @Nullable DownloadFormat downloadFormat) {
        downloader.download(dataProvider, resourceName, downloadFormat);
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
    public void download(FileRef fileReference, DownloadFormat format) {
        downloader.download(fileReference, format);
    }

    @Override
    public void download(byte[] data, String resourceName) {
        downloader.download(data, resourceName);
    }

    @Override
    public void download(byte[] data, String resourceName, DownloadFormat format) {
        downloader.download(data, resourceName, format);
    }
}
