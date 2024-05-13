/*
 * Copyright 2024 Haulmont.
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

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.composite.CompositeActions;
import io.jmix.flowui.component.composite.CompositeComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.LoaderResolver;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

// TODO: gg, base class with LayoutLoader
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@org.springframework.stereotype.Component("flowui_CompositeComponentLoader")
public class CompositeComponentLoader {

    public static final String CONTENT_ELEMENT_NAME = "layout";

    protected ApplicationContext applicationContext;
    protected Environment environment;
    protected LoaderResolver loaderResolver;
    protected LoaderSupport loaderSupport;
    protected ActionLoaderSupport actionLoaderSupport;

    protected final CompositeComponentLoaderContext context;
    protected final Element element;

    public CompositeComponentLoader(CompositeComponentLoaderContext context, Element element) {
        this.context = context;
        this.element = element;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    public void createContent() {
        Element contentElement = getContentElement(element);

        loadActions(element);

        ComponentLoader<?> componentLoader = getLoader(contentElement);
        componentLoader.initComponent();
        componentLoader.loadComponent();
    }

    protected Element getContentElement(Element element) {
        Element contentElement = element.element(CONTENT_ELEMENT_NAME);
        if (contentElement == null) {
            throw new GuiDevelopmentException(
                    String.format("Required '%s' element is not found", CONTENT_ELEMENT_NAME), context);
        }

        List<Element> elements = contentElement.elements();
        if (elements.size() != 1) {
            throw new GuiDevelopmentException(
                    String.format("'%s' must contain a single child element", CONTENT_ELEMENT_NAME), context);
        }

        return elements.get(0);
    }

    protected void loadActions(Element element) {
        Element actionsElement = element.element("actions");
        if (actionsElement == null) {
            return;
        }

        CompositeActions compositeActions = CompositeComponentUtils.getCompositeActions(context.getComposite());
        for (Element actionElement : actionsElement.elements("action")) {
            compositeActions.addAction(loadDeclarativeAction(actionElement));
        }
    }

    protected Action loadDeclarativeAction(Element element) {
        return getActionLoaderSupport().loadDeclarativeActionByType(element)
                .orElseGet(() ->
                        getActionLoaderSupport().loadDeclarativeAction(element));
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
    protected ComponentLoader<?> initLoader(Element element, Class<? extends ComponentLoader> loaderClass) {
        // In case if of changes, sync with 'io.jmix.flowui.xml.layout.loader.LayoutLoader.initLoader'
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
        loader.setFactory(new UiComponents() {
            @Override
            public <T extends Component> T create(Class<T> type) {
                Component content = context.getComposite().getContent();
                if (!type.isAssignableFrom(content.getClass())) {
                    throw new GuiDevelopmentException("Composite content type and XML content type don't match",
                            context, ImmutableMap.of(
                            "Composite content type", content.getClass().getName(),
                            "XML content type", type.getName()
                    ));
                }

                //noinspection unchecked
                return ((T) content);
            }
        });
        loader.setElement(element);

        return loader;
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }
}
