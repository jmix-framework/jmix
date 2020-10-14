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

import com.vaadin.server.ExternalResource;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.UrlResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component("ui_UrlResource")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UrlResourceImpl extends AbstractResource implements WebResource, UrlResource {

    protected URL url;

    protected String mimeType;

    @Override
    public UrlResource setUrl(URL url) {
        Preconditions.checkNotNullArgument(url);

        this.url = url;
        hasSource = true;

        fireResourceUpdateEvent();

        return this;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    protected void createResource() {
        resource = new ExternalResource(url);

        ((ExternalResource) resource).setMIMEType(mimeType);
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;

        if (resource != null) {
            ((ExternalResource) resource).setMIMEType(mimeType);
        }
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
}