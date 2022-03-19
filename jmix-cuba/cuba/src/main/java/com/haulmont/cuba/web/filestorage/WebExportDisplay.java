/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.web.filestorage;

import com.haulmont.cuba.core.app.CubaFileStorage;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.export.ExportDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import io.jmix.ui.component.Frame;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Allows to show exported data in web browser or download it.
 */
@Component(ExportDisplay.NAME)
@Scope("prototype")
public class WebExportDisplay implements ExportDisplay {

    @Autowired
    protected CubaFileStorage cubaFileStorage;

    protected Downloader delegate;

    @Autowired
    public void setDownloader(Downloader downloader) {
        this.delegate = downloader;
        delegate.setFileStorage(cubaFileStorage.getDelegate());
    }

    @Override
    public void show(ExportDataProvider dataProvider, String resourceName, @Nullable ExportFormat format) {
        DownloadFormat downloadFormat = format == null ? null : format.getDownloadFormat();
        delegate.download(dataProvider, resourceName, downloadFormat);
    }

    @Override
    public void show(ExportDataProvider dataProvider, String resourceName) {
        delegate.download(dataProvider, resourceName);
    }

    @Override
    public void show(FileDescriptor fileDescriptor, @Nullable ExportFormat format) {
        DownloadFormat downloadFormat = format == null ? null : format.getDownloadFormat();
        delegate.download(cubaFileStorage.toFileRef(fileDescriptor), downloadFormat);
    }

    @Override
    public void show(FileDescriptor fileDescriptor) {
        delegate.download(cubaFileStorage.toFileRef(fileDescriptor));
    }

    @Override
    public boolean isShowNewWindow() {
        return delegate.isShowNewWindow();
    }

    @Override
    public void setShowNewWindow(boolean showNewWindow) {
        delegate.setShowNewWindow(showNewWindow);
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
    }
}