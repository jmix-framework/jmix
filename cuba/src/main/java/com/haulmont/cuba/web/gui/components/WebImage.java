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
import io.jmix.core.FileRef;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.FileStorageResource;
import io.jmix.ui.component.Resource;
import io.jmix.ui.component.StreamResource;
import io.jmix.ui.component.impl.FileStorageResourceImpl;
import io.jmix.ui.component.impl.ImageImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

@Deprecated
public class WebImage extends ImageImpl<FileDescriptor> implements Image {

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
        if (resourceObject == null) {
            return null;
        }

        if (resourceObject instanceof byte[]) {
            return applicationContext.getBean(StreamResource.class)
                    .setStreamSupplier(() ->
                            new ByteArrayInputStream((byte[]) resourceObject));
        }
        if (resourceObject instanceof FileDescriptor) {
            return applicationContext.getBean(FileStorageResource.class)
                    .setFileReference(cubaFileStorage.toFileRef((FileDescriptor) resourceObject));
        }

        throw new GuiDevelopmentException(
                "The Image component does not support property value binding for the property of type: "
                        + resourceObject.getClass().getName(), getFrame().getId());
    }

    @Override
    public <R extends Resource> R createResource(Class<R> type) {
        return applicationContext.getBean(type);
    }

    @Override
    public void removeSourceChangeListener(Consumer<SourceChangeEvent> listener) {
        unsubscribe(SourceChangeEvent.class, listener);
    }
}
