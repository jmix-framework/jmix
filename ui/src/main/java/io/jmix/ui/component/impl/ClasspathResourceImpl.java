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

import com.vaadin.server.StreamResource;
import io.jmix.core.Resources;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.ClasspathResource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("ui_ClasspathResource")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ClasspathResourceImpl extends AbstractStreamSettingsResource implements WebResource, ClasspathResource {

    protected Resources resources;

    protected String path;

    protected String mimeType;

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Override
    public ClasspathResource setPath(String path) {
        Preconditions.checkNotNullArgument(path);

        this.path = path;
        hasSource = true;

        fireResourceUpdateEvent();

        return this;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    protected void createResource() {
        StringBuilder name = new StringBuilder();

        String fullName = StringUtils.isNotEmpty(fileName) ? fileName : path;
        String baseName = FilenameUtils.getBaseName(fullName);

        if (StringUtils.isNotEmpty(baseName)) {
            name.append(baseName)
                    .append('-')
                    .append(UUID.randomUUID().toString())
                    .append('.')
                    .append(FilenameUtils.getExtension(fullName));
        } else {
            name.append(UUID.randomUUID().toString());
        }

        resource = new StreamResource(() ->
                resources.getResourceAsStream(path), name.toString());

        StreamResource streamResource = (StreamResource) this.resource;

        streamResource.setMIMEType(mimeType);
        streamResource.setCacheTime(cacheTime);
        streamResource.setBufferSize(bufferSize);
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;

        if (resource != null) {
            ((StreamResource) resource).setMIMEType(mimeType);
        }
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
}