/*
 * Copyright (c) 2008-2022 Haulmont.
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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.core.ClassManager;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsItemsEnum;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.UiControllerUtils;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.LoaderResolver;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

@org.springframework.stereotype.Component("flowui_DataLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DataLoaderSupport {

    protected Context context;
    protected LoaderResolver loaderResolver;
    protected ClassManager classManager;

    public DataLoaderSupport(Context context) {
        this.context = context;
    }

    @Autowired
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @Autowired
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    public void loadData(SupportsValueSource<?> component, Element element) {
        loadContainer(component, element);
    }

    public void loadContainer(SupportsValueSource<?> component, Element element) {
        String property = element.attributeValue("property");
        loadContainer(element, property).ifPresent(container ->
                component.setValueSource(new ContainerValueSource<>(container, property)));
    }

    public Optional<InstanceContainer<?>> loadContainer(Element element, @Nullable String property) {
        String containerId = element.attributeValue("dataContainer");

        // In case a component has only a property,
        // we try to obtain `dataContainer` from a parent element.
        // For instance, a component is placed within the Form component
        if (Strings.isNullOrEmpty(containerId) && property != null) {
            containerId = getParentDataContainer(element);
        }

        if (!Strings.isNullOrEmpty(containerId)) {
            if (property == null) {
                throw new GuiDevelopmentException(
                        String.format("Can't set container '%s' for component '%s' because 'property' " +
                                "attribute is not defined", containerId, element.attributeValue("id")), context);
            }

            View<?> view = getComponentContext().getView();
            ViewData viewData = UiControllerUtils.getViewData(view);

            return Optional.of(viewData.getContainer(containerId));
        }

        return Optional.empty();
    }

    public void loadItems(Component component, Element element) {
        if (component instanceof SupportsItemsContainer) {
            loadItemsContainer(((SupportsItemsContainer<?>) component), element);
        }

        if (component instanceof SupportsItemsEnum) {
            loadItemsEnum(((SupportsItemsEnum<?>) component), element);
        }
    }

    public <E> void loadItemsContainer(SupportsItemsContainer<E> component, Element element) {
        Optional<CollectionContainer<E>> container = loadItemsContainer(element);
        container.ifPresent(component::setItems);
    }

    protected <E> Optional<CollectionContainer<E>> loadItemsContainer(Element element) {
        String containerId = element.attributeValue("itemsContainer");
        if (containerId != null) {

            View<?> view = getComponentContext().getView();
            ViewData viewData = UiControllerUtils.getViewData(view);
            InstanceContainer<?> container = viewData.getContainer(containerId);
            if (!(container instanceof CollectionContainer)) {
                throw new GuiDevelopmentException(String.format("Not a %s: %s",
                        CollectionContainer.class.getSimpleName(), containerId),
                        context);
            }
            //noinspection unchecked
            return Optional.of((CollectionContainer<E>) container);
        }

        return Optional.empty();
    }

    public <T> void loadItemsEnum(SupportsItemsEnum<T> component, Element element) {
        Optional<Class<T>> enumClass = loadItemsEnum(element);
        enumClass.ifPresent(component::setItems);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Class<T>> loadItemsEnum(Element element) {
        String itemsEnumClass = element.attributeValue("itemsEnum");
        if (!Strings.isNullOrEmpty(itemsEnumClass)) {
            return Optional.ofNullable(((Class<T>) classManager.findClass(itemsEnumClass)));
        }

        return Optional.empty();
    }

    @Nullable
    protected String getParentDataContainer(Element element) {
        Element parent = element.getParent();
        while (parent != null) {
            if (loaderResolver.getLoader(parent) != null && parent.attributeValue("dataContainer") != null) {
                return parent.attributeValue("dataContainer");
            }
            parent = parent.getParent();
        }
        return null;
    }

    protected ComponentContext getComponentContext() {
        checkState(context instanceof ComponentContext,
                "'context' must implement " + ComponentContext.class.getName());

        return (ComponentContext) context;
    }
}
