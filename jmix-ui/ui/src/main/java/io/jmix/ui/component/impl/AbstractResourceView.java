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

import com.vaadin.shared.util.SharedUtil;
import com.vaadin.ui.AbstractEmbedded;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Resource;
import io.jmix.ui.component.ResourceView;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class AbstractResourceView<T extends AbstractEmbedded> extends AbstractComponent<T>
        implements ResourceView {

    protected Resource resource;

    protected Runnable resourceUpdateHandler;

    protected AbstractResourceView() {
        resourceUpdateHandler = () -> {
            com.vaadin.server.Resource vRes = this.resource == null ? null : ((AbstractResource) this.resource).getResource();
            component.setSource(vRes);
        };
    }

    @Nullable
    @Override
    public Resource getSource() {
        return resource;
    }

    @Override
    public void setSource(@Nullable Resource resource) {
        if (SharedUtil.equals(this.resource, resource)) {
            return;
        }
        updateValue(resource);
    }

    protected void updateValue(@Nullable Resource value) {
        Resource oldValue = this.resource;
        if (oldValue != null) {
            ((AbstractResource) oldValue).setResourceUpdatedHandler(null);
        }

        this.resource = value;

        com.vaadin.server.Resource vResource = null;
        if (value != null && ((WebResource) value).hasSource()) {
            vResource = ((WebResource) value).getResource();
        }
        component.setSource(vResource);

        if (value != null) {
            ((AbstractResource) value).setResourceUpdatedHandler(resourceUpdateHandler);
        }

        publish(SourceChangeEvent.class, new SourceChangeEvent(this, oldValue, this.resource));
    }

    @Override
    public <R extends Resource> R setSource(Class<R> type) {
        R resource = applicationContext.getBean(type);

        updateValue(resource);

        return resource;
    }

    @Override
    public void setAlternateText(@Nullable String alternateText) {
        component.setAlternateText(alternateText);
    }

    @Nullable
    @Override
    public String getAlternateText() {
        return component.getAlternateText();
    }

    @Override
    public Subscription addSourceChangeListener(Consumer<SourceChangeEvent> listener) {
        return getEventHub().subscribe(SourceChangeEvent.class, listener);
    }
}
