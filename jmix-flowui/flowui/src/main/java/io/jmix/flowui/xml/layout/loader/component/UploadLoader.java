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
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.server.streams.UploadHandler;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.flowui.component.upload.JmixUpload;
import io.jmix.flowui.component.upload.handler.FileTemporaryStorageUploadHandler;
import io.jmix.flowui.component.upload.handler.InMemoryUploadHandler;
import io.jmix.flowui.component.upload.receiver.FileTemporaryStorageBuffer;
import io.jmix.flowui.component.upload.receiver.MultiFileTemporaryStorageBuffer;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.IconLoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.BeansException;

import java.util.Optional;

public class UploadLoader extends AbstractComponentLoader<JmixUpload> {

    protected IconLoaderSupport iconLoaderSupport;

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
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
    }

    @Deprecated(since = "2.8", forRemoval = true)
    protected void loadReceiver(JmixUpload component, Element element) {
        Optional<String> receiverFqn = loadString(element, "receiverFqn");

        if (receiverFqn.isPresent()) {
            loadReceiverFqn(component, receiverFqn.get());
        } else {
            loadReceiverType(component, element);
        }
    }

    @Deprecated(since = "2.8", forRemoval = true)
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
        Optional<String> type = loadString(element, "receiverType");
        if (type.isPresent()) {
            switch (type.get()) {
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
        } else {
            loadUploadHandler(resultComponent, element);
        }
    }

    protected void loadUploadHandler(JmixUpload component, Element element) {
        Optional<String> receiverFqn = loadString(element, "uploadHandlerFqn");
        if (receiverFqn.isPresent()) {
            loadUploadHandlerFqn(component, receiverFqn.get());
        } else {
            loadUploadHandlerType(component, element);
        }
    }

    protected void loadUploadHandlerFqn(JmixUpload component, String uploadHandlerFqn) {
        Class<?> clazz;

        try {
            clazz = ReflectionHelper.loadClass(uploadHandlerFqn);
        } catch (ClassNotFoundException e) {
            String message = String.format("Class not found for 'uploadHandlerFqn' attribute of '%s'",
                    component.getClass().getSimpleName());
            throw new GuiDevelopmentException(message, context);
        }

        if (!UploadHandler.class.isAssignableFrom(clazz)) {
            String message = String.format(
                    "UploadHandler for '%s' with id '%s' should implement the '%s' interface",
                    component.getClass().getSimpleName(),
                    component.getId().orElse("null"),
                    UploadHandler.class.getName()
            );

            throw new GuiDevelopmentException(message, context);
        }

        UploadHandler uploadHandler;

        try {
            uploadHandler = (UploadHandler) applicationContext.getBean(clazz);
        } catch (BeansException e) {
            uploadHandler = null;
        }

        if (uploadHandler == null) {
            try {
                uploadHandler = (UploadHandler) ReflectionHelper.newInstance(clazz);
            } catch (NoSuchMethodException e) {
                String message = String.format("Can't find constructor for '%s' class", clazz.getSimpleName());
                throw new GuiDevelopmentException(message, context);
            }
        }

        component.setUploadHandler(uploadHandler);
    }

    protected void loadUploadHandlerType(JmixUpload component, Element element) {
        String type = loadString(element, "uploadHandlerType")
                .orElse("IN_MEMORY");

        UploadHandler uploadHandler = switch (type) {
            case "FILE_TEMPORARY_STORAGE" -> {
                yield applicationContext.getBean(FileTemporaryStorageUploadHandler.class);
            }
            default -> {
                yield applicationContext.getBean(InMemoryUploadHandler.class);
            }
        };

        component.setUploadHandler(uploadHandler);
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

        iconLoaderSupport().loadIcon(element, "dropLabelIcon", component::setDropLabelIcon);
    }

    protected void loadUploadButton(JmixUpload component, Element element) {
        Optional<String> uploadText = loadResourceString(element, "uploadText", context.getMessageGroup());
        Optional<Component> uploadIcon = iconLoaderSupport().loadIcon(element, "uploadIcon");

        if (uploadText.isEmpty() && uploadIcon.isEmpty()) {
            return;
        }

        Button uploadButton = factory.create(Button.class);
        uploadIcon.ifPresent(uploadButton::setIcon);
        uploadText.ifPresent(uploadButton::setText);

        component.setUploadButton(uploadButton);
    }

    protected IconLoaderSupport iconLoaderSupport() {
        if (iconLoaderSupport == null) {
            iconLoaderSupport = applicationContext.getBean(IconLoaderSupport.class, context);
        }

        return iconLoaderSupport;
    }
}
