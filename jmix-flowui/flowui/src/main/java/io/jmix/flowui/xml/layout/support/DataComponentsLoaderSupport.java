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

package io.jmix.flowui.xml.layout.support;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.impl.FetchPlanLoader;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.ConditionXmlLoader;
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.model.*;
import io.jmix.flowui.model.impl.NoopDataContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Objects;

@Component("flowui_DataComponentsLoaderSupport")
public class DataComponentsLoaderSupport {

    public static final String GENERATED_PREFIX = "generated_";
    public static final String COLLECTION_CONTAINER_ELEMENT = "collection";
    public static final String INSTANCE_CONTAINER_ELEMENT = "instance";

    protected final ApplicationContext applicationContext;
    protected final FetchPlanRepository fetchPlanRepository;
    protected final FetchPlanLoader fetchPlanLoader;
    protected final Metadata metadata;
    protected final MetadataTools metadataTools;
    protected final DataComponents factory;
    protected final ConditionXmlLoader conditionXmlLoader;
    protected final DatatypeRegistry datatypeRegistry;
    protected final LoaderSupport loaderSupport;

    public DataComponentsLoaderSupport(ApplicationContext applicationContext,
                                       FetchPlanRepository fetchPlanRepository,
                                       FetchPlanLoader fetchPlanLoader,
                                       Metadata metadata,
                                       MetadataTools metadataTools,
                                       DataComponents factory,
                                       ConditionXmlLoader conditionXmlLoader,
                                       DatatypeRegistry datatypeRegistry,
                                       LoaderSupport loaderSupport) {
        this.applicationContext = applicationContext;
        this.fetchPlanRepository = fetchPlanRepository;
        this.fetchPlanLoader = fetchPlanLoader;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.factory = factory;
        this.conditionXmlLoader = conditionXmlLoader;
        this.datatypeRegistry = datatypeRegistry;
        this.loaderSupport = loaderSupport;
    }

    public void load(HasDataComponents dataHolder, Element element) {
        load(dataHolder, element, null);
    }

    public void load(HasDataComponents dataHolder, Element element,
                     @Nullable HasDataComponents hostDataHolder) {
        Preconditions.checkNotNullArgument(dataHolder, HasDataComponents.class.getSimpleName() + " is null");
        Preconditions.checkNotNullArgument(element, "Element is null");

        DataContext hostDataContext = hostDataHolder != null
                ? hostDataHolder.getDataContextOrNull()
                : null;
        if (hostDataContext != null) {
            dataHolder.setDataContext(hostDataContext);
        } else {
            boolean readOnly = loadReadOnly(element);
            DataContext dataContext = readOnly ? new NoopDataContext(applicationContext) : factory.createDataContext();
            dataHolder.setDataContext(dataContext);
        }

        for (Element el : element.elements()) {
            switch (el.getName()) {
                case "collection":
                    loadCollectionContainer(dataHolder, el, hostDataHolder);
                    break;
                case "instance":
                    loadInstanceContainer(dataHolder, el, hostDataHolder);
                    break;
                case "keyValueCollection":
                    loadKeyValueCollectionContainer(dataHolder, el, hostDataHolder);
                    break;
                case "keyValueInstance":
                    loadKeyValueInstanceContainer(dataHolder, el, hostDataHolder);
                    break;
                default:
                    // no action
                    break;
            }
        }
    }

    protected void loadInstanceContainer(HasDataComponents dataHolder, Element element,
                                         @Nullable HasDataComponents hostDataHolder) {
        String containerId = loadRequiredAttribute(element, "id");

        InstanceContainer<?> container;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            container = Objects.requireNonNull(hostDataHolder).getContainer(containerId);
        } else {
            container = factory.createInstanceContainer(getEntityClass(element));
            loadFetchPlan(element, getEntityClass(element), container);
        }

        dataHolder.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadInstanceLoader(dataHolder, loaderEl, container, hostDataHolder);
        }

        for (Element collectionEl : element.elements()) {
            loadNestedContainer(dataHolder, collectionEl, container, hostDataHolder);
        }
    }

    protected void loadCollectionContainer(HasDataComponents dataHolder, Element element,
                                           @Nullable HasDataComponents hostDataHolder) {
        String containerId = loadRequiredAttribute(element, "id");

        CollectionContainer<?> container;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            container = Objects.requireNonNull(hostDataHolder).getContainer(containerId);
        } else {
            container = factory.createCollectionContainer(getEntityClass(element));
            loadFetchPlan(element, getEntityClass(element), container);
        }

        dataHolder.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadCollectionLoader(dataHolder, loaderEl, container, hostDataHolder);
        }

        for (Element collectionEl : element.elements()) {
            loadNestedContainer(dataHolder, collectionEl, container, hostDataHolder);
        }
    }

    protected void loadKeyValueCollectionContainer(HasDataComponents dataHolder, Element element,
                                                   @Nullable HasDataComponents hostDataHolder) {
        String containerId = loadRequiredAttribute(element, "id");

        KeyValueCollectionContainer container;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            container = Objects.requireNonNull(hostDataHolder).getContainer(containerId);
        } else {
            container = factory.createKeyValueCollectionContainer();
            loadProperties(element, container);
        }

        loaderSupport.loadString(element, "idName", container::setIdName);

        dataHolder.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadKeyValueCollectionLoader(dataHolder, loaderEl, container, hostDataHolder);
        }
    }

    protected void loadKeyValueInstanceContainer(HasDataComponents dataHolder, Element element,
                                                 @Nullable HasDataComponents hostDataHolder) {
        String containerId = loadRequiredAttribute(element, "id");

        KeyValueContainer container;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            container = Objects.requireNonNull(hostDataHolder).getContainer(containerId);
        } else {
            container = factory.createKeyValueContainer();
            loadProperties(element, container);
        }

        loaderSupport.loadString(element, "idName", container::setIdName);

        dataHolder.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadKeyValueInstanceLoader(dataHolder, loaderEl, container, hostDataHolder);
        }
    }

    private void loadProperties(Element element, KeyValueContainer container) {
        Element propsElelement = element.element("properties");
        if (propsElelement != null) {
            for (Element propElelement : propsElelement.elements()) {
                String name = loadRequiredAttribute(propElelement, "name");
                String className = propElelement.attributeValue("class");
                if (Strings.isNullOrEmpty(className)) {
                    String typeName = propElelement.attributeValue("datatype");
                    Datatype<?> datatype = typeName == null
                            ? datatypeRegistry.get(String.class)
                            : datatypeRegistry.get(typeName);
                    container.addProperty(name, datatype);
                } else {
                    container.addProperty(name, ReflectionHelper.getClass(className));
                }
            }

            loaderSupport.loadString(propsElelement, "idProperty")
                    .ifPresent(idProperty -> {
                        if (container.getEntityMetaClass().findProperty(idProperty) == null) {
                            throw new DevelopmentException(String.format("Property '%s' is not defined", idProperty));
                        }
                        container.setIdName(idProperty);
                    });
        }
    }

    protected void loadNestedContainer(HasDataComponents dataHolder, Element element,
                                       InstanceContainer<?> masterContainer,
                                       @Nullable HasDataComponents hostDataHolder) {
        if (!element.getName().equals(COLLECTION_CONTAINER_ELEMENT)
                && !element.getName().equals(INSTANCE_CONTAINER_ELEMENT)) {
            return;
        }

        String containerId = loadRequiredAttribute(element, "id");
        String property = loadRequiredAttribute(element, "property");
        MetaProperty metaProperty = masterContainer.getEntityMetaClass().getProperty(property);

        InstanceContainer<?> nestedContainer = null;

        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            nestedContainer = Objects.requireNonNull(hostDataHolder).getContainer(containerId);
        } else if (element.getName().equals(COLLECTION_CONTAINER_ELEMENT)) {
            if (!metaProperty.getRange().isClass() || !metaProperty.getRange().getCardinality().isMany()) {
                throw new IllegalStateException(String.format(
                        "Cannot bind collection container '%s' to a non-collection property '%s'",
                        containerId, property));
            }
            nestedContainer = factory.createCollectionContainer(
                    metaProperty.getRange().asClass().getJavaClass(), masterContainer, property);

        } else if (element.getName().equals(INSTANCE_CONTAINER_ELEMENT)) {
            if (!metaProperty.getRange().isClass() || metaProperty.getRange().getCardinality().isMany()) {
                throw new IllegalStateException(String.format(
                        "Cannot bind instance container '%s' to a non-reference property '%s'",
                        containerId, property));
            }
            nestedContainer = factory.createInstanceContainer(
                    metaProperty.getRange().asClass().getJavaClass(), masterContainer, property);
        }

        if (nestedContainer != null) {
            dataHolder.registerContainer(containerId, nestedContainer);

            for (Element collectionEl : element.elements()) {
                loadNestedContainer(dataHolder, collectionEl, nestedContainer, hostDataHolder);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void loadInstanceLoader(HasDataComponents dataHolder, Element element,
                                      InstanceContainer<?> container,
                                      @Nullable HasDataComponents hostDataHolder) {
        String loaderId = loadIdOrGenerate(element);

        InstanceLoader<?> loader;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            loader = Objects.requireNonNull(hostDataHolder).getLoader(loaderId);
        } else {
            loader = factory.createInstanceLoader();

            if (!loadReadOnly(element)) {
                loader.setDataContext(dataHolder.getDataContextOrNull());
            }

            loader.setContainer(((InstanceContainer) container));

            loadQuery(element, loader);
            loadEntityId(element, loader);
        }

        dataHolder.registerLoader(loaderId, loader);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void loadCollectionLoader(HasDataComponents dataHolder, Element element,
                                        CollectionContainer<?> container,
                                        @Nullable HasDataComponents hostDataHolder) {
        String loaderId = loadIdOrGenerate(element);

        CollectionLoader<?> loader;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            loader = Objects.requireNonNull(hostDataHolder).getLoader(loaderId);
        } else {
            loader = factory.createCollectionLoader();

            if (!loadReadOnly(element)) {
                loader.setDataContext(dataHolder.getDataContextOrNull());
            }
            loader.setContainer(((CollectionContainer) container));

            loadQuery(element, loader);
            loaderSupport.loadInteger(element, "firstResult", loader::setFirstResult);
            loaderSupport.loadInteger(element, "maxResults", loader::setMaxResults);
            loaderSupport.loadBoolean(element, "cacheable", loader::setCacheable);
        }

        dataHolder.registerLoader(loaderId, loader);
    }

    protected void loadKeyValueCollectionLoader(HasDataComponents dataHolder, Element element,
                                                KeyValueCollectionContainer container,
                                                @Nullable HasDataComponents hostDataHolder) {
        String loaderId = loadIdOrGenerate(element);

        KeyValueCollectionLoader loader;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            loader = Objects.requireNonNull(hostDataHolder).getLoader(loaderId);
        } else {
            loader = factory.createKeyValueCollectionLoader();
            loader.setContainer(container);

            loadQuery(element, loader);
            loaderSupport.loadInteger(element, "firstResult", loader::setFirstResult);
            loaderSupport.loadInteger(element, "maxResults", loader::setMaxResults);
            loaderSupport.loadString(element, "store", loader::setStoreName);
        }

        dataHolder.registerLoader(loaderId, loader);
    }

    protected void loadKeyValueInstanceLoader(HasDataComponents dataHolder, Element element,
                                              KeyValueContainer container,
                                              @Nullable HasDataComponents hostDataHolder) {
        String loaderId = loadIdOrGenerate(element);

        KeyValueInstanceLoader loader;
        if (checkProvided(element, hostDataHolder)) {
            // 'checkProvided' throws exception if provided="true" and hostDataHolder is null
            loader = Objects.requireNonNull(hostDataHolder).getLoader(loaderId);
        } else {
            loader = factory.createKeyValueInstanceLoader();
            loader.setContainer(container);

            loadQuery(element, loader);
            loaderSupport.loadString(element, "store", loader::setStoreName);
        }

        dataHolder.registerLoader(loaderId, loader);
    }

    protected Class<?> getEntityClass(Element element) {
        String entityClassName = loadRequiredAttribute(element, "class");
        return ReflectionHelper.getClass(entityClassName);
    }

    protected void loadFetchPlan(Element element, Class<?> entityClass,
                                 InstanceContainer<?> container) {
        Element fetchPlanElement = element.element("fetchPlan");
        if (fetchPlanElement != null) {
            container.setFetchPlan(loadInlineFetchPlan(fetchPlanElement, entityClass));
            return;
        }

        loaderSupport.loadString(element, "fetchPlan")
                .map(fetchPlanName ->
                        fetchPlanRepository.getFetchPlan(entityClass, fetchPlanName))
                .ifPresent(container::setFetchPlan);
    }

    protected FetchPlan loadInlineFetchPlan(Element fetchPlanElement, Class<?> entityClass) {
        FetchPlanLoader.FetchPlanInfo fetchPlanInfo =
                fetchPlanLoader.getFetchPlanInfo(fetchPlanElement, metadata.getClass(entityClass));

        FetchPlanBuilder builder = fetchPlanLoader.getFetchPlanBuilder(fetchPlanInfo, ancestorFetchPlanResolver ->
                fetchPlanRepository.getFetchPlan(fetchPlanInfo.getMetaClass(), ancestorFetchPlanResolver));

        fetchPlanLoader.loadFetchPlanProperties(fetchPlanElement, builder,
                fetchPlanInfo.isSystemProperties(), fetchPlanRepository::getFetchPlan);

        return builder.build();
    }

    protected void loadQuery(Element element, DataLoader loader) {
        Element queryElement = element.element("query");
        if (queryElement == null) {
            return;
        }

        loader.setQuery(loadQueryText(queryElement));
        Element conditionEl = queryElement.element("condition");
        if (conditionEl != null) {
            if (!conditionEl.elements().isEmpty()) {
                if (conditionEl.elements().size() == 1) {
                    Condition condition = conditionXmlLoader.fromXml(conditionEl.elements().get(0));
                    loader.setCondition(condition);
                } else {
                    throw new IllegalStateException("'condition' element must have exactly one nested element");
                }
            }
        }
    }

    protected String loadQueryText(Element queryElement) {
        return queryElement.getText().trim();
    }

    protected void loadEntityId(Element element, InstanceLoader<?> loader) {
        String entityId = element.attributeValue("entityId");
        if (Strings.isNullOrEmpty(entityId)) {
            return;
        }

        MetaProperty property = metadataTools.getPrimaryKeyProperty(loader.getContainer().getEntityMetaClass());
        if (property == null) {
            throw new IllegalStateException("Cannot determine id property for " +
                    loader.getContainer().getEntityMetaClass());
        }

        if (property.getRange().isDatatype()) {
            try {
                Object value = property.getRange().asDatatype().parse(entityId);
                loader.setEntityId(value);
            } catch (ParseException e) {
                throw new RuntimeException("Error parsing entityId for " + loader, e);
            }
        } else {
            throw new IllegalStateException("Cannot assign id to " + loader +
                    " because the entity has a composite PK");
        }
    }

    protected String loadRequiredAttribute(Element element, String attributeName) {
        return loaderSupport.loadString(element, attributeName)
                .orElseThrow(() ->
                        new IllegalStateException("Required attribute '" + attributeName +
                                "' not found in " + element));
    }

    protected String loadIdOrGenerate(Element element) {
        return loaderSupport.loadString(element, "id")
                .orElseGet(this::generateId);
    }

    protected boolean loadReadOnly(Element element) {
        return Boolean.parseBoolean(element.attributeValue("readOnly"));
    }

    protected boolean checkProvided(Element element, @Nullable HasDataComponents hostDataHolder) {
        boolean provided = Boolean.parseBoolean(element.attributeValue("provided"));
        if (provided && hostDataHolder == null) {
            throw new IllegalStateException("Host data holder is null");
        }

        return provided;
    }

    protected String generateId() {
        return GENERATED_PREFIX + RandomStringUtils.randomAlphanumeric(8);
    }
}
