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
import io.jmix.core.impl.FetchPlanLoader;
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.component.SupportsItemsFetchCallback;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsItemsEnum;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.sys.substitutor.StringSubstitutor;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.LoaderResolver;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;

import java.util.Optional;

@org.springframework.stereotype.Component("flowui_DataLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DataLoaderSupport implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DataLoaderSupport.class);

    protected static final String ITEMS_QUERY_ELEMENT = "itemsQuery";
    protected static final String VALUE_PARAMETER = "value";

    protected Context context;
    protected ApplicationContext applicationContext;
    protected LoaderResolver loaderResolver;
    protected ClassManager classManager;
    protected LoaderSupport loaderSupport;
    protected Metadata metadata;
    protected FetchPlanLoader fetchPlanLoader;
    protected FetchPlanRepository fetchPlanRepository;

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

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setFetchPlanLoader(FetchPlanLoader fetchPlanLoader) {
        this.fetchPlanLoader = fetchPlanLoader;
    }

    @Autowired
    public void setFetchPlanRepository(FetchPlanRepository fetchPlanRepository) {
        this.fetchPlanRepository = fetchPlanRepository;
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

            HasDataComponents dataHolder = context.getDataHolder();
            return Optional.of(dataHolder.getContainer(containerId));
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

    /**
     * Loads {@code itemsQuery} element that may contain a query to load either entities or scalar values.
     *
     * @param component a component to set a result
     * @param element   an xml definition
     * @see #loadEntityItemsQuery(SupportsItemsFetchCallback, Element)
     * @see #loadValueItemsQuery(SupportsItemsFetchCallback, Element)
     */
    public void loadItemsQuery(SupportsItemsFetchCallback<?, String> component, Element element) {
        Element itemsElement = element.element(ITEMS_QUERY_ELEMENT);
        if (itemsElement == null) {
            return;
        }

        loaderSupport.loadString(itemsElement, "class")
                .map(ReflectionHelper::getClass).ifPresentOrElse(
                        entityClass -> loadEntityItemsQueryInternal(component, itemsElement, entityClass),
                        () -> loadValueItemsQueryInternal(component, itemsElement));
    }

    /**
     * Loads {@code itemsQuery} element that contains a query to load entities only.
     *
     * @param component a component to set a result
     * @param element   an xml definition
     * @see #loadItemsQuery(SupportsItemsFetchCallback, Element)
     * @see #loadValueItemsQuery(SupportsItemsFetchCallback, Element)
     */
    public void loadEntityItemsQuery(SupportsItemsFetchCallback<?, String> component, Element element) {
        Element itemsElement = element.element(ITEMS_QUERY_ELEMENT);
        if (itemsElement == null) {
            return;
        }

        Class<?> entityClass = loaderSupport.loadString(itemsElement, "class")
                .map(ReflectionHelper::getClass)
                .orElseThrow(() ->
                        new GuiDevelopmentException(String.format("Field 'class' is empty in component %s.",
                                ((Component) component).getId()), context));

        loadEntityItemsQueryInternal(component, itemsElement, entityClass);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void loadEntityItemsQueryInternal(SupportsItemsFetchCallback<?, String> component,
                                                Element itemsElement, Class<?> entityClass) {
        String queryString = loadQuery((Component) component, itemsElement);
        String searchStringFormat = loadSearchStringFormat(itemsElement);
        boolean escapeValue = loadEscapeValueForLike(itemsElement);

        FetchPlan fetchPlan = loadFetchPlan(itemsElement, entityClass);

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

    /**
     * Loads {@code itemsQuery} element that contains a query to load scalar values only.
     *
     * @param component a component to set a result
     * @param element   an xml definition
     * @see #loadItemsQuery(SupportsItemsFetchCallback, Element)
     * @see #loadEntityItemsQuery(SupportsItemsFetchCallback, Element)
     */
    public void loadValueItemsQuery(SupportsItemsFetchCallback<?, String> component, Element element) {
        Element itemsElement = element.element(ITEMS_QUERY_ELEMENT);
        if (itemsElement == null) {
            return;
        }

        loadValueItemsQueryInternal(component, itemsElement);
    }

    protected void loadValueItemsQueryInternal(SupportsItemsFetchCallback<?, String> component,
                                               Element itemsElement) {
        String queryString = loadQuery((Component) component, itemsElement);
        String searchStringFormat = loadSearchStringFormat(itemsElement);
        boolean escapeValue = loadEscapeValueForLike(itemsElement);

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

    protected String loadQuery(Component component, Element itemsElement) {
        Element queryElement = itemsElement.element("query");
        if (queryElement == null) {
            throw new GuiDevelopmentException(String.format("Nested 'query' element is missing " +
                            "for '%s' element in component %s.",
                    ITEMS_QUERY_ELEMENT, component.getId()), context);
        }

        return queryElement.getTextTrim();
    }

    @Nullable
    protected String loadSearchStringFormat(Element itemsElement) {
        return loaderSupport
                .loadString(itemsElement, "searchStringFormat")
                .orElse(null);
    }

    protected Boolean loadEscapeValueForLike(Element itemsElement) {
        return loaderSupport
                .loadBoolean(itemsElement, "escapeValueForLike")
                .orElse(false);
    }

    @Nullable
    protected FetchPlan loadFetchPlan(Element itemsElement, Class<?> entityClass) {
        Element fetchPlanElement = itemsElement.element("fetchPlan");
        if (fetchPlanElement != null) {
            return loadInlineFetchPlan(fetchPlanElement, entityClass);
        }

        return loaderSupport.loadString(itemsElement, "fetchPlan")
                .map(fetchPlanName ->
                        fetchPlanRepository.getFetchPlan(entityClass, fetchPlanName))
                .orElse(null);
    }

    protected FetchPlan loadInlineFetchPlan(Element fetchPlanElement, Class<?> entityClass) {
        FetchPlanLoader.FetchPlanInfo fetchPlanInfo = fetchPlanLoader
                .getFetchPlanInfo(fetchPlanElement, metadata.getClass(entityClass));

        FetchPlanBuilder builder = fetchPlanLoader.getFetchPlanBuilder(fetchPlanInfo, name ->
                fetchPlanRepository.getFetchPlan(fetchPlanInfo.getMetaClass(), name));

        fetchPlanLoader.loadFetchPlanProperties(fetchPlanElement, builder,
                fetchPlanInfo.isSystemProperties(), (metaClass, fetchPlanName) ->
                        fetchPlanRepository.getFetchPlan(metaClass, fetchPlanName));

        return builder.build();
    }

    protected String getSearchString(Query<?, String> query,
                                     @Nullable String searchStringFormat, boolean escapeValue) {
        String searchString = query.getFilter().orElse("");
        if (escapeValue) {
            searchString = QueryUtils.escapeForLike(searchString);
        }

        if (!Strings.isNullOrEmpty(searchStringFormat)) {
            StringSubstitutor substitutor = applicationContext.getBean(StringSubstitutor.class);
            searchString = substitutor.substitute(searchStringFormat, ParamsMap.of("inputString", searchString));
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

            HasDataComponents dataHolder = context.getDataHolder();
            InstanceContainer<?> container = dataHolder.getContainer(containerId);
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

        log.warn("Unable to load container for component with '{}' ID", element.attributeValue("id"));

        return null;
    }
}
