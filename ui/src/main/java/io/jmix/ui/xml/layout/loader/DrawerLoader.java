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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.component.mainwindow.Drawer;

public class DrawerLoader extends ContainerLoader<Drawer> {


    @Override
    public void createComponent() {
        resultComponent = factory.create(Drawer.NAME);
        loadId(resultComponent, element);
        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadStyleName(resultComponent, element);
        loadCss(resultComponent, element);
        loadBoolean(element, "expandOnHover", resultComponent::setExpandOnHover);

        loadSubComponents();
    }
}
