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

package io.jmix.ui.component.groupfilter;

import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.GroupFilter;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.GroupFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Internal
@Component("ui_GroupFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupFilterConverter implements FilterConverter<GroupFilter, GroupFilterCondition> {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected FilterComponents filterComponents;

    protected final Filter filter;

    public GroupFilterConverter(Filter filter) {
        this.filter = filter;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GroupFilter convertToComponent(GroupFilterCondition model) {
        GroupFilter groupFilter = uiComponents.create(GroupFilter.NAME);
        groupFilter.setDataLoader(filter.getDataLoader());
        groupFilter.setVisible(model.getVisible());
        groupFilter.setEnabled(model.getEnabled());
        groupFilter.setStyleName(model.getStyleName());
        groupFilter.setOperation(model.getOperation());
        groupFilter.setCaption(model.getCaption());

        if (model.getOwnFilterConditions() != null) {
            for (FilterCondition ownFilterCondition : model.getOwnFilterConditions()) {
                FilterConverter ownFilterConverter =
                        filterComponents.getConverterByModelClass(ownFilterCondition.getClass(), filter);
                groupFilter.add(ownFilterConverter.convertToComponent(ownFilterCondition));
            }
        }

        groupFilter.setAutoApply(filter.isAutoApply());
        return groupFilter;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GroupFilterCondition convertToModel(GroupFilter groupFilter) {
        GroupFilterCondition groupFilterCondition = metadata.create(GroupFilterCondition.class);
        groupFilterCondition.setVisible(groupFilter.isVisible());
        groupFilterCondition.setEnabled(groupFilter.isEnabled());
        groupFilterCondition.setComponentId(groupFilter.getId());
        groupFilterCondition.setStyleName(groupFilter.getStyleName());
        groupFilterCondition.setOperation(groupFilter.getOperation());
        groupFilterCondition.setCaption(groupFilter.getCaption());

        List<FilterCondition> ownFilterConditions = new ArrayList<>();
        for (FilterComponent ownFilterComponent : groupFilter.getOwnFilterComponents()) {
            FilterConverter ownFilterConverter =
                    filterComponents.getConverterByComponentClass(ownFilterComponent.getClass(), filter);
            FilterCondition ownFilterCondition = ownFilterConverter.convertToModel(ownFilterComponent);
            ownFilterCondition.setParent(groupFilterCondition);
            ownFilterConditions.add(ownFilterCondition);
        }

        if (!ownFilterConditions.isEmpty()) {
            groupFilterCondition.setOwnFilterConditions(ownFilterConditions);
        }

        return groupFilterCondition;
    }
}
