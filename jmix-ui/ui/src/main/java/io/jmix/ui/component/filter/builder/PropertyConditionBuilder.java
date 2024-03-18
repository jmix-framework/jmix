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

import io.jmix.core.JmixOrder;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.SupportsCaptionPosition;
import io.jmix.ui.component.filter.FilterMetadataTools;
import io.jmix.ui.component.filter.inspector.FilterPropertiesInspector;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.FilterValueComponent;
import io.jmix.ui.entity.HeaderFilterCondition;
import io.jmix.ui.entity.PropertyFilterCondition;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Component("ui_PropertyConditionBuilder")
public class PropertyConditionBuilder extends AbstractConditionBuilder {

    protected FilterComponents filterComponents;
    protected Messages messages;
    protected MessageTools messageTools;
    protected PropertyFilterSupport propertyFilterSupport;
    protected FilterMetadataTools filterMetadataTools;
    protected SingleFilterSupport singleFilterSupport;

    @Autowired
    public PropertyConditionBuilder(FilterComponents filterComponents,
                                    Messages messages,
                                    MessageTools messageTools,
                                    PropertyFilterSupport propertyFilterSupport,
                                    FilterMetadataTools filterMetadataTools, SingleFilterSupport singleFilterSupport) {
        this.filterComponents = filterComponents;
        this.messages = messages;
        this.messageTools = messageTools;
        this.propertyFilterSupport = propertyFilterSupport;
        this.filterMetadataTools = filterMetadataTools;
        this.singleFilterSupport = singleFilterSupport;
    }

    @Override
    public List<FilterCondition> build(Filter filter) {
        MetaClass filterMetaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        String query = filter.getDataLoader().getQuery();
        Predicate<MetaPropertyPath> propertiesFilterPredicate = filter.getPropertiesFilterPredicate();
        List<MetaPropertyPath> paths;
        if(filter.getPropertiesFilterPredicate() instanceof FilterPropertiesInspector && ((FilterPropertiesInspector) filter.getPropertiesFilterPredicate()).getIncludedProperties().size() > 0){
            paths = filterMetadataTools.getPropertyPathsFromIncludedProperties(filterMetaClass, query,
                    propertiesFilterPredicate, ((FilterPropertiesInspector) filter.getPropertiesFilterPredicate()).getIncludedProperties());
        } else {
            paths = filterMetadataTools.getPropertyPaths(filterMetaClass, query,
                    propertiesFilterPredicate);
        }

        return !paths.isEmpty()
                ? createFilterConditionsByPaths(paths)
                : Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE;
    }

    protected List<FilterCondition> createFilterConditionsByPaths(List<MetaPropertyPath> paths) {
        List<FilterCondition> conditions = new ArrayList<>();

        HeaderFilterCondition propertiesHeaderCondition = createHeaderFilterCondition(
                messages.getMessage(PropertyConditionBuilder.class, "propertyConditionBuilder.headerCaption"));
        conditions.add(propertiesHeaderCondition);

        Map<FilterCondition,MetaPropertyPath> propertyMap = new HashMap<>();

        for (MetaPropertyPath path : paths) {
            FilterCondition condition = createFilterConditionByPath(path);
            conditions.add(condition);
            propertyMap.put(condition,path);
        }

        for(FilterCondition condition: conditions){
            MetaPropertyPath path = propertyMap.get(condition);
            if(path == null) {
                continue;
            }
            FilterCondition parent = path.isDirectProperty()
                    ? propertiesHeaderCondition
                    : getParentCondition(path, conditions);
            condition.setParent(parent);
        }

        conditions.sort((condition1, condition2) ->
                ObjectUtils.compare(condition1.getLocalizedCaption(), condition2.getLocalizedCaption()));

        return conditions;
    }

    protected FilterCondition createFilterConditionByPath(MetaPropertyPath metaPropertyPath) {

        Class modelClass = filterComponents.getModelClass(PropertyFilter.class);
        FilterCondition filterCondition = (FilterCondition) metadata.create(modelClass);
        MetaClass metaClass = metaPropertyPath.getMetaClass();

        if (PropertyFilterCondition.class.isAssignableFrom(modelClass)) {
            String property = metaPropertyPath.toPathString();
            ((PropertyFilterCondition) filterCondition).setProperty(property);
            ((PropertyFilterCondition) filterCondition).setParameterName(
                    PropertyConditionUtils.generateParameterName(property));
            ((PropertyFilterCondition) filterCondition)
                    .setCaptionPosition(SupportsCaptionPosition.CaptionPosition.LEFT);
            String localizedCaption = messageTools.getPropertyCaption(metaClass, property);
            filterCondition.setLocalizedCaption(localizedCaption);

            String propertyFilterPrefix = propertyFilterSupport.getPropertyFilterPrefix(null, property);
            filterCondition.setComponentId(propertyFilterPrefix.substring(0, propertyFilterPrefix.length() - 1));

            PropertyFilter.Operation operation = propertyFilterSupport.getDefaultOperation(metaClass, property);
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

    @Nullable
    protected FilterCondition getParentCondition(MetaPropertyPath mpp, List<FilterCondition> conditions) {
        String[] path = mpp.getPath();
        String parentPath = String.join(".", Arrays.copyOf(path, path.length - 1));
        return conditions.stream()
                .filter(filterCondition -> filterCondition instanceof PropertyFilterCondition
                        && parentPath.equals(((PropertyFilterCondition) filterCondition).getProperty()))
                .findFirst()
                .orElse(null);
    }
}
