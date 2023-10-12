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

package io.jmix.flowui.model.impl;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.impl.FetchPlanLoader;
import io.jmix.core.impl.FetchPlanLoader.FetchPlanInfo;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.ConditionXmlLoader;
import io.jmix.flowui.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component("flowui_ViewDataXmlLoader")
public class ViewDataXmlLoader {

    public static final String GENERATED_PREFIX = "generated_";

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected FetchPlanLoader fetchPlanLoader;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected DataComponents factory;

    @Autowired
    protected ConditionXmlLoader conditionXmlLoader;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    public void load(ViewData viewData, Element element) {
        Preconditions.checkNotNullArgument(viewData, ViewData.class.getSimpleName() + " is null");
        Preconditions.checkNotNullArgument(element, "Element is null");

        boolean readOnly = Boolean.parseBoolean(element.attributeValue("readOnly"));
        DataContext dataContext = readOnly ? new NoopDataContext(applicationContext) : factory.createDataContext();
        viewData.setDataContext(dataContext);

        for (Element el : element.elements()) {
            switch (el.getName()) {
                case "collection":
                    loadCollectionContainer(viewData, el);
                    break;
                case "instance":
                    loadInstanceContainer(viewData, el);
                    break;
                case "keyValueCollection":
                    loadKeyValueCollectionContainer(viewData, el);
                    break;
                case "keyValueInstance":
                    loadKeyValueInstanceContainer(viewData, el);
                    break;
                default:
                    // no action
                    break;
            }
        }
    }

    protected void loadInstanceContainer(ViewData viewData, Element element) {
        String containerId = getRequiredAttr(element, "id");

        InstanceContainer<Object> container = factory.createInstanceContainer(getEntityClass(element));
        loadFetchPlan(element, getEntityClass(element), container);

        viewData.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadInstanceLoader(viewData, loaderEl, container);
        }

        for (Element collectionEl : element.elements()) {
            loadNestedContainer(viewData, collectionEl, container);
        }
    }

    protected void loadCollectionContainer(ViewData viewData, Element element) {
        String containerId = getRequiredAttr(element, "id");

        CollectionContainer<Object> container = factory.createCollectionContainer(getEntityClass(element));
        loadFetchPlan(element, getEntityClass(element), container);

        viewData.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadCollectionLoader(viewData, loaderEl, container);
        }

        for (Element collectionEl : element.elements()) {
            loadNestedContainer(viewData, collectionEl, container);
        }
    }

    protected void loadKeyValueCollectionContainer(ViewData viewData, Element element) {
        String containerId = getRequiredAttr(element, "id");

        KeyValueCollectionContainer container = factory.createKeyValueCollectionContainer();
        loadProperties(element, container);

        String idName = element.attributeValue("idName");
        if (!Strings.isNullOrEmpty(idName)) {
            container.setIdName(idName);
        }

        viewData.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadKeyValueCollectionLoader(viewData, loaderEl, container);
        }
    }

    protected void loadKeyValueInstanceContainer(ViewData viewData, Element element) {
        String containerId = getRequiredAttr(element, "id");

        KeyValueContainer container = factory.createKeyValueContainer();
        loadProperties(element, container);

        String idName = element.attributeValue("idName");
        if (!Strings.isNullOrEmpty(idName))
            container.setIdName(idName);

        viewData.registerContainer(containerId, container);

        Element loaderEl = element.element("loader");
        if (loaderEl != null) {
            loadKeyValueInstanceLoader(viewData, loaderEl, container);
        }
    }

    private void loadProperties(Element element, KeyValueContainer container) {
        Element propsEl = element.element("properties");
        if (propsEl != null) {
            for (Element propEl : propsEl.elements()) {
                String name = propEl.attributeValue("name");
                String className = propEl.attributeValue("class");
                if (className != null) {
                    container.addProperty(name, ReflectionHelper.getClass(className));
                } else {
                    String typeName = propEl.attributeValue("datatype");
                    Datatype<?> datatype = typeName == null
                            ? datatypeRegistry.get(String.class)
                            : datatypeRegistry.find(typeName);
                    container.addProperty(name, datatype);
                }
            }
            String idProperty = propsEl.attributeValue("idProperty");
            if (idProperty != null) {
                if (container.getEntityMetaClass().findProperty(idProperty) == null) {
                    throw new DevelopmentException(String.format("Property '%s' is not defined", idProperty));
                }
                container.setIdName(idProperty);
            }
        }
    }

    protected void loadNestedContainer(ViewData viewData, Element element, InstanceContainer<?> masterContainer) {
        if (!element.getName().equals("collection") && !element.getName().equals("instance"))
            return;

        String containerId = getRequiredAttr(element, "id");

        String property = getRequiredAttr(element, "property");
        MetaProperty metaProperty = masterContainer.getEntityMetaClass().getProperty(property);

        InstanceContainer<?> nestedContainer = null;

        if (element.getName().equals("collection")) {
            if (!metaProperty.getRange().isClass() || !metaProperty.getRange().getCardinality().isMany()) {
                throw new IllegalStateException(String.format(
                        "Cannot bind collection container '%s' to a non-collection property '%s'", containerId, property));
            }
            nestedContainer = factory.createCollectionContainer(
                    metaProperty.getRange().asClass().getJavaClass(), masterContainer, property);

        } else if (element.getName().equals("instance")) {
            if (!metaProperty.getRange().isClass() || metaProperty.getRange().getCardinality().isMany()) {
                throw new IllegalStateException(String.format(
                        "Cannot bind instance container '%s' to a non-reference property '%s'", containerId, property));
            }
            nestedContainer = factory.createInstanceContainer(
                    metaProperty.getRange().asClass().getJavaClass(), masterContainer, property);
        }

        if (nestedContainer != null) {
            viewData.registerContainer(containerId, nestedContainer);

            for (Element collectionEl : element.elements()) {
                loadNestedContainer(viewData, collectionEl, nestedContainer);
            }
        }
    }

    protected void loadInstanceLoader(ViewData viewData, Element element, InstanceContainer<Object> container) {
        String loaderId = element.attributeValue("id");
        if (loaderId == null) {
            loaderId = generateId();
        }

        InstanceLoader<Object> loader = factory.createInstanceLoader();

        if (!Boolean.parseBoolean(element.attributeValue("readOnly"))) {
            loader.setDataContext(viewData.getDataContextOrNull());
        }
        loader.setContainer(container);

        loadAdditionalLoaderProperties(element, loader);
        loadQuery(element, loader);
        loadEntityId(element, loader);

        viewData.registerLoader(loaderId, loader);
    }

    protected void loadCollectionLoader(ViewData viewData, Element element,
                                        CollectionContainer<Object> container) {
        String loaderId = element.attributeValue("id");
        if (loaderId == null) {
            loaderId = generateId();
        }

        CollectionLoader<Object> loader = createCollectionLoader(element);

        if (!Boolean.parseBoolean(element.attributeValue("readOnly"))) {
            loader.setDataContext(viewData.getDataContextOrNull());
        }
        loader.setContainer(container);

        loadQuery(element, loader);
        loadAdditionalLoaderProperties(element, loader);
        loadFirstResult(element, loader);
        loadMaxResults(element, loader);
        loadCacheable(element, loader);

        viewData.registerLoader(loaderId, loader);
    }

    protected CollectionLoader<Object> createCollectionLoader(Element element) {
        return factory.createCollectionLoader();
    }

    protected void loadKeyValueCollectionLoader(ViewData viewData, Element element,
                                                KeyValueCollectionContainer container) {
        String loaderId = element.attributeValue("id");
        if (loaderId == null) {
            loaderId = generateId();
        }

        KeyValueCollectionLoader loader = factory.createKeyValueCollectionLoader();
        loader.setContainer(container);

        loadQuery(element, loader);
        loadAdditionalLoaderProperties(element, loader);
        loadFirstResult(element, loader);
        loadMaxResults(element, loader);

        String storeName = element.attributeValue("store");
        if (!Strings.isNullOrEmpty(storeName)) {
            loader.setStoreName(storeName);
        }

        viewData.registerLoader(loaderId, loader);
    }

    protected void loadKeyValueInstanceLoader(ViewData viewData, Element element, KeyValueContainer container) {
        String loaderId = element.attributeValue("id");
        if (loaderId == null) {
            loaderId = generateId();
        }

        KeyValueInstanceLoader loader = factory.createKeyValueInstanceLoader();
        loader.setContainer(container);

        loadQuery(element, loader);
        loadAdditionalLoaderProperties(element, loader);

        String storeName = element.attributeValue("store");
        if (!Strings.isNullOrEmpty(storeName)) {
            loader.setStoreName(storeName);
        }

        viewData.registerLoader(loaderId, loader);
    }

    protected Class<Object> getEntityClass(Element element) {
        String entityClassName = getRequiredAttr(element, "class");
        return ReflectionHelper.getClass(entityClassName);
    }

    protected void loadFetchPlan(Element element, Class<Object> entityClass, InstanceContainer<Object> container) {

        Element fetchPlanElement = element.element("fetchPlan");
        if (fetchPlanElement == null) {
            fetchPlanElement = element.element("view");
        }
        if (fetchPlanElement != null) {
            container.setFetchPlan(loadInlineFetchPlan(fetchPlanElement, entityClass));
            return;
        }

        String fetchPlanName = element.attributeValue("fetchPlan");
        if (fetchPlanName == null) {
            fetchPlanName = element.attributeValue("view");
        }
        if (fetchPlanName != null) {
            container.setFetchPlan(fetchPlanRepository.getFetchPlan(entityClass, fetchPlanName));
        }
    }

    protected FetchPlan loadInlineFetchPlan(Element viewElem, Class<?> entityClass) {
        FetchPlanInfo viewInfo = fetchPlanLoader.getFetchPlanInfo(viewElem, metadata.getClass(entityClass));

        FetchPlanBuilder builder = fetchPlanLoader.getFetchPlanBuilder(viewInfo, a ->
                fetchPlanRepository.getFetchPlan(viewInfo.getMetaClass(), a));

        fetchPlanLoader.loadFetchPlanProperties(viewElem, builder,
                viewInfo.isSystemProperties(), (metaClass, viewName) ->
                        fetchPlanRepository.getFetchPlan(metaClass, viewName));

        return builder.build();
    }

    protected void loadQuery(Element element, DataLoader loader) {
        Element queryEl = element.element("query");
        if (queryEl != null) {
            loader.setQuery(loadQueryText(queryEl));
            Element conditionEl = queryEl.element("condition");
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
    }

    protected String loadQueryText(Element queryEl) {
        return queryEl.getText().trim();
    }

    protected void loadAdditionalLoaderProperties(Element element, DataLoader loader) {
    }

    protected void loadEntityId(Element element, InstanceLoader<Object> loader) {
        String entityIdStr = element.attributeValue("entityId");
        if (Strings.isNullOrEmpty(entityIdStr)) {
            return;
        }

        MetaProperty property = metadataTools.getPrimaryKeyProperty(loader.getContainer().getEntityMetaClass());
        if (property == null) {
            throw new IllegalStateException("Cannot determine id property for " +
                    loader.getContainer().getEntityMetaClass());
        }

        if (property.getRange().isDatatype()) {
            try {
                Object value = property.getRange().asDatatype().parse(entityIdStr);
                loader.setEntityId(value);
            } catch (ParseException e) {
                throw new RuntimeException("Error parsing entityId for " + loader, e);
            }
        } else {
            throw new IllegalStateException("Cannot assign id to " + loader + " because the entity has a composite PK");
        }
    }

    protected void loadFirstResult(Element element, BaseCollectionLoader loader) {
        String firstResultStr = element.attributeValue("firstResult");
        if (Strings.isNullOrEmpty(firstResultStr))
            return;

        loader.setFirstResult(Integer.parseInt(firstResultStr));
    }

    protected void loadMaxResults(Element element, BaseCollectionLoader loader) {
        String maxResultsStr = element.attributeValue("maxResults");
        if (Strings.isNullOrEmpty(maxResultsStr)) {
            return;
        }

        loader.setMaxResults(Integer.parseInt(maxResultsStr));
    }

    protected void loadCacheable(Element element, CollectionLoader<Object> loader) {
        String cacheableVal = element.attributeValue("cacheable");
        if (!Strings.isNullOrEmpty(cacheableVal)) {
            loader.setCacheable(Boolean.parseBoolean(cacheableVal));
        }
    }

    protected String getRequiredAttr(Element element, String attributeName) {
        String id = element.attributeValue(attributeName);
        if (id == null) {
            throw new IllegalStateException("Required attribute '" + attributeName + "' not found in " + element);
        }
        return id.trim();
    }

    protected String generateId() {
        return GENERATED_PREFIX + RandomStringUtils.randomAlphanumeric(8);
    }
}
