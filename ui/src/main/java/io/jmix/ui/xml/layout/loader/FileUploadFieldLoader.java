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
package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.FileUploadField;
import io.jmix.ui.component.SingleFileUploadField;
import io.jmix.ui.component.UploadField;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.HashSet;

public class FileUploadFieldLoader<T extends Field & SingleFileUploadField>
        extends AbstractFieldLoader<T> {

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadData(resultComponent, element);
        loadEnable(resultComponent, element);
        loadEditable(resultComponent, element);
        loadRequired(resultComponent, element);
        loadVisible(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadAlign(resultComponent, element);
        loadCss(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);
        loadIcon(resultComponent, element);

        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadTabIndex(resultComponent, element);

        loadAccept(resultComponent, element);

        loadPermittedExtensions(resultComponent, element);

        loadDropZone(resultComponent, element);
        loadPasteZone(resultComponent, element);

        loadFileSizeLimit();

        loadShowFileName(resultComponent, element);

        loadClearButton(resultComponent, element);
        loadUploadButton(resultComponent, element);

        loadBuffered(resultComponent, element);
    }

    protected void loadFileSizeLimit() {
        String fileSizeLimit = element.attributeValue("fileSizeLimit");
        if (StringUtils.isNotEmpty(fileSizeLimit)) {
            resultComponent.setFileSizeLimit(Long.parseLong(fileSizeLimit));
        }
    }

    protected void loadUploadButton(SingleFileUploadField resultComponent, Element element) {
        String uploadButtonCaption = element.attributeValue("uploadButtonCaption");
        if (uploadButtonCaption != null) {
            resultComponent.setUploadButtonCaption(loadResourceString(uploadButtonCaption));
        }

        String uploadButtonIcon = element.attributeValue("uploadButtonIcon");
        if (StringUtils.isNotEmpty(uploadButtonIcon)) {
            resultComponent.setUploadButtonIcon(getIconPath(uploadButtonIcon));
        }

        String uploadButtonDescription = element.attributeValue("uploadButtonDescription");
        if (uploadButtonDescription != null) {
            resultComponent.setUploadButtonDescription(loadResourceString(uploadButtonDescription));
        }
    }

    protected void loadClearButton(SingleFileUploadField resultComponent, Element element) {
        String showClearButton = element.attributeValue("showClearButton");
        if (StringUtils.isNotEmpty(showClearButton)) {
            resultComponent.setShowClearButton(Boolean.parseBoolean(showClearButton));
        }

        String clearButtonCaption = element.attributeValue("clearButtonCaption");
        if (clearButtonCaption != null) {
            resultComponent.setClearButtonCaption(loadResourceString(clearButtonCaption));
        }

        String clearButtonIcon = element.attributeValue("clearButtonIcon");
        if (StringUtils.isNotEmpty(clearButtonIcon)) {
            resultComponent.setClearButtonIcon(getIconPath(clearButtonIcon));
        }

        String clearButtonDescription = element.attributeValue("clearButtonDescription");
        if (clearButtonDescription != null) {
            resultComponent.setClearButtonDescription(loadResourceString(clearButtonDescription));
        }
    }

    protected void loadShowFileName(SingleFileUploadField resultComponent, Element element) {
        String showFileName = element.attributeValue("showFileName");
        if (StringUtils.isNotEmpty(showFileName)) {
            resultComponent.setShowFileName(Boolean.parseBoolean(showFileName));
        }
    }

    @Override
    public void createComponent() {
        resultComponent = factory.create(FileUploadField.NAME);
        loadId(resultComponent, element);
    }

    protected void loadAccept(UploadField uploadField, Element element) {
        String accept = element.attributeValue("accept");
        if (StringUtils.isNotEmpty(accept)) {
            uploadField.setAccept(accept);
        }
    }

    protected void loadPermittedExtensions(UploadField uploadField, Element element) {
        String permittedExtensions = element.attributeValue("permittedExtensions");
        if (StringUtils.isNotEmpty(permittedExtensions)) {
            uploadField.setPermittedExtensions(new HashSet<>(Arrays.asList(permittedExtensions.split("\\s*,\\s*"))));
        }
    }

    protected void loadDropZone(UploadField uploadField, Element element) {
        String dropZoneId = element.attributeValue("dropZone");
        if (StringUtils.isNotEmpty(dropZoneId)) {
            Component dropZone = findComponent(dropZoneId);
            if (dropZone instanceof BoxLayout) {
                uploadField.setDropZone(new UploadField.DropZone((BoxLayout) dropZone));
            } else if (dropZone != null) {
                throw new GuiDevelopmentException("Unsupported dropZone class " + dropZone.getClass().getName(),
                        context);
            } else {
                throw new GuiDevelopmentException("Unable to find dropZone component with id: " + dropZoneId, context);
            }
        }

        String dropZonePrompt = element.attributeValue("dropZonePrompt");
        if (StringUtils.isNotEmpty(dropZonePrompt)) {
            uploadField.setDropZonePrompt(loadResourceString(dropZonePrompt));
        }
    }

    protected void loadPasteZone(UploadField uploadField, Element element) {
        String pasteZoneId = element.attributeValue("pasteZone");
        if (StringUtils.isNotEmpty(pasteZoneId)) {
            Component pasteZone = findComponent(pasteZoneId);
            if (pasteZone instanceof ComponentContainer) {
                uploadField.setPasteZone((ComponentContainer) pasteZone);
            } else if (pasteZone != null) {
                throw new GuiDevelopmentException("Unsupported pasteZone class " + pasteZone.getClass().getName(),
                        context);
            } else {
                throw new GuiDevelopmentException("Unable to find pasteZone component with id: " + pasteZoneId, context);
            }
        }
    }
}
