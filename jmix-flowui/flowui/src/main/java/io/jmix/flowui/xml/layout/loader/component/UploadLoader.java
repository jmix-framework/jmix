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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Splitter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.flowui.component.upload.JmixUpload;
import io.jmix.flowui.component.upload.receiver.FileTemporaryStorageBuffer;
import io.jmix.flowui.component.upload.receiver.MultiFileTemporaryStorageBuffer;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;
import org.springframework.beans.BeansException;

import java.util.Optional;
import java.util.function.Consumer;

public class UploadLoader extends AbstractComponentLoader<JmixUpload> {

    @Override
    protected JmixUpload createComponent() {
        return factory.create(JmixUpload.class);
    }

    @Override
    public void loadComponent() {
        loadAcceptedFileTypes(resultComponent, element);
        loadDropLabel(resultComponent, element);
        loadUploadButton(resultComponent, element);
        loadReceiver(resultComponent, element);

        loadBoolean(element, "autoUpload", resultComponent::setAutoUpload);
        loadBoolean(element, "dropAllowed", resultComponent::setDropAllowed);
        loadInteger(element, "maxFiles", resultComponent::setMaxFiles);
        loadInteger(element, "maxFileSize", resultComponent::setMaxFileSize);

        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
    }

    protected void loadReceiver(JmixUpload component, Element element) {
        String receiverFqn = loadString(element, "receiverFqn")
                .orElse(null);

        if (receiverFqn != null) {
            loadReceiverFqn(component, receiverFqn);
        } else {
            loadReceiverType(component, element);
        }
    }

    protected void loadReceiverFqn(JmixUpload component, String receiverFqn) {
        Class<?> clazz;

        try {
            clazz = ReflectionHelper.loadClass(receiverFqn);
        } catch (ClassNotFoundException e) {
            String message = String.format("Class not found for 'receiverFqn' attribute of '%s'",
                    component.getClass().getSimpleName());
            throw new GuiDevelopmentException(message, context);
        }

        if (!Receiver.class.isAssignableFrom(clazz)) {
            String message = String.format(
                    "Receiver for '%s' with id '%s' should implement '%s' interface",
                    component.getClass().getSimpleName(),
                    component.getId().orElse("null"),
                    Receiver.class.getSimpleName()
            );

            throw new GuiDevelopmentException(message, context);
        }

        Receiver receiver;

        try {
            receiver = (Receiver) applicationContext.getBean(clazz);
        } catch (BeansException e) {
            receiver = null;
        }

        if (receiver == null) {
            try {
                receiver = (Receiver) ReflectionHelper.newInstance(clazz);
            } catch (NoSuchMethodException e) {
                String message = String.format("Can't find constructor for '%s' class", clazz.getSimpleName());
                throw new GuiDevelopmentException(message, context);
            }
        }

        component.setReceiver(receiver);
    }

    protected void loadReceiverType(JmixUpload component, Element element) {
        String receiver = loadString(element, "receiverType")
                .orElse("MEMORY_BUFFER");

        switch (receiver) {
            case "MULTI_FILE_TEMPORARY_STORAGE_BUFFER":
                component.setReceiver(applicationContext.getBean(MultiFileTemporaryStorageBuffer.class));
                break;

            case "FILE_TEMPORARY_STORAGE_BUFFER":
                component.setReceiver(applicationContext.getBean(FileTemporaryStorageBuffer.class));
                break;

            case "MULTI_FILE_MEMORY_BUFFER":
                component.setReceiver(new MultiFileMemoryBuffer());
                break;

            default:
                component.setReceiver(new MemoryBuffer());
        }
    }

    protected void loadAcceptedFileTypes(JmixUpload component, Element element) {
        loadString(element, "acceptedFileTypes")
                .map(s -> Splitter.onPattern("[\\s,]+")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(s))
                .ifPresent(types -> component.setAcceptedFileTypes(types.toArray(new String[0])));
    }

    protected void loadDropLabel(JmixUpload component, Element element) {
        loadResourceString(element, "dropLabel", context.getMessageGroup(),
                message -> {
                    Span dropLabelComponent = factory.create(Span.class);
                    dropLabelComponent.setText(message);

                    component.setDropLabel(dropLabelComponent);
                });

        loadIcon(element, "dropLabelIcon", component::setDropLabelIcon);
    }

    protected void loadUploadButton(JmixUpload component, Element element) {
        Button uploadButton = factory.create(Button.class);

        Optional<String> uploadText = loadResourceString(element, "uploadText", context.getMessageGroup());
        Optional<Icon> uploadIcon = loadString(element, "uploadIcon").map(ComponentUtils::parseIcon);

        if (uploadText.isEmpty() && uploadIcon.isEmpty()) {
            return;
        }

        uploadIcon.ifPresent(uploadButton::setIcon);
        uploadText.ifPresent(uploadButton::setText);

        component.setUploadButton(uploadButton);
    }

    protected void loadIcon(Element element, String attributeName, Consumer<Component> iconSetter) {
        loadString(element, attributeName)
                .map(ComponentUtils::parseIcon)
                .ifPresent(iconSetter);
    }
}
