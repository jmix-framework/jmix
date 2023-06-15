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

package io.jmix.flowui.component.genericfilter;

import com.google.common.collect.ImmutableSet;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction;
import io.jmix.flowui.action.genericfilter.GenericFilterCopyAction;
import io.jmix.flowui.action.genericfilter.GenericFilterEditAction;
import io.jmix.flowui.app.filter.condition.FilterConditionDetailView;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.component.genericfilter.configuration.FilterConfigurationDetail;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.view.DialogWindow;
import org.springframework.stereotype.Component;

import java.util.*;

@Internal
@Component("flowui_GenericFilterSupport")
public class GenericFilterSupport {

    protected final Actions actions;
    protected final UiComponents uiComponents;

    public GenericFilterSupport(Actions actions, UiComponents uiComponents) {
        this.actions = actions;
        this.uiComponents = uiComponents;
    }

    public List<GenericFilterAction<?>> getDefaultFilterActions(GenericFilter filter) {
        List<GenericFilterAction<?>> filterActions = new ArrayList<>();
        for (String actionId : getDefaultFilterActionIds()) {
            filterActions.add(createFilterAction(actionId, filter));
        }
        return filterActions;
    }

    public Configuration saveCurrentFilterConfiguration(Configuration configuration,
                                                        boolean isNewConfiguration,
                                                        LogicalFilterComponent<?> rootFilterComponent,
                                                        AbstractConfigurationDetail configurationDetail) {
        String id = "";
        String name = "";

        if (configurationDetail instanceof FilterConfigurationDetail) {
            id = ((FilterConfigurationDetail) configurationDetail).getConfigurationId();
            name = ((FilterConfigurationDetail) configurationDetail).getConfigurationName();
        }

        return initFilterConfiguration(id, name, configuration, isNewConfiguration, rootFilterComponent);
    }

    public void removeCurrentFilterConfiguration(GenericFilter filter) {
        filter.removeConfiguration(filter.getCurrentConfiguration());
    }

    public AbstractConfigurationDetail createFilterConfigurationDetail(
            DialogWindow<? extends FilterConditionDetailView<?>> dialog,
            boolean isNewConfiguration,
            Configuration currentConfiguration) {
        FilterConfigurationDetail configurationDetail = uiComponents.create(FilterConfigurationDetail.class);
        initFilterConfigurationDetail(configurationDetail, isNewConfiguration, currentConfiguration);

        return configurationDetail;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Object> initConfigurationValuesMap(Configuration configuration) {
        HashMap<String, Object> valuesMap = new HashMap<>();
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase singleFilterComponent = ((SingleFilterComponentBase<?>) filterComponent);
                String parameterName = singleFilterComponent.getParameterName();
                valuesMap.put(parameterName, singleFilterComponent.getValue());
                singleFilterComponent.setValue(configuration.getFilterComponentDefaultValue(parameterName));
            }
        }

        return valuesMap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void resetConfigurationValuesMap(Configuration configuration, Map<String, Object> valuesMap) {
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase singleFilterComponent = (SingleFilterComponentBase) filterComponent;
                singleFilterComponent.setValue(valuesMap.get(singleFilterComponent.getParameterName()));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void refreshConfigurationValuesMap(Configuration configuration, Map<String, Object> valuesMap) {
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase singleFilterComponent = (SingleFilterComponentBase) filterComponent;

                String parameterName = singleFilterComponent.getParameterName();
                Object value = valuesMap.get(parameterName);
                Object defaultValue = configuration.getFilterComponentDefaultValue(parameterName);

                if (value == null && defaultValue != null) {
                    singleFilterComponent.setValue(defaultValue);
                } else {
                    try {
                        singleFilterComponent.setValue(value);
                    } catch (ClassCastException e) {
                        singleFilterComponent.setValue(defaultValue);
                    }
                }
            }
        }
    }

    public void refreshConfigurationDefaultValues(Configuration configuration) {
        configuration.resetAllDefaultValues();
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                configuration.setFilterComponentDefaultValue(
                        ((SingleFilterComponentBase<?>) filterComponent).getParameterName(),
                        ((SingleFilterComponentBase<?>) filterComponent).getValue()
                );
            }
        }
    }

    protected Configuration initFilterConfiguration(String id,
                                                    String name,
                                                    Configuration existedConfiguration,
                                                    boolean isNewConfiguration,
                                                    LogicalFilterComponent<?> rootFilterComponent) {
        Configuration resultConfiguration;
        GenericFilter owner = existedConfiguration.getOwner();

        if (isNewConfiguration) {
            resultConfiguration = new RunTimeConfiguration(id, rootFilterComponent, owner);
        } else if (!existedConfiguration.getId().equals(id)) {
            resultConfiguration = new RunTimeConfiguration(id, rootFilterComponent, owner);
            owner.removeConfiguration(existedConfiguration);
        } else {
            resultConfiguration = existedConfiguration;
            resultConfiguration.setRootLogicalFilterComponent(rootFilterComponent);
        }

        resultConfiguration.setName(name);

        return resultConfiguration;
    }

    protected void initFilterConfigurationDetail(FilterConfigurationDetail filterConfigurationDetail,
                                                 boolean isNewConfiguration,
                                                 Configuration currentConfiguration) {
        filterConfigurationDetail.setConfigurationName(currentConfiguration.getName());

        if (!isNewConfiguration) {
            filterConfigurationDetail.setConfigurationId(currentConfiguration.getId());
        }
    }

    protected Set<String> getDefaultFilterActionIds() {
        return ImmutableSet.of(
                GenericFilterEditAction.ID,
                GenericFilterCopyAction.ID,
                GenericFilterClearValuesAction.ID
        );
    }

    public Map<Configuration, Boolean> getConfigurationsMap(GenericFilter filter) {
        return Collections.emptyMap();
    }

    protected GenericFilterAction<?> createFilterAction(String filterActionId,
                                                        GenericFilter filter) {
        GenericFilterAction<?> filterAction = actions.create(filterActionId);
        filterAction.setTarget(filter);
        return filterAction;
    }
}
