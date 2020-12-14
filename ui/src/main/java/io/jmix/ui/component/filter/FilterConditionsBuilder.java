/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.filter;

import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.ui.UiProperties;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.SupportsCaptionPosition;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.HeaderFilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.ui.entity.PropertyFilterCondition;
import io.jmix.ui.entity.PropertyFilterValueComponent;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
@Internal
@Component("ui_FilterConditionsBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FilterConditionsBuilder {

    @Autowired
    protected FilterComponents filterComponents;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected AccessManager accessManager;

    public List<FilterCondition> createFilterConditionsByMetaClass(MetaClass rootEntityMetaClass,
                                                                   @Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate) {
        HeaderFilterCondition propertiesHeaderCondition = createHeaderFilterCondition(
                messages.getMessage(FilterAddConditionAction.class, "addConditionAction.properties"));

        List<FilterCondition> conditions = createFilterConditionsByMetaClass(rootEntityMetaClass, rootEntityMetaClass,
                0, "", propertiesHeaderCondition, propertiesFilterPredicate);
        conditions.sort((condition1, condition2) ->
                ObjectUtils.compare(condition1.getCaption(), condition2.getCaption()));

        if (!conditions.isEmpty()) {
            conditions.add(0, propertiesHeaderCondition);
        }

        return conditions;
    }

    public List<FilterCondition> createFilterConditionsByComponents(List<FilterComponent> components, Filter filter) {
        List<FilterCondition> conditions = new ArrayList<>();

        HeaderFilterCondition conditionsHeaderCondition = createHeaderFilterCondition(
                messages.getMessage(FilterAddConditionAction.class, "addConditionAction.conditions"));
        conditions.add(conditionsHeaderCondition);

        for (FilterComponent component : components) {
            FilterConverter converter =
                    filterComponents.getConverterByComponentClass(component.getClass(), filter);
            FilterCondition condition = converter.convertToModel(component);
            condition.setParent(conditionsHeaderCondition);
            conditions.add(condition);

            if (condition instanceof LogicalFilterCondition) {
                List<FilterCondition> children = createFilterConditionsByLogicalFilterCondition(
                        (LogicalFilterCondition) condition, condition, true);
                conditions.addAll(children);
            }
        }

        return conditions;
    }

    public List<FilterCondition> createFilterConditionsByConfigurations(List<Filter.Configuration> configurations) {
        List<FilterCondition> conditions = new ArrayList<>();

        HeaderFilterCondition configurationsHeaderCondition = createHeaderFilterCondition(
                messages.getMessage(FilterAddConditionAction.class, "addConditionAction.configurations"));
        conditions.add(configurationsHeaderCondition);

        for (Filter.Configuration configuration : configurations) {
            conditions.addAll(createFilterConditionsByConfiguration(configuration,
                    configurationsHeaderCondition, true));
        }

        return conditions;
    }

    public List<FilterCondition> createFilterConditionsByConfiguration(Filter.Configuration configuration,
                                                                       @Nullable FilterCondition parent,
                                                                       boolean addHeaderCondition) {
        List<FilterCondition> conditions = new ArrayList<>();

        LogicalFilterComponent rootLogicalFilterComponent = configuration.getRootLogicalFilterComponent();
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(),
                configuration.getOwner());
        LogicalFilterCondition rootGroupCondition =
                (LogicalFilterCondition) converter.convertToModel(rootLogicalFilterComponent);

        FilterCondition configurationHeaderCondition = null;
        if (addHeaderCondition) {
            configurationHeaderCondition = createHeaderFilterCondition(configuration.getCaption());
            configurationHeaderCondition.setParent(parent);
            conditions.add(configurationHeaderCondition);
        }

        List<FilterCondition> groupConditions = createFilterConditionsByLogicalFilterCondition(rootGroupCondition,
                addHeaderCondition ? configurationHeaderCondition : parent,
                true);
        conditions.addAll(groupConditions);

        return conditions;
    }

    public List<FilterCondition> createFilterConditionsByLogicalFilterCondition(LogicalFilterCondition logicalFilterCondition,
                                                                                FilterCondition parent,
                                                                                boolean isRootGroupFilterComponent) {
        List<FilterCondition> conditions = new ArrayList<>();

        if (!isRootGroupFilterComponent) {
            logicalFilterCondition.setParent(parent);
            conditions.add(logicalFilterCondition);
        }

        if (logicalFilterCondition.getOwnFilterConditions() != null) {
            for (FilterCondition ownFilterCondition : logicalFilterCondition.getOwnFilterConditions()) {
                FilterCondition parentCondition = isRootGroupFilterComponent ? parent : logicalFilterCondition;
                if (ownFilterCondition instanceof LogicalFilterCondition) {
                    List<FilterCondition> children = createFilterConditionsByLogicalFilterCondition(
                            (LogicalFilterCondition) ownFilterCondition,
                            parentCondition,
                            false);
                    conditions.addAll(children);
                } else {
                    ownFilterCondition.setParent(parentCondition);
                    conditions.add(ownFilterCondition);
                }
            }
        }

        return conditions;
    }

    protected List<FilterCondition> createFilterConditionsByMetaClass(MetaClass rootEntityMetaClass,
                                                                      MetaClass currentEntityMetaClass,
                                                                      int currentDepth,
                                                                      String currentPropertyPath,
                                                                      @Nullable FilterCondition parent,
                                                                      @Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate) {
        List<FilterCondition> conditions = new ArrayList<>();
        for (MetaProperty property : currentEntityMetaClass.getProperties()) {
            String propertyPath = StringUtils.isEmpty(currentPropertyPath)
                    ? property.getName()
                    : currentPropertyPath + "." + property.getName();
            MetaPropertyPath metaPropertyPath = rootEntityMetaClass.getPropertyPath(propertyPath);

            if (!isMetaPropertyPathAllowed(metaPropertyPath)
                    || (propertiesFilterPredicate != null
                    && !propertiesFilterPredicate.test(metaPropertyPath))) {
                continue;
            }

            FilterCondition condition = createFilterConditionByMetaClass(property, parent);
            conditions.add(condition);

            if (currentDepth < uiProperties.getGenericFilterPropertiesHierarchyDepth()
                    && property.getRange().isClass()) {
                MetaClass childMetaClass = property.getRange().asClass();
                List<FilterCondition> children =
                        createFilterConditionsByMetaClass(rootEntityMetaClass, childMetaClass,
                                ++currentDepth, propertyPath, condition, propertiesFilterPredicate);
                conditions.addAll(children);
            }
        }

        return conditions;
    }

    protected boolean isMetaPropertyPathAllowed(MetaPropertyPath propertyPath) {
        UiEntityAttributeContext context = new UiEntityAttributeContext(propertyPath);
        accessManager.applyRegisteredConstraints(context);
        return context.canView();
    }

    protected FilterCondition createFilterConditionByMetaClass(MetaProperty metaProperty,
                                                               @Nullable FilterCondition parent) {

        Class modelClass = filterComponents.getModelClass(PropertyFilter.class);
        FilterCondition filterCondition = (FilterCondition) metadata.create(modelClass);
        filterCondition.setCaption(messageTools.getPropertyCaption(metaProperty));
        filterCondition.setParent(parent);

        if (PropertyFilterCondition.class.isAssignableFrom(modelClass)) {
            ((PropertyFilterCondition) filterCondition).setProperty(metaProperty.getName());
            ((PropertyFilterCondition) filterCondition).setParameterName(
                    PropertyConditionUtils.generateParameterName(metaProperty.getName()));
            ((PropertyFilterCondition) filterCondition).setCaptionPosition(SupportsCaptionPosition.CaptionPosition.LEFT);

            String propertyFilterPrefix = propertyFilterSupport
                    .getPropertyFilterPrefix(null, metaProperty.getName());
            filterCondition.setComponentId(propertyFilterPrefix.substring(0, propertyFilterPrefix.length() - 1));

            EnumSet<PropertyFilter.Operation> availableOperations =
                    propertyFilterSupport.getAvailableOperations(metaProperty.getDomain(), metaProperty.getName());
            PropertyFilter.Operation operation = availableOperations.stream()
                    .findFirst()
                    .orElse(PropertyFilter.Operation.EQUAL);
            ((PropertyFilterCondition) filterCondition).setOperation(operation);

            PropertyFilterValueComponent modelValueComponent = metadata.create(PropertyFilterValueComponent.class);
            HasValue valueComponent = propertyFilterSupport.generateValueComponent(metaProperty.getDomain(),
                    metaProperty.getName(), operation, filterCondition.getComponentId());
            modelValueComponent.setComponentId(valueComponent.getId());
            modelValueComponent.setComponentName(propertyFilterSupport.getValueComponentName(valueComponent));
            ((PropertyFilterCondition) filterCondition).setValueComponent(modelValueComponent);
        }

        return filterCondition;
    }

    protected HeaderFilterCondition createHeaderFilterCondition(String caption) {
        HeaderFilterCondition headerFilterCondition = metadata.create(HeaderFilterCondition.class);
        headerFilterCondition.setCaption(caption);
        return headerFilterCondition;
    }
}
