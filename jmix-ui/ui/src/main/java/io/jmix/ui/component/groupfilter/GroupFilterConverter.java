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

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.GroupFilter;
import io.jmix.ui.component.SizeWithUnit;
import io.jmix.ui.component.filter.converter.AbstractFilterComponentConverter;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.GroupFilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
@Internal
@Component("ui_GroupFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupFilterConverter extends AbstractFilterComponentConverter<GroupFilter, GroupFilterCondition> {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected FilterComponents filterComponents;
    @Autowired
    protected LogicalFilterSupport logicalFilterSupport;

    public GroupFilterConverter(Filter filter) {
        super(filter);
    }

    @Override
    public GroupFilter convertToComponent(GroupFilterCondition model) {
        GroupFilter groupFilter = super.convertToComponent(model);

        groupFilter.setOperation(model.getOperation());
        groupFilter.setCaption(model.getCaption());
        groupFilter.setOperationCaptionVisible(model.getOperationCaptionVisible());
        groupFilter.setCaptionWidth(new SizeWithUnit(filter.getCaptionWidth(), filter.getCaptionWidthSizeUnit())
                .stringValue());

        if (model.getOwnFilterConditions() != null) {
            for (FilterCondition ownFilterCondition : model.getOwnFilterConditions()) {
                FilterConverter ownFilterConverter =
                        filterComponents.getConverterByModelClass(ownFilterCondition.getClass(), filter);
                groupFilter.add(ownFilterConverter.convertToComponent(ownFilterCondition));
            }
        }

        return groupFilter;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GroupFilterCondition convertToModel(GroupFilter groupFilter) {
        GroupFilterCondition groupFilterCondition = super.convertToModel(groupFilter);

        groupFilterCondition.setOperation(groupFilter.getOperation());
        groupFilterCondition.setCaption(groupFilter.getCaption());
        groupFilterCondition.setLocalizedCaption(getLocalizedModelCaption(groupFilter));
        groupFilterCondition.setOperationCaptionVisible(groupFilter.isOperationCaptionVisible());

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
        return uiComponents.create(GroupFilter.NAME);
    }

    @Override
    protected GroupFilterCondition createModel() {
        return metadata.create(GroupFilterCondition.class);
    }

    @Nullable
    @Override
    protected String getLocalizedModelCaption(GroupFilter component) {
        String caption = component.getCaption();
        if (Strings.isNullOrEmpty(caption)) {
            return logicalFilterSupport.getOperationCaption(component.getOperation(),
                    component.isOperationCaptionVisible());
        } else {
            return caption;
        }
    }
}
