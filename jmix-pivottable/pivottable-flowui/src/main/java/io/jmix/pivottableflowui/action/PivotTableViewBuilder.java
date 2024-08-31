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

package io.jmix.pivottableflowui.action;

import com.nimbusds.jose.shaded.gson.JsonSyntaxException;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.pivottableflowui.data.item.EntityDataItem;
import io.jmix.pivottableflowui.export.view.PivotTableView;
import io.jmix.pivottableflowui.kit.data.DataItem;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Prepares data and builds a view with pivot table component.
 *
 * @see ShowPivotTableAction
 */
@Component("pvttbl_PivotTableViewBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PivotTableViewBuilder {

    protected ViewNavigators viewNavigators;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected FetchPlanRepository fetchPlanRepository;
    protected Messages messages;
    protected MessageTools messageTools;
    protected AccessManager accessManager;

    protected List<String> includedProperties;
    protected List<String> excludedProperties;
    protected List<String> additionalProperties;

    protected List<DataItem> dataItems;
    protected String nativeJson;

    protected ListDataComponent target;

    public PivotTableViewBuilder(ListDataComponent target) {
        this.target = target;
    }

    @Autowired
    protected void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Autowired
    protected void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    protected void setFetchPlanRepository(FetchPlanRepository fetchPlanRepository) {
        this.fetchPlanRepository = fetchPlanRepository;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    /**
     * @return list of included properties
     */
    public List<String> getIncludedProperties() {
        if (includedProperties == null) {
            return Collections.emptyList();
        }
        return includedProperties;
    }

    /**
     * Set included properties list using fluent API method. If included properties aren't set, all properties in the
     * fetch plan will be shown, otherwise only included properties will be shown in the pivot table unless
     * {@link ShowPivotTableAction#setExcludedProperties(String)} is not set.
     *
     * @param includedProperties list of included properties
     * @return current instance of action
     */
    public PivotTableViewBuilder withIncludedProperties(List<String> includedProperties) {
        this.includedProperties = includedProperties;

        return this;
    }

    /**
     * @return list of excluded properties
     */
    public List<String> getExcludedProperties() {
        if (excludedProperties == null) {
            return Collections.emptyList();
        }
        return excludedProperties;
    }

    /**
     * Set excluded properties list using fluent API method.
     * <br>
     * Note, if it is used without {@link ShowPivotTableAction#setExcludedProperties(String)}, excluded properties will be
     * applied for all properties in the fetch plan.
     *
     * @param excludedProperties list of excluded properties
     * @return current instance
     */
    public PivotTableViewBuilder withExcludedProperties(List<String> excludedProperties) {
        this.excludedProperties = excludedProperties;

        return this;
    }

    /**
     * Set properties which should be additionally included. Additional property doesn't applied if excluded
     * properties list contains it.
     *
     * @param additionalProperties list of additional properties
     * @return current instance of action
     */
    public PivotTableViewBuilder withAdditionalProperties(List<String> additionalProperties) {
        this.additionalProperties = additionalProperties;

        return this;
    }

    /**
     * @return list of additionally included properties
     */
    public List<String> getAdditionalProperties() {
        if (additionalProperties == null) {
            return Collections.emptyList();
        }
        return additionalProperties;
    }

    /**
     * @return configuration json of pivot table
     */
    public String getNativeJson() {
        return nativeJson;
    }

    /**
     * Set native json using fluent API method. Using native json you can configure pivot table with initial values.
     * For instance, for non-editable pivot table:
     * <pre> {@code
     * {
     * 	"cols": ["localized property", "localized property"],
     * 	"rows": ["localized property"],
     * 	"editable": false,
     * 	"renderer": "heatmap",
     * 	"aggregation": {
     * 		"id": "d8fc3fdf-730d-c94f-a0c8-72a9ce3dcb3a",
     * 		"mode": "sumOverSum",
     * 		"properties": ["localized property", "localized property"]
     *    }
     * }
     * }
     * </pre>
     * for editable pivot table:
     * <pre> {@code
     * {
     * 	"cols": ["localized property"],
     * 	"rows": ["localized property"],
     * 	"editable": true,
     * 	"renderers": {
     * 		"selectedRenderer": "barChart"
     *    },
     * 	"autoSortUnusedProperties": true,
     * 	"aggregationProperties": ["localized property", "localized property"],
     * 	"aggregations": {
     * 		"selectedAggregation": "count",
     * 		"aggregations": [{
     * 			"id": "647780f0-c6d0-6ade-a63a-542b5c8cdbd5",
     * 			"mode": "count",
     * 			"caption": "Count"
     *        }, {
     * 			"id": "c2663238-2654-67f0-2dec-add6962d867c",
     * 			"mode": "sumOverSum"
     *        }]
     *    }
     * }
     * }
     * </pre>
     *
     * @param nativeJson configuration json of pivot table
     * @return current instance of action
     */
    public PivotTableViewBuilder withNativeJson(String nativeJson) {
        if (nativeJson != null) {
            try {
//                JsonParser parser = new JsonParser();
//                parser.parse(nativeJson);
            } catch (JsonSyntaxException e) {
                throw new IllegalStateException("Unable to parse JSON chart configuration");
            }
        }

        this.nativeJson = nativeJson;

        return this;
    }

    /**
     * Sets items that should be shown in PivotTable.
     *
     * @param items collection of entities
     * @return current instance
     */
    public PivotTableViewBuilder withItems(Collection<? extends Entity> items) {
        dataItems = new ArrayList<>(items.size());
        for (Entity entity : items) {
            dataItems.add(new EntityDataItem(entity));
        }
        return this;
    }

    public void build() {
        if (!(target instanceof Grid<?> grid) || UiComponentUtils.findView(grid) == null) {
            throw new IllegalStateException(
                    String.format("Component '%s' is null or not added to a view", ((Grid) target).getId()));
        }

        if (dataItems == null) {
            dataItems = Collections.emptyList();
        }

        Map<String, String> properties = getPropertiesWithLocale();

        viewNavigators.view(UiComponentUtils.findView((Grid) target), PivotTableView.class)
                .withAfterNavigationHandler(event -> {
                    PivotTableView pivotTableView = event.getView();
                    pivotTableView.setProperties(properties);
                    pivotTableView.setDataItems(dataItems);
                })
                .navigate();
    }

    protected Map<String, String> getPropertiesWithLocale() {
        Map<String, String> resultMap = new HashMap<>();

        ContainerDataUnit containerDataUnit = (ContainerDataUnit) target.getItems();
        FetchPlan fetchPlan = containerDataUnit.getContainer().getFetchPlan();
        MetaClass metaClass = containerDataUnit.getEntityMetaClass();

        List<String> appliedProperties = includedProperties == null ?
                new ArrayList<>() : new ArrayList<>(includedProperties);

        if (appliedProperties.isEmpty()) {
            appliedProperties.addAll(getPropertiesFromView(metaClass, fetchPlan));
            appliedProperties.addAll(getEmbeddedIdProperties(metaClass));
        }

        appliedProperties.addAll(additionalProperties == null ?
                Collections.emptyList() : additionalProperties);

        // exclude properties
        if (!CollectionUtils.isEmpty(excludedProperties)) {
            appliedProperties.removeAll(excludedProperties);
        }

        appliedProperties = removeNonExistingProperties(appliedProperties, metaClass, fetchPlan);

        for (String property : appliedProperties) {
            MetaPropertyPath mpp = metaClass.getPropertyPath(property);
            if (mpp == null) {
                continue;
            }

            MetaProperty metaProperty = mpp.getMetaProperty();
            Class<?> declaringClass = metaProperty.getDeclaringClass();
            MetaClass propertyMetaClass = declaringClass == null ? null : metadata.getClass(declaringClass);

            if (!isManagedProperty(metaProperty, propertyMetaClass)) {
                continue;
            }

            resultMap.put(property, messageTools.getPropertyCaption(metaProperty));
        }

        return resultMap;
    }

    protected boolean isManagedProperty(MetaProperty metaProperty, MetaClass metaClass) {
        if (metadataTools.isSystemLevel(metaProperty)
                || metaProperty.getRange().getCardinality().isMany()
                || !isPermitted(metaClass, metaProperty)) {
            return false;
        }

        if (metaProperty.getRange().isDatatype() && (isByteArray(metaProperty) || isUuid(metaProperty))) {
            return false;
        }

        // id is system property, so it should be checked before isSystem() checking
        if (isIdProperty(metaProperty.getName(), metaClass)) {
            return true;
        }

        if (metadataTools.isSystem(metaProperty)) {
            return false;
        }

        return true;
    }

    protected boolean isPermitted(MetaClass metaClass, MetaProperty metaProperty) {
        UiEntityAttributeContext attributeContext =
                new UiEntityAttributeContext(metaClass, metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        return attributeContext.canView();
    }

    protected boolean isByteArray(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(byte[].class);
    }

    protected boolean isUuid(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(UUID.class);
    }

    protected List<String> getPropertiesFromView(MetaClass metaClass, FetchPlan fetchPlan) {
        if (!metadataTools.isJpaEntity(metaClass)) {
            return metaClass.getProperties().stream()
                    .map(MetaProperty::getName)
                    .collect(Collectors.toList());
        }

        fetchPlan = fetchPlan == null ? getBaseFetchPlan(metaClass) : fetchPlan;
        if (fetchPlan.getProperties().isEmpty()) {
            return Collections.emptyList();
        }

        return fetchPlan.getProperties().stream()
                .map(FetchPlanProperty::getName)
                .collect(Collectors.toList());
    }

    protected FetchPlan getBaseFetchPlan(MetaClass metaClass) {
        return fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.BASE);
    }

    protected List<String> getEmbeddedIdProperties(MetaClass metaClass) {
        List<String> result = new ArrayList<>();

        if (hasEmbeddedId(metaClass)) {
            MetaClass embeddedMetaClass = getEmbeddedIdMetaClass(metaClass);
            if (embeddedMetaClass == null) {
                return null;
            }

            String primaryKey = metadataTools.getPrimaryKeyName(metaClass);
            for (MetaProperty metaProperty : embeddedMetaClass.getOwnProperties()) {
                result.add(primaryKey + "." + metaProperty.getName());
            }
        }
        return result;
    }

    protected boolean hasEmbeddedId(MetaClass metaClass) {
        return metadataTools.hasCompositePrimaryKey(metaClass);
    }

    protected MetaClass getEmbeddedIdMetaClass(MetaClass metaClass) {
        String primaryKey = metadataTools.getPrimaryKeyName(metaClass);
        if (primaryKey == null) {
            return null;
        }

        MetaProperty metaProperty = metaClass.getProperty(primaryKey);
        // in this case we should use `metaProperty.getJavaType()` because
        // we need to get class type of EmbeddedId property and then get MetaClass of it
        return metadataTools.isEmbedded(metaProperty) ? metadata.getClass(metaProperty.getJavaType()) : null;
    }

    protected List<String> removeNonExistingProperties(List<String> properties, MetaClass metaClass, FetchPlan fetchPlan) {
        if (!metadataTools.isJpaEntity(metaClass)) {
            return properties.stream()
                    .filter(s -> metaClass.getPropertyPath(s) != null)
                    .collect(Collectors.toList());
        }

        List<String> result = new ArrayList<>();
        fetchPlan = fetchPlan == null ? getBaseFetchPlan(metaClass) : fetchPlan;

        for (String property : properties) {
            MetaPropertyPath mpp = metaClass.getPropertyPath(property);
            // is nested property and fetch plan contains it

            if (mpp != null && metadataTools.fetchPlanContainsProperty(fetchPlan, mpp)) {
                result.add(property);
                // simple property
            } else if (fetchPlan.containsProperty(property)) {
                result.add(property);
                // id property
            } else if (isIdProperty(property, metaClass)) {
                result.add(property);
                // EmbeddedId's property
            } else if (hasEmbeddedId(metaClass)
                    && isEmbeddedIdProperty(property, metaClass)) {
                result.add(property);
                // if metaClass contains property path, we need to check nested entities in fetch plan
            } else if (mpp != null) {
                for (MetaProperty metaProperty : mpp.getMetaProperties()) {
                    MetaClass propertyMetaClass = getPropertyMetaClass(metaProperty);
                    if (propertyMetaClass == null) {
                        propertyMetaClass = metaClass;
                    }
                    // EmbeddedId's property
                    if (isEmbeddedIdProperty(property, propertyMetaClass)) {
                        result.add(property);
                        // Id property
                    } else if (isIdProperty(metaProperty.getName(), propertyMetaClass)) {
                        result.add(property);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks if current MetaClass contains given id property.
     *
     * @param property  property to check
     * @param metaClass metaClass
     * @return true if MetaClass contains given id property
     */
    protected boolean isIdProperty(String property, MetaClass metaClass) {
        String primaryId = metadataTools.getPrimaryKeyName(metaClass);
        return property.equals(primaryId);
    }

    protected boolean isEmbeddedIdProperty(String property, MetaClass metaClass) {
        MetaClass embeddedMetaClass = getEmbeddedIdMetaClass(metaClass);
        if (embeddedMetaClass == null) {
            return false;
        }

        String[] propertyPathParts = property.split("\\.");
        String propertyName = propertyPathParts[propertyPathParts.length - 1];

        MetaProperty metaProperty = embeddedMetaClass.getProperty(propertyName);

        return embeddedMetaClass.getOwnProperties().contains(metaProperty);
    }

    protected MetaClass getPropertyMetaClass(MetaProperty metaProperty) {
        Class<?> declaringClass = metaProperty.getDeclaringClass();
        if (declaringClass == null) {
            return null;
        }
        return metadata.getClass(declaringClass);
    }
}

