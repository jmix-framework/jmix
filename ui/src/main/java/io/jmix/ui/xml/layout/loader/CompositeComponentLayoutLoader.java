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

import com.google.common.base.Preconditions;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Component;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderResolver;
import io.jmix.ui.xml.layout.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@org.springframework.stereotype.Component("ui_CompositeComponentLayoutLoader")
public class CompositeComponentLayoutLoader {

    public static final String COMPOSITE_COMPONENT_ELEMENT_NAME = "composite";

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected UiComponents factory;
    @Autowired
    protected LoaderResolver loaderResolver;
    @Autowired
    protected LoaderSupport loaderSupport;

    protected ComponentLoader.Context context;

    public CompositeComponentLayoutLoader(ComponentLoader.Context context) {
        this.context = context;
    }

    protected ComponentLoader getLoader(Element element) {
        if (COMPOSITE_COMPONENT_ELEMENT_NAME.equals(element.getName())) {
            List<Element> elements = element.elements();
            Preconditions.checkArgument(elements.size() == 1,
                    "%s must contain a single root element", COMPOSITE_COMPONENT_ELEMENT_NAME);
            element = elements.get(0);
        }

        Class<? extends ComponentLoader> loaderClass = loaderResolver.getLoader(element);
        if (loaderClass == null) {
            throw new GuiDevelopmentException("Unknown component: " + element.getName(), context);
        }

        return initLoader(element, loaderClass);
    }

    protected ComponentLoader initLoader(Element element, Class<? extends ComponentLoader> loaderClass) {
        ComponentLoader loader;

        Constructor<? extends ComponentLoader> constructor;
        try {
            constructor = loaderClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new GuiDevelopmentException("Unable to get constructor for loader: " + e, context);
        }

        try {
            loader = constructor.newInstance();
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new GuiDevelopmentException("Loader instantiation error: " + e, context);
        }

        loader.setApplicationContext(applicationContext);

        loader.setContext(context);
        loader.setLoaderResolver(loaderResolver);
        loader.setLoaderSupport(loaderSupport);
        loader.setFactory(factory);
        loader.setElement(element);

        return loader;
    }

    public Component createComponent(Element element) {
        ComponentLoader loader = getLoader(element);

        loader.createComponent();
        loader.loadComponent();
        return loader.getResultComponent();
    }
}
