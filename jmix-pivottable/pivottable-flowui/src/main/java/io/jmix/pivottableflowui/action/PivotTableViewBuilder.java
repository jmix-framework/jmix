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

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.view.View;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.kit.component.model.*;
import io.jmix.pivottableflowui.view.PivotTableView;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
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

    protected List<Object> items;
    protected PivotTableOptions options;

    protected ListDataComponent<?> target;

    public PivotTableViewBuilder(ListDataComponent<?> target) {
        this.target = target;
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setFetchPlanRepository(FetchPlanRepository fetchPlanRepository) {
        this.fetchPlanRepository = fetchPlanRepository;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
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
        return Objects.requireNonNullElse(includedProperties, Collections.emptyList());
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
        return Objects.requireNonNullElse(excludedProperties, Collections.emptyList());
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
        return Objects.requireNonNullElse(additionalProperties, Collections.emptyList());
    }

    /**
     * Sets items that should be shown in PivotTable.
     *
     * @param items collection of entities
     * @return current instance
     */
    public PivotTableViewBuilder withItems(Collection<?> items) {
        this.items = new ArrayList<>(items);

        return this;
    }

    /**
     * Sets a collection of attribute names to use as rows.
     *
     * @param rows a collection of attribute names to use as rows
     */
    public PivotTableViewBuilder withRows(List<String> rows) {
        getPivotTableOptions().setRows(rows);

        return this;
    }

    /**
     * Sets a collection of attribute names to use as columns.
     *
     * @param columns a collection of attribute names to use as columns
     */
    public PivotTableViewBuilder withColumns(List<String> columns) {
        getPivotTableOptions().setColumns(columns);

        return this;
    }

    /**
     * Original property name: {@code aggregator}.
     * <p>
     * Sets a descriptor of an object which will aggregate results per cell
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
     * <p>
     * Applies only when {@code showUI=false}.
     *
     * @param aggregation an object which will aggregate results per cell
     */
    public PivotTableViewBuilder withAggregation(Aggregation aggregation) {
        getPivotTableOptions().setAggregation(aggregation);

        return this;
    }

    /**
     * Sets a descriptor of an object which will generate output from pivot data structure
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Renderers">documentation</a>).
     * <p>
     * Applies only when {@code showUI=false}.
     *
     * @param renderer an object which will generate output from pivot data structure
     */
    public PivotTableViewBuilder withRenderer(Renderer renderer) {
        getPivotTableOptions().setRenderer(renderer);

        return this;
    }

    /**
     * Original property name: {@code vals}.
     * <p>
     * Sets attribute names to prepopulate in vals area (gets passed to aggregator generating function).
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param aggregationProperties attribute names to prepopulate in vals area
     */
    public PivotTableViewBuilder withAggregationProperties(List<String> aggregationProperties) {
        getPivotTableOptions().setAggregationProperties(aggregationProperties);

        return this;
    }

    /**
     * Original property name: {@code aggregators}.
     * <p>
     * Sets an object that represents a list of generators for aggregation functions in dropdown
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Aggregators">documentation</a>).
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param aggregations an object that represents a list of generators for aggregation functions in dropdown
     */
    public PivotTableViewBuilder withAggregations(Aggregations aggregations) {
        getPivotTableOptions().setAggregations(aggregations);

        return this;
    }

    /**
     * Sets an object that represents a list of rendering functions
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Renderers">documentation</a>).
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param renderers n object that represents a list of rendering functions
     */
    public PivotTableViewBuilder withRenderers(Renderers renderers) {
        getPivotTableOptions().setRenderers(renderers);

        return this;
    }

    /**
     * Sets attribute names to omit from the UI.
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param hiddenProperties attribute names to omit from the UI
     */
    public PivotTableViewBuilder withHiddenProperties(List<String> hiddenProperties) {
        getPivotTableOptions().setHiddenProperties(hiddenProperties);

        return this;
    }

    /**
     * Sets attribute names to omit from the aggregation arguments dropdowns.
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param hiddenFromAggregations attribute names to omit from the aggregation arguments dropdowns
     */
    public PivotTableViewBuilder withHiddenFromAggregations(List<String> hiddenFromAggregations) {
        getPivotTableOptions().setHiddenFromAggregations(hiddenFromAggregations);

        return this;
    }

    /**
     * Sets attribute names to omit from the drag'n'drop portion of the UI.
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param hiddenFromDragDrop attribute names to omit from the drag'n'drop portion of the UI
     */
    public PivotTableViewBuilder withHiddenFromDragDrop(List<String> hiddenFromDragDrop) {
        getPivotTableOptions().setHiddenFromDragDrop(hiddenFromDragDrop);

        return this;
    }

    /**
     * Sets the order in which column data is provided to the renderer.
     * <p>
     * Ordering by value orders by column total.
     *
     * @param columnOrder the order in which column data is provided to the renderer
     */
    public PivotTableViewBuilder withColumnOrder(Order columnOrder) {
        getPivotTableOptions().setColumnOrder(columnOrder);

        return this;
    }

    /**
     * Sets the order in which row data is provided to the renderer.
     * <p>
     * Ordering by value orders by row total.
     *
     * @param rowOrder the order in which row data is provided to the renderer
     */
    public PivotTableViewBuilder withRowOrder(Order rowOrder) {
        getPivotTableOptions().setRowOrder(rowOrder);

        return this;
    }

    /**
     * Sets the maximum number of values to list in the double click menu.
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param menuLimit the maximum number of values to list in the double click menu
     */
    public PivotTableViewBuilder withMenuLimit(Integer menuLimit) {
        getPivotTableOptions().setMenuLimit(menuLimit);

        return this;
    }

    /**
     * Original property name: {@code autoSortUnusedAttrs}.
     * <p>
     * Sets whether unused attributes are kept sorted in the UI.
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param autoSortUnusedProperties whether unused attributes are kept sorted in the UI
     */
    public PivotTableViewBuilder withAutoSortUnusedProperties(Boolean autoSortUnusedProperties) {
        getPivotTableOptions().setAutoSortUnusedProperties(autoSortUnusedProperties);

        return this;
    }

    /**
     * Original property name: {@code unusedAttrsVertical}.
     * <p>
     * Sets whether unused attributes are shown vertically
     * instead of the default which is horizontally. {@code true} means
     * always vertical, {@code false} means always horizontal. If set to
     * a number (as is the default) then if the attributes' names' combined
     * length in characters exceeds the number then the attributes will be shown vertically.
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param unusedPropertiesVertical whether unused attributes are shown vertically
     */
    public PivotTableViewBuilder withUnusedPropertiesVertical(UnusedPropertiesVertical unusedPropertiesVertical) {
        getPivotTableOptions().setUnusedPropertiesVertical(unusedPropertiesVertical);

        return this;
    }

    /**
     * Original property name: {@code filter}.
     * <p>
     * Sets a filter function that is called on each record, returns {@code false} if the record
     * is to be excluded from the input before rendering or {@code true} otherwise.
     *
     * @param filter a filter function that is called on each record
     */
    public PivotTableViewBuilder withFilterFunction(JsFunction filter) {
        getPivotTableOptions().setFilterFunction(filter);

        return this;
    }

    /**
     * Original property name: {@code sorters}.
     * <p>
     * Sets a sorter function that is called with an attribute name and can return
     * a function which can be used as an argument to {@code Array.sort} for output
     * purposes. If no function is returned, the default sorting mechanism is a built-in
     * "natural sort" implementation. Useful for sorting attributes like month names.
     *
     * @param sorters a sorter function
     */
    public PivotTableViewBuilder withSortersFunction(JsFunction sorters) {
        getPivotTableOptions().setSortersFunction(sorters);

        return this;
    }

    /**
     * Sets an object that is passed through to renderer as options.
     *
     * @param rendererOptions an object that is passed through to renderer as options
     */
    public PivotTableViewBuilder withRendererOptions(RendererOptions rendererOptions) {
        getPivotTableOptions().setRendererOptions(rendererOptions);

        return this;
    }

    /**
     * Sets a map whose keys are attribute names and values are arrays of attribute values
     * which denote records to include in rendering; used to prepopulate the filter menus
     * that appear on double-click (overrides {@link #withExclusions(Map)}).
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param inclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #withExclusions(Map)
     */
    public PivotTableViewBuilder withInclusions(Map<String, List<String>> inclusions) {
        getPivotTableOptions().setInclusions(inclusions);

        return this;
    }

    /**
     * Sets a map whose keys are attribute names and values are arrays of attribute values
     * which denote records to exclude from rendering; used to prepopulate the filter menus
     * that appear on double-click.
     * <p>
     * Applies only when {@code showUI=true}.
     *
     * @param exclusions a map whose keys are attribute names and values are arrays of attribute values
     * @see #withInclusions(Map)
     */
    public PivotTableViewBuilder withExclusions(Map<String, List<String>> exclusions) {
        getPivotTableOptions().setExclusions(exclusions);

        return this;
    }

    /**
     * Original property name: {@code derivedAttributes}.
     * <p>
     * Sets an object that represents derived properties
     * (see <a href="https://github.com/nicolaskruchten/pivottable/wiki/Derived-Attributes">documentation</a>).
     *
     * @param derivedProperties an object that represents derived properties
     */
    public PivotTableViewBuilder withDerivedProperties(DerivedProperties derivedProperties) {
        getPivotTableOptions().setDerivedProperties(derivedProperties);

        return this;
    }

    /**
     * If component doesn't have data to aggregate, the message will be displayed
     *
     * @param emptyDataMessage string with an empty data message
     */
    public PivotTableViewBuilder withEmptyDataMessage(String emptyDataMessage) {
        getPivotTableOptions().setEmptyDataMessage(emptyDataMessage);

        return this;
    }

    /**
     * Shows or hides UI.
     *
     * @param showUI show UI option
     */
    public PivotTableViewBuilder withShowUI(Boolean showUI) {
        getPivotTableOptions().setShowUI(showUI);

        return this;
    }

    /**
     * Shows or hides row totals. {@code true} by default.
     *
     * @param showRowTotals row totals option
     */
    public PivotTableViewBuilder withShowRowTotals(Boolean showRowTotals) {
        getPivotTableOptions().setShowUI(showRowTotals);

        return this;
    }

    /**
     * Shows or hides col totals. {@code true} by default.
     *
     * @param showColumnTotals column total options
     */
    public PivotTableViewBuilder withShowColumnTotals(Boolean showColumnTotals) {
        getPivotTableOptions().setShowColumnTotals(showColumnTotals);

        return this;
    }

    /**
     * Navigate to {@link PivotTableView} and show {@link PivotTable} component with the set parameters
     */
    public void show() {
        if (target == null) {
            throw new IllegalStateException(String.format("%s is not set", ListDataComponent.class.getSimpleName()));
        }

        View<?> targetView = UiComponentUtils.getView((com.vaadin.flow.component.Component) target);

        PivotTableOptions pivotTableOptions = getPivotTableOptions();
        pivotTableOptions.setProperties(getPropertiesWithLocale());
        viewNavigators.view(targetView, PivotTableView.class)
                .withAfterNavigationHandler(event -> {
                    PivotTableView pivotTableView = event.getView();
                    pivotTableView.setPivotTableOptions(pivotTableOptions);
                    pivotTableView.setDataItems(items == null ? Collections.emptyList() : items);
                })
                .navigate();
    }

    protected Map<String, String> getPropertiesWithLocale() {
        Map<String, String> resultMap = new HashMap<>();

        ContainerDataUnit<?> containerDataUnit = (ContainerDataUnit<?>) target.getItems();
        if (containerDataUnit == null) {
            return resultMap;
        }

        FetchPlan fetchPlan = containerDataUnit.getContainer().getFetchPlan();
        MetaClass metaClass = containerDataUnit.getEntityMetaClass();
        if (fetchPlan == null) {
            fetchPlan = getBaseFetchPlan(metaClass);
        }

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

            if (propertyMetaClass != null && !isManagedProperty(metaProperty, propertyMetaClass)) {
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

        // id is a system property, so it should be checked before isSystem() checking
        if (isIdProperty(metaProperty.getName(), metaClass)) {
            return true;
        }

        return !metadataTools.isSystem(metaProperty);
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
                return result;
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

    @Nullable
    protected MetaClass getEmbeddedIdMetaClass(MetaClass metaClass) {
        String primaryKey = metadataTools.getPrimaryKeyName(metaClass);
        if (primaryKey == null) {
            return null;
        }

        MetaProperty metaProperty = metaClass.getProperty(primaryKey);

        // in this case we should use `metaProperty.getJavaType()` because
        // we need to get class type of EmbeddedId property and then get MetaClass of it
        return MetaProperty.Type.EMBEDDED == metaProperty.getType()
                ? metadata.getClass(metaProperty.getJavaType())
                : null;
    }

    protected List<String> removeNonExistingProperties(List<String> properties, MetaClass metaClass, FetchPlan fetchPlan) {
        if (!metadataTools.isJpaEntity(metaClass)) {
            return properties.stream()
                    .filter(s -> metaClass.getPropertyPath(s) != null)
                    .collect(Collectors.toList());
        }

        List<String> result = new ArrayList<>();
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

    @Nullable
    protected MetaClass getPropertyMetaClass(MetaProperty metaProperty) {
        Class<?> declaringClass = metaProperty.getDeclaringClass();
        if (declaringClass == null) {
            return null;
        }
        return metadata.getClass(declaringClass);
    }

    protected PivotTableOptions getPivotTableOptions() {
        if (options == null) {
            options = new PivotTableOptions();
        }
        return options;
    }
}