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
import com.haulmont.cuba.gui.export.ExportDisplay;
import io.jmix.core.FileStorageLocator;
import io.jmix.ui.export.ExportFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Allows to show exported data in web browser or download it.
 */
@Component(ExportDisplay.NAME)
@Scope("prototype")
public class WebExportDisplay extends io.jmix.ui.export.WebExportDisplay implements ExportDisplay {

    @Autowired
    protected CubaFileStorage cubaFileStorage;

    @Autowired
    @Override
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        super.setFileStorageLocator(fileStorageLocator);
        //use cuba file storage
        fileStorage = cubaFileStorage.asFileStorage();
    }

    @Override
    public void show(FileDescriptor fileDescriptor, @Nullable ExportFormat format) {
        super.show(fileDescriptor, format);
    }

    @Override
    public void show(FileDescriptor fileDescriptor) {
        super.show(fileDescriptor);
    }
}