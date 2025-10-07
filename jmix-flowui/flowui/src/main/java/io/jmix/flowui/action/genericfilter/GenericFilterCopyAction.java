/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.action.genericfilter;

import io.jmix.core.Messages;
import io.jmix.flowui.UiActionProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.kit.component.ComponentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@ActionType(GenericFilterCopyAction.ID)
public class GenericFilterCopyAction extends GenericFilterAction<GenericFilterCopyAction> {

    public static final String ID = "genericFilter_copy";

    protected FilterComponents filterComponents;
    protected GenericFilterSupport genericFilterSupport;

    public GenericFilterCopyAction() {
        this(ID);
    }

    public GenericFilterCopyAction(String id) {
        super(ID);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.genericFilter.Copy");
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Autowired
    public void setGenericFilterSupport(GenericFilterSupport genericFilterSupport) {
        this.genericFilterSupport = genericFilterSupport;
    }

    @Autowired
    protected void setUiActionProperties(UiActionProperties uiActionProperties) {
        // For backward compatibility, set the default icon only if the icon is null,
        // i.e., it was not set in the 'initAction' method, which is called first.
        if (icon == null) {
            this.icon = ComponentUtils.parseIcon(uiActionProperties.getGenericFilterCopyIcon());
        }
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && target.getCurrentConfiguration() != target.getEmptyConfiguration();
    }

    @Override
    public void execute() {
        checkTarget();

        Configuration currentConfiguration = target.getCurrentConfiguration();
        Configuration emptyConfiguration = target.getEmptyConfiguration();

        if (currentConfiguration != emptyConfiguration) {
            copyConfiguration(currentConfiguration, emptyConfiguration);
            emptyConfiguration.setModified(true);
            target.setCurrentConfiguration(emptyConfiguration);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void copyConfiguration(Configuration sourceConfiguration, Configuration destConfiguration) {
        destConfiguration.getRootLogicalFilterComponent().removeAll();

        Map<String, Object> valuesMap = genericFilterSupport.initConfigurationValuesMap(sourceConfiguration);
        LogicalFilterComponent sourceRootComponent = sourceConfiguration.getRootLogicalFilterComponent();
        FilterConverter converter =
                filterComponents.getConverterByComponentClass(sourceRootComponent.getClass(), target);
        FilterCondition filterCondition = converter.convertToModel(sourceRootComponent);

        genericFilterSupport.resetConfigurationValuesMap(sourceConfiguration, valuesMap);

        LogicalFilterComponent copy = (LogicalFilterComponent) converter.convertToComponent(filterCondition);
        destConfiguration.setRootLogicalFilterComponent(copy);
        genericFilterSupport.refreshConfigurationDefaultValues(destConfiguration);
        genericFilterSupport.resetConfigurationValuesMap(destConfiguration, valuesMap);
    }
}
