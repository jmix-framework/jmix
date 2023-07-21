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

package io.jmix.flowui.xml.layout.support;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.Query;
import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.flowui.component.SupportsItemsFetchCallback;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsItemsEnum;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.substitutor.StringSubstitutor;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.LoaderResolver;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

@org.springframework.stereotype.Component("flowui_DataLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DataLoaderSupport implements ApplicationContextAware {

    protected static final String QUERY_ITEMS_ELEMENT = "queryItems";
    protected static final String VALUE_PARAMETER = "value";

    protected Context context;
    protected ApplicationContext applicationContext;
    protected LoaderResolver loaderResolver;
    protected ClassManager classManager;
    protected LoaderSupport loaderSupport;

    public DataLoaderSupport(Context context) {
        this.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @Autowired
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
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
            ViewData viewData = ViewControllerUtils.getViewData(view);

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

    public void loadQueryItems(SupportsItemsFetchCallback<?, String> component, Element element) {
        Element queryItemsElement = element.element(QUERY_ITEMS_ELEMENT);
        if (queryItemsElement == null) {
            return;
        }

        loaderSupport.loadString(queryItemsElement, "class")
                .map(ReflectionHelper::getClass).ifPresentOrElse(
                        entityClass -> loadEntityQueryItemsInternal(component, queryItemsElement, entityClass),
                        () -> loadValueQueryItemsInternal(component, queryItemsElement));
    }

    public void loadEntityQueryItems(SupportsItemsFetchCallback<?, String> component, Element element) {
        Element queryItemsElement = element.element(QUERY_ITEMS_ELEMENT);
        if (queryItemsElement == null) {
            return;
        }

        Class<?> entityClass = loaderSupport.loadString(queryItemsElement, "class")
                .map(ReflectionHelper::getClass)
                .orElseThrow(() ->
                        new GuiDevelopmentException(String.format("Field 'class' is empty in component %s.",
                                ((Component) component).getId()), getComponentContext()));

        loadEntityQueryItemsInternal(component, queryItemsElement, entityClass);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void loadEntityQueryItemsInternal(SupportsItemsFetchCallback<?, String> component,
                                                Element queryItemsElement, Class<?> entityClass) {
        String queryString = queryItemsElement.getStringValue();
        String searchStringFormat = loadSearchStringFormat(queryItemsElement);
        boolean escapeValue = loadEscapeValueForLike(queryItemsElement);

        FetchPlan fetchPlan = loadFetchPlan(queryItemsElement, entityClass);

        DataManager dataManager = applicationContext.getBean(DataManager.class);
        component.setItemsFetchCallback(query -> {
            String searchString = getSearchString(query, searchStringFormat, escapeValue);

            FluentLoader.ByQuery loader = dataManager.load(entityClass)
                    .query(queryString)
                    .parameter("searchString", searchString)
                    .firstResult(query.getOffset())
                    .maxResults(query.getLimit());
            if (fetchPlan != null) {
                loader.fetchPlan(fetchPlan);
            }

            return loader.list().stream();
        });
    }

    public void loadValueQueryItems(SupportsItemsFetchCallback<?, String> component, Element element) {
        Element queryItemsElement = element.element(QUERY_ITEMS_ELEMENT);
        if (queryItemsElement == null) {
            return;
        }

        loadValueQueryItemsInternal(component, queryItemsElement);
    }

    protected void loadValueQueryItemsInternal(SupportsItemsFetchCallback<?, String> component,
                                               Element queryItemsElement) {
        String queryString = queryItemsElement.getStringValue();
        String searchStringFormat = loadSearchStringFormat(queryItemsElement);
        boolean escapeValue = loadEscapeValueForLike(queryItemsElement);

        DataManager dataManager = applicationContext.getBean(DataManager.class);
        component.setItemsFetchCallback(query -> {
            String searchString = getSearchString(query, searchStringFormat, escapeValue);

            return dataManager.loadValues(queryString)
                    .properties(VALUE_PARAMETER)
                    .parameter("searchString", searchString)
                    .firstResult(query.getOffset())
                    .maxResults(query.getLimit())
                    .list().stream()
                    .map(entity -> entity.getValue(VALUE_PARAMETER));
        });
    }

    @Nullable
    protected String loadSearchStringFormat(Element queryItemsElement) {
        return loaderSupport
                .loadString(queryItemsElement, "searchStringFormat")
                .orElse(null);
    }

    protected Boolean loadEscapeValueForLike(Element queryItemsElement) {
        return loaderSupport
                .loadBoolean(queryItemsElement, "escapeValueForLike")
                .orElse(false);
    }

    @Nullable
    protected FetchPlan loadFetchPlan(Element queryItemsElement, Class<?> entityClass) {
        return loaderSupport.loadString(queryItemsElement, "fetchPlan")
                .map(fetchPlanName -> {
                    FetchPlanRepository fetchPlanRepository = applicationContext.getBean(FetchPlanRepository.class);
                    return fetchPlanRepository.getFetchPlan(entityClass, fetchPlanName);
                })
                .orElse(null);
    }

    protected String getSearchString(Query<?, String> query,
                                     @Nullable String searchStringFormat, boolean escapeValue) {
        String searchString = query.getFilter().orElse("");
        if (escapeValue) {
            searchString = QueryUtils.escapeForLike(searchString);
        }

        if (!Strings.isNullOrEmpty(searchStringFormat)) {
            StringSubstitutor substitutor = applicationContext.getBean(StringSubstitutor.class);
            searchString = substitutor.substitute(searchStringFormat, ParamsMap.of("searchString", searchString));
        }

        return searchString;
    }

    public <E> void loadItemsContainer(SupportsItemsContainer<E> component, Element element) {
        Optional<CollectionContainer<E>> container = loadItemsContainer(element);
        container.ifPresent(component::setItems);
    }

    protected <E> Optional<CollectionContainer<E>> loadItemsContainer(Element element) {
        String containerId = element.attributeValue("itemsContainer");
        if (containerId != null) {

            View<?> view = getComponentContext().getView();
            ViewData viewData = ViewControllerUtils.getViewData(view);
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
