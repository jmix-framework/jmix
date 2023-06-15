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

package io.jmix.flowui.component.logicalfilter;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.converter.AbstractFilterComponentConverter;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.GroupFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.List;

@Internal
@Component("flowui_GroupFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupFilterConverter extends AbstractFilterComponentConverter<GroupFilter, GroupFilterCondition> {

    protected Metadata metadata;
    protected UiComponents uiComponents;
    protected FilterComponents filterComponents;
    protected LogicalFilterSupport logicalFilterSupport;

    public GroupFilterConverter(GenericFilter filter) {
        super(filter);
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Autowired
    public void setLogicalFilterComponent(LogicalFilterSupport logicalFilterSupport) {
        this.logicalFilterSupport = logicalFilterSupport;
    }

    @Override
    public GroupFilter convertToComponent(GroupFilterCondition model) {
        GroupFilter groupFilter = super.convertToComponent(model);

        groupFilter.setOperation(model.getOperation());
        groupFilter.setSummaryText(model.getLabel());
        groupFilter.setOperationTextVisible(model.getOperationTextVisible());

        if (model.getOwnFilterConditions() != null) {
            for (FilterCondition ownFilterCondition : model.getOwnFilterConditions()) {
                FilterConverter ownFilterConverter =
                        filterComponents.getConverterByModelClass(ownFilterCondition.getClass(), filter);
                groupFilter.add(ownFilterConverter.convertToComponent(ownFilterCondition));
            }
        }

        return groupFilter;
    }

    @Override
    public GroupFilterCondition convertToModel(GroupFilter groupFilter) {
        GroupFilterCondition groupFilterCondition = super.convertToModel(groupFilter);

        groupFilterCondition.setOperation(groupFilter.getOperation());
        groupFilterCondition.setLabel(groupFilter.getSummaryText());
        groupFilterCondition.setLocalizedLabel(getLocalizedModelLabel(groupFilter));
        groupFilterCondition.setOperationTextVisible(groupFilter.isOperationTextVisible());

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

    @Override
    protected GroupFilter createComponent() {
        return uiComponents.create(GroupFilter.class);
    }

    @Override
    protected GroupFilterCondition createModel() {
        return metadata.create(GroupFilterCondition.class);
    }

    @Nullable
    @Override
    protected String getLocalizedModelLabel(GroupFilter component) {
        String label = component.getSummaryText();
        if (Strings.isNullOrEmpty(label)) {
            return logicalFilterSupport.getOperationText(component.getOperation(),
                    component.isOperationTextVisible());
        } else {
            return label;
        }
    }
}
