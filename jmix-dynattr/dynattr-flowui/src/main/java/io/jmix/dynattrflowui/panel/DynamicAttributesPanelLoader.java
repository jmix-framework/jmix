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

package io.jmix.dynattrflowui.panel;

import com.google.common.base.Strings;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

@Internal
public class DynamicAttributesPanelLoader extends AbstractComponentLoader<DynamicAttributesPanelImpl> {
    @SuppressWarnings("NullableProblems")
    @Override
    public DynamicAttributesPanelImpl createComponent() {
        return factory.create(DynamicAttributesPanelImpl.class);
    }

    @Override
    public void loadComponent() {
        loadDataContainer(resultComponent, element);
        loadColumnsCount(resultComponent, element);
        loadRowsCount(resultComponent, element);
        loadFieldWidth(resultComponent, element);
        loadFieldCaptionWidth(resultComponent, element);
    }


    protected void loadDataContainer(DynamicAttributesPanelImpl resultComponent, Element element) {
        String containerId = element.attributeValue("dataContainer");
        if (Strings.isNullOrEmpty(containerId)) {
            throw new GuiDevelopmentException("DynamicAttributesPanel element doesn't have 'dataContainer' attribute",
                    context, "DynamicAttributesPanel ID", element.attributeValue("id"));
        }
        InstanceContainer<Object> container = getComponentContext().getViewData().getContainer(containerId);
        resultComponent.setInstanceContainer(container);
    }

    protected void loadColumnsCount(DynamicAttributesPanelImpl resultComponent, Element element) {
        resultComponent.setColumnsCount(getIntegerAttribute("cols", element));
    }

    protected void loadRowsCount(DynamicAttributesPanelImpl resultComponent, Element element) {
        resultComponent.setRowsCount(getIntegerAttribute("rows", element));
    }

    protected void loadFieldWidth(DynamicAttributesPanelImpl resultComponent, Element element) {
        String fieldWidth = element.attributeValue("fieldWidth");
        if (!Strings.isNullOrEmpty(fieldWidth)) {
            resultComponent.setFieldWidth(fieldWidth);
        }
    }

    protected void loadFieldCaptionWidth(DynamicAttributesPanelImpl resultComponent, Element element) {
        String fieldWidth = element.attributeValue("fieldCaptionWidth");
        if (!Strings.isNullOrEmpty(fieldWidth)) {
            resultComponent.setFieldCaptionWidth(fieldWidth);
        }
    }

    protected Integer getIntegerAttribute(String attributeName, Element element) {
        String columnsCountStr = element.attributeValue(attributeName);
        if (!Strings.isNullOrEmpty(columnsCountStr)) {
            return Integer.parseInt(columnsCountStr);
        }
        return null;
    }
}
