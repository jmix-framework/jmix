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

package io.jmix.flowui.xml.layout.loader.container;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import io.jmix.flowui.xml.layout.support.PrefixSuffixLoaderSupport;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

import static io.jmix.flowui.component.tabsheet.TabSheetUtils.applySettingsToTabContent;
import static io.jmix.flowui.component.tabsheet.TabSheetUtils.updateTabContent;

public class TabSheetLoader extends AbstractTabsLoader<JmixTabSheet> {

    protected PrefixSuffixLoaderSupport prefixSuffixLoaderSupport;
    protected Map<Tab, LazyTabLoader> lazyTabs = new HashMap<>();

    @Override
    protected JmixTabSheet createComponent() {
        return factory.create(JmixTabSheet.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createTabs(element);
        getPrefixSuffixLoaderSupport().createPrefixSuffixComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        getPrefixSuffixLoaderSupport().loadPrefixSuffixComponents();
    }

    protected void createTabs(Element element) {
        LayoutLoader loader = getLayoutLoader();

        boolean firstTab = true;
        for (Element subElement : element.elements("tab")) {
            boolean shouldBeLazy = loadBoolean(subElement, "lazy").orElse(false) && !firstTab;

            //LazyTabLoader skips loading subcomponents and initialises them after lazy tab activation
            ComponentLoader<?> componentLoader = loader.getLoader(subElement,
                    shouldBeLazy ? LazyTabLoader.class : TabLoader.class);

            componentLoader.initComponent();
            if (shouldBeLazy) {
                lazyTabs.put((Tab) componentLoader.getResultComponent(), (LazyTabLoader) componentLoader);
            }
            pendingLoadComponents.add(componentLoader);

            firstTab = false;
        }

        if (!lazyTabs.isEmpty()) {
            resultComponent.addSelectedChangeListener(this::selectedTabChangeHandler);
        }
    }

    protected void selectedTabChangeHandler(JmixTabSheet.SelectedChangeEvent selectedChangeEvent) {
        LazyTabLoader loader = lazyTabs.remove(selectedChangeEvent.getSelectedTab());
        if (loader != null) {
            loader.forceCreateSubComponents();
            loader.loadSubComponents();

            updateTabContent(
                    selectedChangeEvent.getSource(),
                    selectedChangeEvent.getSelectedTab(),
                    loader.getContent());

            applySettingsToTabContent(
                    getResultComponent(),
                    selectedChangeEvent.getSelectedTab());
        }
        if (lazyTabs.isEmpty()) {
            selectedChangeEvent.unregisterListener();
        }
    }

    @Override
    protected void loadSubComponents() {
        for (ComponentLoader<?> componentLoader : pendingLoadComponents) {
            componentLoader.loadComponent();
            Tab tab = (Tab) componentLoader.getResultComponent();
            Component content = ((TabLoader) componentLoader).getContent();

            resultComponent.add(tab, content);
        }

        pendingLoadComponents.clear();
    }

    public static class TabLoader extends AbstractTabLoader {

        protected Component content;

        @Override
        protected Tab createComponent() {
            return factory.create(Tab.class);
        }

        @Override
        public void initComponent() {
            super.initComponent();
            if (resultComponent.getId().isEmpty()) {
                String message = String.format("ID attribute is required for %s",
                        resultComponent.getClass().getSimpleName());
                throw new GuiDevelopmentException(message, context);
            }

            createSubComponents(resultComponent, element);
        }

        @Override
        protected void createSubComponents(HasComponents container, Element containerElement) {
            if (containerElement.elements().size() != 1 &&
                    (containerElement.elements().size() != 2 ||
                            containerElement.element("tooltip") == null)) {
                String message = String.format("%s with '%s' ID should have a single child component",
                        resultComponent.getClass().getSimpleName(), resultComponent.getId().orElse(null));

                throw new GuiDevelopmentException(message, context);
            }

            LayoutLoader loader = getLayoutLoader();

            for (Element subElement : containerElement.elements()) {
                if (!isChildElementIgnored(subElement)) {
                    ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
                    componentLoader.initComponent();
                    pendingLoadComponents.add(componentLoader);

                    content = componentLoader.getResultComponent();
                }
            }
        }

        public Component getContent() {
            return content;
        }
    }

    public static class LazyTabLoader extends TabLoader {

        @Override
        public Component getContent() {
            return content != null ? content : factory.create(Div.class);
        }

        public void forceCreateSubComponents() {
            super.createSubComponents(resultComponent, element);
        }

        @Override
        protected void createSubComponents(HasComponents container, Element containerElement) {
            //Skip subcomponents creating while tab isn't active
        }
    }

    protected PrefixSuffixLoaderSupport getPrefixSuffixLoaderSupport() {
        if (prefixSuffixLoaderSupport == null) {
            prefixSuffixLoaderSupport = applicationContext.getBean(PrefixSuffixLoaderSupport.class, context);
        }
        return prefixSuffixLoaderSupport;
    }
}
