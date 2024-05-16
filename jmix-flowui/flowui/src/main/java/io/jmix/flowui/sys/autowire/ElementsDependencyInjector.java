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

package io.jmix.flowui.sys.autowire;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.ViewXmlLoader;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AutowireElement;
import io.jmix.flowui.view.*;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.jmix.flowui.sys.ValuePathHelper.parse;
import static io.jmix.flowui.sys.ValuePathHelper.pathPrefix;

/**
 * An injector that autowires a fields and setters methods that are annotated by the {@link ViewComponent} annotation.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 40)
@org.springframework.stereotype.Component("flowui_ElementsDependencyInjector")
public class ElementsDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(ElementsDependencyInjector.class);

    protected ApplicationContext applicationContext;
    protected ReflectionCacheManager reflectionCacheManager;

    public ElementsDependencyInjector(ApplicationContext applicationContext,
                                      ReflectionCacheManager reflectionCacheManager) {
        this.applicationContext = applicationContext;
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext autowireContext) {
        ViewAutowireContext viewAutowireContext = (ViewAutowireContext) autowireContext;
        View<?> view = viewAutowireContext.getView();

        List<AutowireElement> autowireElements =
                reflectionCacheManager.getViewAutowireElements(view.getClass());
        Collection<Object> autowired = viewAutowireContext.getAutowired();

        for (AutowireElement element : autowireElements) {
            if (!autowired.contains(element)) {
                doAutowiring(element, view, autowired);
            }
        }
    }

    protected void doAutowiring(AutowireElement autowireElement, View<?> view, Collection<Object> autowired) {
        String name = AutowireUtils.getAutowiringName(autowireElement);
        Class<?> type = AutowireUtils.getAutowiringType(autowireElement);

        Object instance = getAutowiredInstance(type, name, view);

        if (instance != null) {
            AutowireUtils.assignValue(autowireElement.getElement(), instance, view);
            autowired.add(autowireElement);
        } else {
            log.trace("Skip autowiring {} of {} because instance not found",
                    name, view.getClass());
        }
    }

    @Nullable
    protected Object getAutowiredInstance(Class<?> type, String name, View<?> view) {
        Component layout = view.getContent();
        if (!UiComponentUtils.isContainer(layout)) {
            throw new IllegalStateException(view.getClass().getSimpleName() + "'s layout component " +
                    "doesn't support child components");
        }

        if (Component.class.isAssignableFrom(type)) {
            Optional<Component> childComponent = UiComponentUtils.findComponent(layout, name);
            // Autowiring a UI component
            return childComponent.orElse(null);
        } else if (InstanceContainer.class.isAssignableFrom(type)) {
            // Autowiring a container
            ViewData data = ViewControllerUtils.getViewData(view);
            return data.getContainer(name);
        } else if (DataLoader.class.isAssignableFrom(type)) {
            // Autowiring a loader
            ViewData data = ViewControllerUtils.getViewData(view);
            return data.getLoader(name);
        } else if (DataContext.class.isAssignableFrom(type)) {
            ViewData data = ViewControllerUtils.getViewData(view);
            return data.getDataContext();

        } else if (Action.class.isAssignableFrom(type)) {
            // Autowiring an action
            String[] elements = parse(name);
            if (elements.length == 1) {
                ViewActions viewActions = ViewControllerUtils.getViewActions(view);
                return viewActions.getAction(name);
            }

            String prefix = pathPrefix(elements);
            return UiComponentUtils.findComponent(layout, prefix)
                    .filter(c -> c instanceof HasActions)
                    .map(c -> ((HasActions) c))
                    .map(component -> component.getAction(elements[elements.length - 1]))
                    .orElse(null);

        } else if (Facet.class.isAssignableFrom(type)) {
            String[] elements = parse(name);
            if (elements.length != 1) {
                throw new IllegalStateException(
                        String.format("Can't autowire %s. Incorrect path: '%s'", Facet.class.getSimpleName(), name));
            }

            return ViewControllerUtils.getViewFacets(view).getFacet(name);
        } else if (MessageBundle.class == type) {
            return createMessageBundle(view);
        }

        return null;
    }

    protected MessageBundle createMessageBundle(View<?> controller) {
        MessageBundle messageBundle = applicationContext.getBean(MessageBundle.class);
        messageBundle.setMessageGroup(ViewControllerUtils.getPackage(controller.getClass()));

        if (controller.getId().isEmpty()) {
            return messageBundle;
        }

        ViewInfo viewInfo = applicationContext.getBean(ViewRegistry.class)
                .getViewInfo(controller.getId().get());

        ViewXmlLoader viewXmlLoader = applicationContext.getBean(ViewXmlLoader.class);
        Optional<String> templatePath = viewInfo.getTemplatePath();
        Element element = templatePath.map(viewXmlLoader::load).orElse(null);
        if (element != null) {
            String messagesGroup = element.attributeValue("messagesGroup");
            if (!Strings.isNullOrEmpty(messagesGroup)) {
                messageBundle.setMessageGroup(messagesGroup);
            }
        }

        return messageBundle;
    }

    @Override
    public boolean isApplicable(AutowireContext autowireContext) {
        return autowireContext instanceof ViewAutowireContext;
    }
}
