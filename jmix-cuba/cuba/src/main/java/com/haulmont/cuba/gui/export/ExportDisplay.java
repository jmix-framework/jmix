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
package com.haulmont.cuba.gui.export;

import com.haulmont.cuba.core.entity.FileDescriptor;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.Frame;
import io.jmix.ui.download.Downloader;

import javax.annotation.Nullable;

/**
 * Generic interface to show data exported from the system.
 *
 * @deprecated Use {@link Downloader} instead
 */
@Deprecated
public interface ExportDisplay {

    String NAME = "cuba_ExportDisplay";

    /**
     * Export an arbitrary resource defined by a ExportDataProvider.
     *
     * @param dataProvider resource provider
     * @param resourceName resource name
     * @param format       export format, can be null
     */
    void show(ExportDataProvider dataProvider, String resourceName, @Nullable ExportFormat format);

    /**
     * Export an arbitrary resource defined by a ExportDataProvider.
     *
     * @param dataProvider resource provider
     * @param resourceName resource name
     */
    void show(ExportDataProvider dataProvider, String resourceName);

    /**
     * Export a file from file storage.
     *
     * @param fileDescriptor file descriptor
     * @param format         export format, can be null
     */
    void show(FileDescriptor fileDescriptor, @Nullable ExportFormat format);

    /**
     * Export a file from file storage.
     *
     * @param fileDescriptor file descriptor
     */
    void show(FileDescriptor fileDescriptor);

    /**
     * @return true if export display should open a new window with the file content
     */
    boolean isShowNewWindow();

    /**
     * Sets explicit new window option.
     *
     * @param showNewWindow true if export display opens new window, false otherwise
     */
    void setShowNewWindow(boolean showNewWindow);

    /** INTERNAL. Don't call from application code. */
    @Internal
    void setFrame(@Nullable Frame frame);
}