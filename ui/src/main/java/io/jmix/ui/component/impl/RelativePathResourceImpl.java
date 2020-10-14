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
import io.jmix.ui.component.RelativePathResource;
import io.jmix.ui.sys.ControllerUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component("ui_RelativePathResource")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RelativePathResourceImpl extends AbstractResource implements WebResource, RelativePathResource {

    protected String path;

    protected String mimeType;

    @Override
    public RelativePathResource setPath(String path) {
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
        try {
            URL context = new URL(ControllerUtils.getLocationWithoutParams());
            resource = new ExternalResource(new URL(context, path));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can't create RelativePathResource", e);
        }
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
