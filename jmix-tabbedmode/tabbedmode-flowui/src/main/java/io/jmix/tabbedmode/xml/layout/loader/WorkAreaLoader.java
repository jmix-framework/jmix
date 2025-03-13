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
import io.jmix.tabbedmode.component.tabsheet.JmixMainTabSheet;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import io.jmix.tabbedmode.component.workarea.WorkAreaSupport;
import org.dom4j.Element;

public class WorkAreaLoader extends AbstractComponentLoader<WorkArea> {

    public static final String TAG = "workArea";

    protected ComponentLoader<?> tabbedViewsContainerLoader;
    protected ComponentLoader<?> initialLayoutLoader;

    @Override
    protected WorkArea createComponent() {
        WorkArea workArea = factory.create(WorkArea.class);
        createTabbedViewsContainer(workArea, element);
        createInitialLayout(workArea, element);

        return workArea;
    }

    @Override
    public void loadComponent() {
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadTabbedViewsContainer();
        loadInitialLayout();
    }

    protected void createTabbedViewsContainer(WorkArea workArea, Element element) {
        TabbedViewsContainer<?> tabbedContainer;

        Element tabbedViewsContainerElement = element.element("tabbedContainer");
        if (tabbedViewsContainerElement != null) {
            tabbedViewsContainerLoader = getLayoutLoader().createComponentLoader(tabbedViewsContainerElement);
            tabbedViewsContainerLoader.initComponent();

            tabbedContainer = ((TabbedViewsContainer<?>) tabbedViewsContainerLoader.getResultComponent());
        } else {
            tabbedContainer = createDefaultTabbedViewsContainer();
        }

        workArea.setTabbedViewsContainer(tabbedContainer);
    }

    /**
     * For compatibility only.
     */
    @Deprecated(since = "2.6", forRemoval = true)
    protected TabbedViewsContainer<?> createDefaultTabbedViewsContainer() {
        JmixMainTabSheet tabSheet = factory.create(JmixMainTabSheet.class);
        tabSheet.setSizeFull();
        tabSheet.setClassName("jmix-main-tabsheet");

        WorkAreaSupport workAreaSupport = applicationContext.getBean(WorkAreaSupport.class);
        workAreaSupport.getDefaultActions()
                .forEach(tabSheet::addAction);

        return tabSheet;
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

    protected void loadTabbedViewsContainer() {
        if (tabbedViewsContainerLoader != null) {
            tabbedViewsContainerLoader.loadComponent();
        }
    }

    protected void loadInitialLayout() {
        if (initialLayoutLoader != null) {
            initialLayoutLoader.loadComponent();
        }
    }
}
