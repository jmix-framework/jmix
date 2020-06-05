/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.core.app.CubaFileStorage;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.components.Image;
import io.jmix.ui.component.Resource;
import io.jmix.ui.component.impl.WebFileStorageResource;
import org.springframework.beans.factory.annotation.Autowired;
import io.jmix.core.common.event.Subscription;

import java.util.function.Consumer;

@Deprecated
public class WebImage extends io.jmix.ui.component.impl.WebImage<FileDescriptor> implements Image {

    protected CubaFileStorage cubaFileStorage;

    @Autowired
    public void setCubaFileStorage(CubaFileStorage cubaFileStorage) {
        this.cubaFileStorage = cubaFileStorage;
    }

    @Override
    public FileDescriptor getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(FileDescriptor value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<FileDescriptor>> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Resource createImageResource(Object resourceObject) {
        Resource resource = super.createImageResource(resourceObject);
        if (resource instanceof WebFileStorageResource) {
            ((WebFileStorageResource) resource).setFileStorage(cubaFileStorage.asFileStorage());
        }
        return resource;
    }

    @Override
    protected boolean isFileReference(Object resourceObject) {
        return FileDescriptor.class.isAssignableFrom(resourceObject.getClass());
    }
}
