/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.xml.layout.loader;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.container.VerticalLayoutLoader;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import org.dom4j.Element;

public class WorkAreaLoader extends AbstractComponentLoader<WorkArea> {

    protected ComponentLoader<?> initialLayoutLoader;

    @Override
    protected WorkArea createComponent() {
        WorkArea workArea = factory.create(WorkArea.class);
        createInitialLayout(workArea, element);

        return workArea;
    }

    @Override
    public void loadComponent() {
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadInitialLayout();
    }

    protected void createInitialLayout(WorkArea workArea, Element element) {
        Element initialLayoutElement = element.element("initialLayout");
        if (initialLayoutElement == null) {
            return;
        }

        initialLayoutLoader = getLayoutLoader().getLoader(initialLayoutElement, VerticalLayoutLoader.class);
        initialLayoutLoader.initComponent();

        VerticalLayout initialLayout = (VerticalLayout) initialLayoutLoader.getResultComponent();
        workArea.setInitialLayout(initialLayout);
    }

    protected void loadInitialLayout() {
        if (initialLayoutLoader != null) {
            initialLayoutLoader.loadComponent();
        }
    }
}
