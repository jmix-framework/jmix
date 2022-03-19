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

import org.springframework.context.ApplicationContext;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.Window;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.UiComponents;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LayoutLoaderConfig;
import io.jmix.ui.xml.layout.LoaderSupport;
import io.jmix.ui.xml.layout.LoaderResolver;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("ui_LayoutLoader")
public class LayoutLoader {

    protected ComponentLoader.Context context;
    protected UiComponents factory;
    @Deprecated
    protected LayoutLoaderConfig config;
    protected LoaderResolver loaderResolver;
    protected LoaderSupport loaderSupport;

    protected ApplicationContext applicationContext;
    protected Environment environment;

    public LayoutLoader(ComponentLoader.Context context) {
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
    protected void setConfig(LayoutLoaderConfig config) {
        this.config = config;
    }

    @Autowired
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    protected ComponentLoader getLoader(Element element) {
        Class<? extends ComponentLoader> loaderClass = loaderResolver.getLoader(element);
        if (loaderClass == null) {
            throw new GuiDevelopmentException("Unknown component: " + element.getName(), context);
        }

        return initLoader(element, loaderClass);
    }

    protected FragmentLoader getFragmentLoader(Element rootWindowElement) {
        Class<? extends ComponentLoader> loaderClass = loaderResolver.getFragmentLoader(rootWindowElement);

        return (FragmentLoader) initLoader(rootWindowElement, loaderClass);
    }

    protected WindowLoader getWindowLoader(Element rootWindowElement) {
        Class<? extends ComponentLoader> loaderClass = loaderResolver.getWindowLoader(rootWindowElement);

        return (WindowLoader) initLoader(rootWindowElement, loaderClass);
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
        loader.setEnvironment(environment);

        loader.setContext(context);
        loader.setLayoutLoaderConfig(config);
        loader.setLoaderResolver(loaderResolver);
        loader.setLoaderSupport(loaderSupport);
        loader.setFactory(factory);
        loader.setElement(element);

        return loader;
    }

    public ComponentLoader createComponent(Element element) {
        ComponentLoader loader = getLoader(element);

        loader.createComponent();
        return loader;
    }

    public ComponentLoader<Fragment> createFragmentContent(Fragment fragment, Element rootWindowElement) {
        FragmentLoader fragmentLoader = getFragmentLoader(rootWindowElement);
        fragmentLoader.setResultComponent(fragment);

        Element layout = rootWindowElement.element("layout");
        if (layout != null) {
            fragmentLoader.createContent(layout);
        }

        return fragmentLoader;
    }

    public ComponentLoader<Window> createWindowContent(Window window, Element rootWindowElement) {
        WindowLoader windowLoader = getWindowLoader(rootWindowElement);
        windowLoader.setResultComponent(window);

        Element layout = rootWindowElement.element("layout");
        if (layout != null) {
            windowLoader.createContent(layout);
        }
        return windowLoader;
    }

    public ComponentLoader getLoader(Element element, String name) {
        Class<? extends ComponentLoader> loaderClass = config.getLoader(name);
        if (loaderClass == null) {
            throw new GuiDevelopmentException("Unknown component: " + name, context);
        }

        return initLoader(element, loaderClass);
    }

    public ComponentLoader getLoader(Element element, Class<? extends ComponentLoader> loaderClass) {
        return initLoader(element, loaderClass);
    }
}