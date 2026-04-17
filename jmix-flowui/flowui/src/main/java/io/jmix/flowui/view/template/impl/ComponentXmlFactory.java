/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view.template.impl;

import io.jmix.core.FileRef;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.entitypicker.EntityOpenCompositionAction;
import jakarta.persistence.Lob;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.UUID;

@Component("flowui_ComponentXmlFactory")
public class ComponentXmlFactory {

    public String createComponentXml(MetaProperty metaProperty, @Nullable String dataContainerId) {
        Range range = metaProperty.getRange();

        if (range.getCardinality().isMany()) {
            throw new IllegalArgumentException(
                    "Collection-valued property '%s' is not supported".formatted(metaProperty.getName()));
        }

        if (metaProperty.getType() == MetaProperty.Type.EMBEDDED) {
            return "";
        }

        Element element = createElement(metaProperty);
        initDataBinding(element, metaProperty, dataContainerId);

        if (range.isClass()) {
            addEntityPickerActions(element, metaProperty);
        }

        return element.asXML();
    }

    protected Element createElement(MetaProperty metaProperty) {
        Range range = metaProperty.getRange();

        if (range.isDatatype()) {
            return DocumentHelper.createElement(getDatatypeComponentName(metaProperty));
        } else if (range.isClass()) {
            return DocumentHelper.createElement("entityPicker");
        } else if (range.isEnum()) {
            return DocumentHelper.createElement("select");
        }

        return DocumentHelper.createElement("textField");
    }

    protected String getDatatypeComponentName(MetaProperty metaProperty) {
        Class<?> type = metaProperty.getRange().asDatatype().getJavaClass();

        if (type.equals(String.class) || type.equals(UUID.class)) {
            return isLob(metaProperty) ? "textArea" : "textField";
        } else if (type.equals(Boolean.class)) {
            return "checkbox";
        } else if (type.equals(java.sql.Date.class) || type.equals(LocalDate.class)) {
            return "datePicker";
        } else if (type.equals(Time.class) || type.equals(LocalTime.class) || type.equals(OffsetTime.class)) {
            return "timePicker";
        } else if (type.equals(Date.class) || type.equals(LocalDateTime.class) || type.equals(OffsetDateTime.class)) {
            return "dateTimePicker";
        } else if (Number.class.isAssignableFrom(type)) {
            return "textField";
        } else if (type.equals(FileRef.class)) {
            return "fileStorageUploadField";
        } else if (type.equals(byte[].class)) {
            return "fileUploadField";
        }

        return isLob(metaProperty) ? "textArea" : "textField";
    }

    protected boolean isLob(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().getAnnotation(Lob.class) != null;
    }

    protected void initDataBinding(Element element, MetaProperty metaProperty, @Nullable String dataContainerId) {
        element.addAttribute("id", metaProperty.getName() + "Field");
        element.addAttribute("property", metaProperty.getName());

        if (dataContainerId != null) {
            element.addAttribute("dataContainer", dataContainerId);
        }

        if ("fileStorageUploadField".equals(element.getName()) || "fileUploadField".equals(element.getName())) {
            element.addAttribute("fileNameVisible", Boolean.TRUE.toString());
            element.addAttribute("clearButtonVisible", Boolean.TRUE.toString());
        }
    }

    protected void addEntityPickerActions(Element element, MetaProperty metaProperty) {
        if (metaProperty.getType() == MetaProperty.Type.ASSOCIATION) {
            Element actions = element.addElement("actions");
            addAction(actions, "entityLookup", EntityLookupAction.ID);
            addAction(actions, "entityClear", EntityClearAction.ID);
        } else if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
            Element actions = element.addElement("actions");
            addAction(actions, "entityOpenComposition", EntityOpenCompositionAction.ID);
            addAction(actions, "entityClear", EntityClearAction.ID);
        }
    }

    protected void addAction(Element actions, String id, String type) {
        actions.addElement("action")
                .addAttribute("id", id)
                .addAttribute("type", type);
    }
}
