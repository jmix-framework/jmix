/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.test_support;

import io.jmix.core.FileRef;
import io.jmix.flowui.download.DownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.reportsflowui.download.ReportDownloader;
import org.jspecify.annotations.Nullable;

public class TestReportDownloader implements ReportDownloader {

    protected int dataProviderDownloadCount;
    protected int fileRefDownloadCount;
    protected int byteArrayDownloadCount;
    protected DownloadDataProvider lastDataProvider;
    protected FileRef lastFileRef;
    protected byte[] lastBytes;
    protected String lastResourceName;
    protected DownloadFormat lastFormat;

    @Override
    public void download(DownloadDataProvider dataProvider, String resourceName, @Nullable DownloadFormat format) {
        dataProviderDownloadCount++;
        lastDataProvider = dataProvider;
        lastResourceName = resourceName;
        lastFormat = format;
    }

    @Override
    public void download(DownloadDataProvider dataProvider, String resourceName) {
        download(dataProvider, resourceName, null);
    }

    @Override
    public void download(FileRef fileReference) {
        download(fileReference, null);
    }

    @Override
    public void download(FileRef fileReference, @Nullable DownloadFormat format) {
        fileRefDownloadCount++;
        lastFileRef = fileReference;
        lastFormat = format;
    }

    @Override
    public void download(byte[] data, String resourceName) {
        download(data, resourceName, null);
    }

    @Override
    public void download(byte[] data, String resourceName, @Nullable DownloadFormat format) {
        byteArrayDownloadCount++;
        lastBytes = data;
        lastResourceName = resourceName;
        lastFormat = format;
    }

    public void reset() {
        dataProviderDownloadCount = 0;
        fileRefDownloadCount = 0;
        byteArrayDownloadCount = 0;
        lastDataProvider = null;
        lastFileRef = null;
        lastBytes = null;
        lastResourceName = null;
        lastFormat = null;
    }

    public int getByteArrayDownloadCount() {
        return byteArrayDownloadCount;
    }

    public int getDataProviderDownloadCount() {
        return dataProviderDownloadCount;
    }

    public int getFileRefDownloadCount() {
        return fileRefDownloadCount;
    }

    public byte[] getLastBytes() {
        return lastBytes;
    }

    public String getLastResourceName() {
        return lastResourceName;
    }

    public DownloadFormat getLastFormat() {
        return lastFormat;
    }
}
