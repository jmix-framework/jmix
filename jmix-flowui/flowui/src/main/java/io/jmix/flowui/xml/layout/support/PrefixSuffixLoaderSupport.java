/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.xml.layout.support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

@org.springframework.stereotype.Component("flowui_PrefixSuffixLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PrefixSuffixLoaderSupport implements ApplicationContextAware {

    protected ComponentLoader.Context context;
    protected LayoutLoader layoutLoader;

    protected Collection<ComponentLoader<?>> pendingLoadComponents = new ArrayList<>();

    protected ApplicationContext applicationContext;

    public PrefixSuffixLoaderSupport(ComponentLoader.Context context) {
        this.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void createPrefixSuffixComponents(Component component, Element element) {
        if (component instanceof HasPrefix hasPrefixComponent) {
            initPrefixSuffixComponent(element, "prefix", hasPrefixComponent::setPrefixComponent);
        }

        if (component instanceof HasSuffix hasSuffixComponent) {
            initPrefixSuffixComponent(element, "suffix", hasSuffixComponent::setSuffixComponent);
        }
    }

    public void loadPrefixSuffixComponents() {
        pendingLoadComponents.forEach(ComponentLoader::loadComponent);
        pendingLoadComponents.clear();
    }

    protected void initPrefixSuffixComponent(Element parentElement, String subElementName, Consumer<Component> setter) {
        Element element = parentElement.element(subElementName);

        if (element == null) {
            return;
        }

        if (element.elements().size() != 1) {
            throw new GuiDevelopmentException(String.format("Only one %s component can be defined", subElementName),
                    context);
        }

        Element componentElement = element.elements().get(0);
        ComponentLoader<?> componentLoader = getLayoutLoader().createComponentLoader(componentElement);
        componentLoader.initComponent();

        setter.accept(componentLoader.getResultComponent());
        pendingLoadComponents.add(componentLoader);
    }

    protected LayoutLoader getLayoutLoader() {
        if (layoutLoader == null) {
            layoutLoader = applicationContext.getBean(LayoutLoader.class, context);
        }
        return layoutLoader;
    }
}
