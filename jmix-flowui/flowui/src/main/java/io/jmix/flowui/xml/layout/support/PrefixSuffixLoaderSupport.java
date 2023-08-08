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

@org.springframework.stereotype.Component("flowui_PrefixSuffixLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PrefixSuffixLoaderSupport implements ApplicationContextAware {

    protected ComponentLoader.Context context;
    protected LayoutLoader layoutLoader;

    protected ComponentLoader<?> pendingLoadPrefixComponent;
    protected ComponentLoader<?> pendingLoadSuffixComponent;

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
            createPrefixComponent(hasPrefixComponent, element);
        }

        if (component instanceof HasSuffix hasSuffixComponent) {
            createSuffixComponent(hasSuffixComponent, element);
        }
    }

    public void loadPrefixSuffixComponents() {
        if (pendingLoadPrefixComponent != null) {
            pendingLoadPrefixComponent.loadComponent();
        }

        if (pendingLoadSuffixComponent != null) {
            pendingLoadSuffixComponent.loadComponent();
        }
    }

    protected void createPrefixComponent(HasPrefix hasPrefixComponent, Element hasPrefixElement) {
        Element prefix = hasPrefixElement.element("prefix");

        if (prefix == null) {
            return;
        }

        if (prefix.elements().size() != 1) {
            throw new GuiDevelopmentException("Only one prefix component can be defined", context);
        }

        Element prefixComponentElement = prefix.elements().get(0);
        pendingLoadPrefixComponent = getLayoutLoader().createComponentLoader(prefixComponentElement);
        pendingLoadPrefixComponent.initComponent();

        hasPrefixComponent.setPrefixComponent(pendingLoadPrefixComponent.getResultComponent());
    }

    protected void createSuffixComponent(HasSuffix hasSuffix, Element hasSuffixElement) {
        Element suffix = hasSuffixElement.element("suffix");

        if (suffix == null) {
            return;
        }

        if (suffix.elements().size() != 1) {
            throw new GuiDevelopmentException("Only one suffix component can be defined", context);
        }

        Element suffixComponentElement = suffix.elements().get(0);
        pendingLoadSuffixComponent = getLayoutLoader().createComponentLoader(suffixComponentElement);
        pendingLoadSuffixComponent.initComponent();

        hasSuffix.setSuffixComponent(pendingLoadSuffixComponent.getResultComponent());
    }

    protected LayoutLoader getLayoutLoader() {
        if (layoutLoader == null) {
            layoutLoader = applicationContext.getBean(LayoutLoader.class, context);
        }
        return layoutLoader;
    }
}
