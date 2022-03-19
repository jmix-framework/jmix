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

import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;

public class AppWorkAreaLoader extends ContainerLoader<AppWorkArea> {

    protected ComponentLoader initialLayoutLoader;

    @Override
    public void createComponent() {
        resultComponent = createComponentInternal();
        loadId(resultComponent, element);

        Element initialLayoutElement = element.element("initialLayout");
        initialLayoutLoader = getLayoutLoader().getLoader(initialLayoutElement, VBoxLayout.NAME);
        initialLayoutLoader.createComponent();
        VBoxLayout initialLayout = (VBoxLayout) initialLayoutLoader.getResultComponent();
        resultComponent.setInitialLayout(initialLayout);
    }

    protected AppWorkArea createComponentInternal() {
        return factory.create(AppWorkArea.NAME);
    }

    @Override
    public void loadComponent() {
        loadId(resultComponent, element);
        assignFrame(resultComponent);

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadAlign(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        initialLayoutLoader.loadComponent();
    }
}