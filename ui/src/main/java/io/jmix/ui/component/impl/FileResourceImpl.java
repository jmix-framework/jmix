/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.FileResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

@Component("ui_FileResource")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileResourceImpl extends AbstractStreamSettingsResource implements WebResource, FileResource {

    protected File file;

    @Override
    public FileResource setFile(File file) {
        Preconditions.checkNotNullArgument(file);

        this.file = file;
        hasSource = true;

        fireResourceUpdateEvent();

        return this;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    protected void createResource() {
        resource = new com.vaadin.server.FileResource(file);

        com.vaadin.server.FileResource vFileResource = (com.vaadin.server.FileResource) this.resource;

        vFileResource.setCacheTime(cacheTime);
        vFileResource.setBufferSize(bufferSize);
    }
}
