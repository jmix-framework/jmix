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

import com.vaadin.event.MouseEvents;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.JmixEntity;
import org.springframework.context.ApplicationContextAware;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.FileStorageResource;
import io.jmix.ui.component.Image;
import io.jmix.ui.component.Resource;
import io.jmix.ui.component.StreamResource;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.widget.JmixImage;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

public class WebImage<T> extends WebAbstractResourceView<JmixImage> implements Image<T>, InitializingBean {
    protected static final String IMAGE_STYLENAME = "c-image";

    protected ValueSource<T> valueSource;

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
    public void setValueSource(@Nullable ValueSource<T> valueSource) {
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

        if (valueSource instanceof ApplicationContextAware) {
            ((ApplicationContextAware) valueSource).setApplicationContext(applicationContext);
        }

        valueChangeSubscription = valueSource.addValueChangeListener(event -> updateComponent());
        if (valueSource instanceof EntityValueSource) {
            instanceChangeSubscription = ((EntityValueSource<JmixEntity, T>) valueSource)
                    .addInstanceChangeListener(event -> updateComponent());
        }
    }

    @Nullable
    @Override
    public ValueSource<T> getValueSource() {
        return valueSource;
    }

    protected void updateComponent() {
        Object propertyValue = valueSource.getValue();
        Resource resource = createImageResource(propertyValue);

        updateValue(resource);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Resource createImageResource(@Nullable final Object resourceObject) {
        if (resourceObject == null) {
            return null;
        }

        if (resourceObject instanceof byte[]) {
            return applicationContext.getBean(StreamResource.class)
                    .setStreamSupplier(() ->
                            new ByteArrayInputStream((byte[]) resourceObject));
        }
        if (isFileReference(resourceObject)) {
            return applicationContext.getBean(FileStorageResource.class)
                    .setFileReference(resourceObject);
        }

        throw new GuiDevelopmentException(
                "The Image component does not support property value binding for the property of type: "
                        + resourceObject.getClass().getName(), getFrame().getId());
    }

    protected boolean isFileReference(final Object resourceObject) {
        FileStorage<?, ?> defaultFileStorage = applicationContext.getBean(FileStorageLocator.class).getDefault();
        return defaultFileStorage.getReferenceType().isAssignableFrom(resourceObject.getClass());
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
