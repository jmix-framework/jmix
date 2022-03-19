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

package io.jmix.uidata.action.filter;

import io.jmix.core.Messages;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.meta.StudioAction;
import io.jmix.uidata.entity.FilterConfiguration;
import io.jmix.uidata.filter.UiDataFilterSupport;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@StudioAction(
        target = "io.jmix.ui.component.Filter",
        description = "Saves changes to current filter configuration using the values in filter components as default values")
@ActionType(FilterSaveWithValuesAction.ID)
public class FilterSaveWithValuesAction extends FilterSaveAction {

    public static final String ID = "filter_saveWithValues";

    public FilterSaveWithValuesAction() {
        this(ID);
    }

    public FilterSaveWithValuesAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.SaveWithValues");
        this.messages = messages;
    }

    @Override
    protected boolean isApplicable() {
        return filter != null
                && !(filter.getCurrentConfiguration() instanceof DesignTimeConfiguration)
                && !filter.getCurrentConfiguration().getRootLogicalFilterComponent().getFilterComponents().isEmpty();
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    protected void saveNewConfigurationModel(Filter.Configuration configuration) {
        ((UiDataFilterSupport) filterSupport).saveConfigurationModel(configuration, null);
        filterSupport.refreshConfigurationDefaultValues(configuration);
        filter.addConfiguration(configuration);
        setCurrentFilterConfiguration(configuration);
        filter.getEmptyConfiguration().getRootLogicalFilterComponent().removeAll();
    }

    @Override
    protected void saveExistedConfigurationModel(Filter.Configuration configuration,
                                                 @Nullable FilterConfiguration existedConfigurationModel) {
        ((UiDataFilterSupport) filterSupport).saveConfigurationModel(configuration, existedConfigurationModel);
        filterSupport.refreshConfigurationDefaultValues(configuration);
        setCurrentFilterConfiguration(configuration);
    }
}
