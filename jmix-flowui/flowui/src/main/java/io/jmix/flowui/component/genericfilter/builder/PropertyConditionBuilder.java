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

package io.jmix.flowui.component.genericfilter.builder;

import com.vaadin.flow.component.HasValueAndElement;
import io.jmix.core.JmixOrder;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.FilterMetadataTools;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.HeaderFilterCondition;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Component("flowui_PropertyConditionBuilder")
public class PropertyConditionBuilder extends AbstractConditionBuilder {

    protected final FilterComponents filterComponents;
    protected final Messages messages;
    protected final MessageTools messageTools;
    protected final PropertyFilterSupport propertyFilterSupport;
    protected final FilterMetadataTools filterMetadataTools;
    protected final SingleFilterSupport singleFilterSupport;

    public PropertyConditionBuilder(Metadata metadata,
                                    FilterComponents filterComponents,
                                    Messages messages,
                                    MessageTools messageTools,
                                    PropertyFilterSupport propertyFilterSupport,
                                    FilterMetadataTools filterMetadataTools,
                                    SingleFilterSupport singleFilterSupport) {
        super(metadata);
        this.filterComponents = filterComponents;
        this.messages = messages;
        this.messageTools = messageTools;
        this.propertyFilterSupport = propertyFilterSupport;
        this.filterMetadataTools = filterMetadataTools;
        this.singleFilterSupport = singleFilterSupport;
    }

    @Override
    public List<FilterCondition> build(GenericFilter filter) {
        MetaClass filterMetaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
        String query = filter.getDataLoader().getQuery();
        Predicate<MetaPropertyPath> propertiesFilterPredicate = filter.getPropertyFiltersPredicate();

        List<MetaPropertyPath> paths = filterMetadataTools.getPropertyPaths(filterMetaClass, query,
                propertiesFilterPredicate);

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

        for (MetaPropertyPath path : paths) {
            FilterCondition condition = createFilterConditionByPath(path);
            FilterCondition parent = path.isDirectProperty()
                    ? propertiesHeaderCondition
                    : getParentCondition(path, conditions);
            condition.setParent(parent);
            conditions.add(condition);
        }

        conditions.sort((condition1, condition2) ->
                ObjectUtils.compare(condition1.getLocalizedLabel(), condition2.getLocalizedLabel()));

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
            String localizedCaption = messageTools.getPropertyCaption(metaClass, property);
            filterCondition.setLocalizedLabel(localizedCaption);

            String propertyFilterPrefix = propertyFilterSupport.getPropertyFilterPrefix(Optional.empty(), property);
            filterCondition.setComponentId(propertyFilterPrefix.substring(0, propertyFilterPrefix.length() - 1));

            PropertyFilter.Operation operation = propertyFilterSupport.getDefaultOperation(metaClass, property);
            ((PropertyFilterCondition) filterCondition).setOperation(operation);

            FilterValueComponent modelValueComponent = metadata.create(FilterValueComponent.class);
            HasValueAndElement<?, ?> valueComponent = singleFilterSupport.generateValueComponent(metaClass,
                    property, operation);
            modelValueComponent.setComponentId(((com.vaadin.flow.component.Component) valueComponent).getId().orElse(null));
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
