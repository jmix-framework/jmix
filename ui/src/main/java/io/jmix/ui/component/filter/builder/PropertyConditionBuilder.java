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

package io.jmix.ui.component.filter.builder;

import io.jmix.core.AccessManager;
import io.jmix.core.JmixOrder;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.ui.UiProperties;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.SupportsCaptionPosition;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.FilterValueComponent;
import io.jmix.ui.entity.HeaderFilterCondition;
import io.jmix.ui.entity.PropertyFilterCondition;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

@Component("ui_PropertyConditionBuilder")
public class PropertyConditionBuilder extends AbstractConditionBuilder {

    @Autowired
    protected FilterComponents filterComponents;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected SingleFilterSupport singleFilterSupport;

    @Override
    public List<FilterCondition> build(Filter filter) {
        MetaClass rootEntityMetaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        Predicate<MetaPropertyPath> propertiesFilterPredicate = filter.getPropertiesFilterPredicate();
        List<FilterCondition> conditions =
                createFilterConditionsByMetaClass(rootEntityMetaClass, propertiesFilterPredicate);

        return conditions.size() > 1
                ? conditions
                : Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE;
    }

    protected List<FilterCondition> createFilterConditionsByMetaClass(MetaClass rootEntityMetaClass,
                                                                      @Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate) {
        HeaderFilterCondition propertiesHeaderCondition = createHeaderFilterCondition(
                messages.getMessage(PropertyConditionBuilder.class, "propertyConditionBuilder.headerCaption"));

        List<FilterCondition> conditions = createFilterConditionsByMetaClass(rootEntityMetaClass, rootEntityMetaClass,
                0, "", propertiesHeaderCondition, propertiesFilterPredicate);
        conditions.sort((condition1, condition2) ->
                ObjectUtils.compare(condition1.getCaption(), condition2.getCaption()));

        if (!conditions.isEmpty()) {
            conditions.add(0, propertiesHeaderCondition);
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

            if (metaPropertyPath == null
                    || !isMetaPropertyPathAllowed(metaPropertyPath)
                    || (propertiesFilterPredicate != null
                    && !propertiesFilterPredicate.test(metaPropertyPath))) {
                continue;
            }

            FilterCondition condition = createFilterConditionByMetaClass(metaPropertyPath, parent);
            conditions.add(condition);

            if (currentDepth < uiProperties.getFilterPropertiesHierarchyDepth()
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

    protected FilterCondition createFilterConditionByMetaClass(MetaPropertyPath metaPropertyPath,
                                                               @Nullable FilterCondition parent) {

        Class modelClass = filterComponents.getModelClass(PropertyFilter.class);
        FilterCondition filterCondition = (FilterCondition) metadata.create(modelClass);
        MetaClass metaClass = metaPropertyPath.getMetaClass();
        filterCondition.setMetaClass(metaClass.getName());
        filterCondition.setCaption(messageTools.getPropertyCaption(metaPropertyPath.getMetaProperty()));
        filterCondition.setParent(parent);

        if (PropertyFilterCondition.class.isAssignableFrom(modelClass)) {
            String property = metaPropertyPath.toPathString();
            ((PropertyFilterCondition) filterCondition).setProperty(property);
            ((PropertyFilterCondition) filterCondition).setParameterName(
                    PropertyConditionUtils.generateParameterName(property));
            ((PropertyFilterCondition) filterCondition)
                    .setCaptionPosition(SupportsCaptionPosition.CaptionPosition.LEFT);

            String propertyFilterPrefix =
                    propertyFilterSupport.getPropertyFilterPrefix(null, property);
            filterCondition.setComponentId(propertyFilterPrefix.substring(0, propertyFilterPrefix.length() - 1));

            EnumSet<PropertyFilter.Operation> availableOperations =
                    propertyFilterSupport.getAvailableOperations(metaClass, property);
            PropertyFilter.Operation operation = availableOperations.stream()
                    .findFirst()
                    .orElse(PropertyFilter.Operation.EQUAL);
            ((PropertyFilterCondition) filterCondition).setOperation(operation);

            FilterValueComponent modelValueComponent = metadata.create(FilterValueComponent.class);
            HasValue valueComponent = singleFilterSupport.generateValueComponent(metaClass,
                    property, operation);
            modelValueComponent.setComponentId(valueComponent.getId());
            modelValueComponent.setComponentName(singleFilterSupport.getValueComponentName(valueComponent));
            ((PropertyFilterCondition) filterCondition).setValueComponent(modelValueComponent);
        }

        return filterCondition;
    }
}
