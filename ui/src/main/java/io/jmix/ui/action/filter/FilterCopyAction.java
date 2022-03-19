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
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@StudioAction(
        target = "io.jmix.ui.component.Filter",
        description = "Copies all conditions from design-time configuration to run-time configuration")
@ActionType(FilterCopyAction.ID)
public class FilterCopyAction extends FilterAction {

    public static final String ID = "filter_copy";

    protected FilterComponents filterComponents;
    protected FilterSupport filterSupport;

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

    @Autowired
    public void setFilterSupport(FilterSupport filterSupport) {
        this.filterSupport = filterSupport;
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
            copyConfiguration(configuration, emptyConfiguration);
            emptyConfiguration.setModified(true);
            filter.setCurrentConfiguration(emptyConfiguration);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void copyConfiguration(Filter.Configuration sourceConfiguration, Filter.Configuration destConfiguration) {
        destConfiguration.getRootLogicalFilterComponent().removeAll();

        Map<String, Object> valuesMap = filterSupport.initConfigurationValuesMap(sourceConfiguration);
        LogicalFilterComponent sourceRootComponent = sourceConfiguration.getRootLogicalFilterComponent();
        FilterConverter converter =
                filterComponents.getConverterByComponentClass(sourceRootComponent.getClass(), filter);
        FilterCondition filterCondition = converter.convertToModel(sourceRootComponent);
        filterSupport.resetConfigurationValuesMap(sourceConfiguration, valuesMap);

        LogicalFilterComponent copy = (LogicalFilterComponent) converter.convertToComponent(filterCondition);
        destConfiguration.setRootLogicalFilterComponent(copy);
        filterSupport.refreshConfigurationDefaultValues(destConfiguration);
        filterSupport.resetConfigurationValuesMap(destConfiguration, valuesMap);
    }
}
