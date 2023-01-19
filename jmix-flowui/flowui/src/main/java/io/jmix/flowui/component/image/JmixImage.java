/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.image;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.view.View;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

/**
 * Component supports data binding with entity properties.
 *
 * @param <V> type of value, e.g. {@link FileRef}, byte array
 */
public class JmixImage<V> extends Image implements SupportsValueSource<V>, HasThemeVariant<JmixImageVariant>,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected FileStorageLocator fileStorageLocator;

    protected ValueSource<V> valueSource;

    protected Registration valueSourceValueChangeRegistration;
    protected Registration valueSourceStateChangeRegistration;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
    }

    protected void autowireDependencies() {
        fileStorageLocator = applicationContext.getBean(FileStorageLocator.class);
    }

    @Nullable
    @Override
    public ValueSource<V> getValueSource() {
        return valueSource;
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        if (this.valueSource != valueSource) {
            unbind();

            this.valueSource = valueSource;

            if (valueSource == null) {
                setSrc(""); // set empty value
                return;
            }

            bind();
            updateSource();
        }
    }

    protected void bind() {
        if (valueSource == null) {
            return;
        }
        if (valueSource instanceof ApplicationContextAware) {
            ((ApplicationContextAware) valueSource).setApplicationContext(applicationContext);
        }
        valueSourceValueChangeRegistration = valueSource.addValueChangeListener(this::onValueSourceValueChange);
        valueSourceStateChangeRegistration = valueSource.addStateChangeListener(this::onValueSourceStateChange);
    }

    protected void unbind() {
        if (valueSourceValueChangeRegistration != null) {
            valueSourceValueChangeRegistration.remove();
            valueSourceValueChangeRegistration = null;
        }
        if (valueSourceStateChangeRegistration != null) {
            valueSourceStateChangeRegistration.remove();
            valueSourceStateChangeRegistration = null;
        }
    }

    protected void onValueSourceValueChange(ValueSource.ValueChangeEvent<V> event) {
        updateSource();
    }

    protected void onValueSourceStateChange(DataUnit.StateChangeEvent event) {
        updateSource();
    }

    protected void updateSource() {
        V value = valueSource.getValue();

        Object resource = createResource(value);
        if (resource instanceof String) {
            setSrc((String) resource);
        } else {
            setSrc((StreamResource) createResource(value));
        }
    }

    protected Object createResource(@Nullable V value) {
        if (value == null) {
            return new StreamResource(generateFileName(), InputStream::nullInputStream);
        }
        if (value instanceof byte[]) {
            return new StreamResource(generateFileName(), () -> new ByteArrayInputStream((byte[]) value));
        }
        if (value instanceof FileRef) {
            FileRef fileRef = (FileRef) value;
            return new StreamResource(((FileRef) value).getFileName(), () ->
                    fileStorageLocator.getByName(fileRef.getStorageName()).openStream(fileRef));
        }
        if (value instanceof String) {
            return value;
        }
        if (value instanceof URI) {
            URI uri = (URI) value;
            try {
                return uri.toURL().toString();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Cannot convert provided URI `" + uri + "' to URL", e);
            }
        }

        View<?> view = UiComponentUtils.findView(this);
        String frameId = view == null ? null : view.getId().orElse(null);

        throw new GuiDevelopmentException(
                String.format("The '%s' component does not support property value binding for the property of type: %s",
                        this.getClass().getName(), value.getClass().getName()), frameId);
    }

    protected String generateFileName() {
        return UUID.randomUUID().toString();
    }
}
