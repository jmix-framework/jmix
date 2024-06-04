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
package io.jmix.flowui.xml.layout.loader;

import io.jmix.flowui.UiComponents;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.LoaderResolver;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("flowui_LayoutLoader")
public class LayoutLoader {

    protected Context context;
    protected UiComponents factory;
    protected LoaderResolver loaderResolver;
    protected LoaderSupport loaderSupport;

    protected ApplicationContext applicationContext;
    protected Environment environment;

    public LayoutLoader(Context context) {
        this.context = context;
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    protected void setFactory(UiComponents factory) {
        this.factory = factory;
    }

    @Autowired
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    @SuppressWarnings("rawtypes")
    protected ComponentLoader<?> getLoader(Element element) {
        Class<? extends ComponentLoader> loaderClass = loaderResolver.getLoader(element);
        if (loaderClass == null) {
            throw new GuiDevelopmentException("Unknown component: " + element.getName(), context);
        }

        return initLoader(element, loaderClass);
    }

    @SuppressWarnings("rawtypes")
    public ComponentLoader<?> getLoader(Element element, Class<? extends ComponentLoader> loaderClass) {
        return initLoader(element, loaderClass);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected AbstractViewLoader<View<?>> getViewLoader(Element rootElement) {
        Class<? extends ComponentLoader> loaderClass = loaderResolver.getViewLoader(rootElement);

        if (loaderClass == null) {
            throw new IllegalStateException(String.format("Loader for %s not found", rootElement.getName()));
        }

        return (AbstractViewLoader<View<?>>) initLoader(rootElement, loaderClass);
    }

    @SuppressWarnings("rawtypes")
    protected ComponentLoader<?> initLoader(Element element, Class<? extends ComponentLoader> loaderClass) {
        // In case if of changes, sync with 'io.jmix.flowui.xml.layout.loader.FragmentLoader.initLoader'
        ComponentLoader<?> loader;

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
        loader.setEnvironment(environment);

        loader.setContext(context);
        loader.setLoaderResolver(loaderResolver);
        loader.setLoaderSupport(loaderSupport);
        loader.setFactory(factory);
        loader.setElement(element);

        return loader;
    }

    public ComponentLoader<?> createComponentLoader(Element element) {
        return getLoader(element);
    }

    public ComponentLoader<View<?>> createViewContent(View<?> view, Element rootElement) {
        AbstractViewLoader<View<?>> loader = getViewLoader(rootElement);
        loader.setResultComponent(view);
        loader.createContent();
        return loader;
    }
}
