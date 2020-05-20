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

package io.jmix.ui.components.impl;

import com.vaadin.event.MouseEvents;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.Entity;
import io.jmix.core.entity.FileDescriptor;
import io.jmix.core.impl.BeanLocatorAware;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.components.FileDescriptorResource;
import io.jmix.ui.components.Image;
import io.jmix.ui.components.Resource;
import io.jmix.ui.components.StreamResource;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.components.data.meta.EntityValueSource;
import io.jmix.ui.widgets.JmixImage;
import org.springframework.beans.factory.InitializingBean;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

public class WebImage extends WebAbstractResourceView<JmixImage> implements Image, InitializingBean {
    protected static final String IMAGE_STYLENAME = "c-image";

    protected ValueSource<FileDescriptor> valueSource;

    protected Subscription valueChangeSubscription;
    protected Subscription instanceChangeSubscription;

    protected ScaleMode scaleMode = ScaleMode.NONE;

    protected MouseEvents.ClickListener vClickListener;

    public WebImage() {
        component = createComponent();
    }

    protected JmixImage createComponent() {
        return new JmixImage();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixImage component) {
        component.setPrimaryStyleName(IMAGE_STYLENAME);
    }

    @Override
    public void setValueSource(ValueSource<FileDescriptor> valueSource) {
        if (this.valueSource == valueSource) {
            return;
        }

        unbindValueSourceEvents();

        if (this.valueSource != null && valueSource == null) {
            component.setSource(null);
            this.valueSource = null;
            return;
        }

        this.valueSource = valueSource;

        bindValueSourceEvents();
        updateComponent();
    }

    protected void unbindValueSourceEvents() {
        if (valueChangeSubscription != null) {
            valueChangeSubscription.remove();
        }
        if (instanceChangeSubscription != null) {
            instanceChangeSubscription.remove();
        }
    }

    protected void bindValueSourceEvents() {
        if (valueSource == null) {
            return;
        }

        if (valueSource instanceof BeanLocatorAware) {
            ((BeanLocatorAware) valueSource).setBeanLocator(beanLocator);
        }

        valueChangeSubscription = valueSource.addValueChangeListener(event -> updateComponent());
        if (valueSource instanceof EntityValueSource) {
            instanceChangeSubscription = ((EntityValueSource<Entity, FileDescriptor>) valueSource)
                    .addInstanceChangeListener(event -> updateComponent());
        }
    }

    @Override
    public ValueSource<FileDescriptor> getValueSource() {
        return valueSource;
    }

    protected void updateComponent() {
        Object propertyValue = valueSource.getValue();
        Resource resource = createImageResource(propertyValue);

        updateValue(resource);
    }

    protected Resource createImageResource(final Object resourceObject) {
        if (resourceObject == null) {
            return null;
        }

        if (resourceObject instanceof FileDescriptor) {
            return createResource(FileDescriptorResource.class)
                    .setFileDescriptor((FileDescriptor) resourceObject);
        }

        if (resourceObject instanceof byte[]) {
            return createResource(StreamResource.class)
                    .setStreamSupplier(() ->
                            new ByteArrayInputStream((byte[]) resourceObject));
        }

        throw new GuiDevelopmentException(
                "The Image component supports only FileDescriptor and byte[] datasource property value binding",
                getFrame().getId());
    }

    @Override
    public ScaleMode getScaleMode() {
        return this.scaleMode;
    }

    @Override
    public void setScaleMode(ScaleMode scaleMode) {
        Preconditions.checkNotNullArgument(scaleMode);

        this.scaleMode = scaleMode;

        component.setScaleMode(scaleMode.name().toLowerCase().replace("_", "-"));
    }

    @Override
    public Subscription addClickListener(Consumer<ClickEvent> listener) {
        if (vClickListener == null) {
            vClickListener = e -> {
                ClickEvent event = new ClickEvent(WebImage.this, WebWrapperUtils.toMouseEventDetails(e));
                publish(ClickEvent.class, event);
            };
            component.addClickListener(vClickListener);
        }

        getEventHub().subscribe(ClickEvent.class, listener);

        return () -> removeClickListener(listener);
    }

    @Override
    public void removeClickListener(Consumer<ClickEvent> listener) {
        unsubscribe(ClickEvent.class, listener);

        if (!hasSubscriptions(ClickEvent.class)) {
            component.removeClickListener(vClickListener);
            vClickListener = null;
        }
    }
}
