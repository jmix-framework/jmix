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
public class DynamicAttributesPanelLoader extends AbstractComponentLoader<DynamicAttributesPanel> {
    @SuppressWarnings("NullableProblems")
    @Override
    public DynamicAttributesPanel createComponent() {
        return factory.create(DynamicAttributesPanel.class);
    }

    @Override
    public void loadComponent() {
        loadDataContainer(resultComponent, element);
        loadFieldWidth(resultComponent, element);
    }


    protected void loadDataContainer(DynamicAttributesPanel resultComponent, Element element) {
        String containerId = element.attributeValue("dataContainer");
        if (Strings.isNullOrEmpty(containerId)) {
            throw new GuiDevelopmentException("DynamicAttributesPanel element doesn't have 'dataContainer' attribute",
                    context, "DynamicAttributesPanel ID", element.attributeValue("id"));
        }
        InstanceContainer<Object> container = getComponentContext().getViewData().getContainer(containerId);
        resultComponent.setInstanceContainer(container);
    }

    protected void loadFieldWidth(DynamicAttributesPanel resultComponent, Element element) {
        String fieldWidth = element.attributeValue("fieldWidth");
        if (!Strings.isNullOrEmpty(fieldWidth)) {
            resultComponent.setFieldWidth(fieldWidth);
        }
    }
}
