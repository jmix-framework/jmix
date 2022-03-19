/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.SplitPanel;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import io.jmix.ui.xml.layout.loader.SplitPanelLoader;
import org.apache.commons.lang3.StringUtils;

public class CubaSplitPanelLoader extends SplitPanelLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(SplitPanel.NAME);
        loadId(resultComponent, element);

        String orientation = element.attributeValue("orientation");
        if (StringUtils.isEmpty(orientation)) {
            resultComponent.setOrientation(io.jmix.ui.component.SplitPanel.ORIENTATION_VERTICAL);
        } else if ("vertical".equalsIgnoreCase(orientation)) {
            resultComponent.setOrientation(io.jmix.ui.component.SplitPanel.ORIENTATION_VERTICAL);
        } else if ("horizontal".equalsIgnoreCase(orientation)) {
            resultComponent.setOrientation(io.jmix.ui.component.SplitPanel.ORIENTATION_HORIZONTAL);
        }

        createSubComponents(resultComponent, element);
        if (resultComponent.getOwnComponents().size() == 1) {
            resultComponent.add(uiComponents.create(VBoxLayout.NAME));
        }
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        ComponentLoaderHelper.loadSettingsEnabled((SplitPanel) resultComponent, element);
    }
}
