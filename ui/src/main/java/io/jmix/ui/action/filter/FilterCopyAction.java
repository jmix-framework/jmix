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

package io.jmix.ui.action.filter;

import io.jmix.core.Messages;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import org.springframework.beans.factory.annotation.Autowired;

@StudioAction(category = "Filter Actions",
        description = "Copies all conditions from design-time configuration to run-time configuration")
@ActionType(FilterCopyAction.ID)
public class FilterCopyAction extends FilterAction {

    public static final String ID = "filter_copy";

    protected FilterComponents filterComponents;

    public FilterCopyAction() {
        this(ID);
    }

    public FilterCopyAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.Copy");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.COPY);
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && filter.getCurrentConfiguration() != filter.getEmptyConfiguration();
    }

    @Override
    public void execute() {
        Filter.Configuration configuration = filter.getCurrentConfiguration();
        Filter.Configuration emptyConfiguration = filter.getEmptyConfiguration();
        if (configuration != emptyConfiguration) {
            copyComponents(configuration, emptyConfiguration);
            emptyConfiguration.setModified(true);
            filter.setCurrentConfiguration(emptyConfiguration);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void copyComponents(Filter.Configuration sourceConfiguration, Filter.Configuration destConfiguration) {
        destConfiguration.getRootLogicalFilterComponent().removeAll();

        LogicalFilterComponent sourceRootComponent = sourceConfiguration.getRootLogicalFilterComponent();
        for (FilterComponent ownFilterComponent : sourceRootComponent.getOwnFilterComponents()) {
            FilterConverter converter =
                    filterComponents.getConverterByComponentClass(ownFilterComponent.getClass(), filter);
            FilterCondition filterCondition = converter.convertToModel(ownFilterComponent);
            FilterComponent copy = converter.convertToComponent(filterCondition);
            destConfiguration.getRootLogicalFilterComponent().add(copy);
        }
    }
}
