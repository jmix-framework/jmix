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

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.accesscontext.UiGenericFilterModifyGlobalConfigurationContext;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action to save changes to current filter configuration using the values in filter components as default values.
 */
@ActionType(GenericFilterSaveWithValuesAction.ID)
public class GenericFilterSaveWithValuesAction
        extends AbstractGenericFilterSaveAction<GenericFilterSaveWithValuesAction> {

    public static final String ID = "genericFilter_saveWithValues";

    protected boolean globalConfigurationModificationPermitted;

    public GenericFilterSaveWithValuesAction() {
        this(ID);
    }

    public GenericFilterSaveWithValuesAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.GenericFilter.SaveWithValues");
        this.messages = messages;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        UiGenericFilterModifyGlobalConfigurationContext globalFilterContext =
                new UiGenericFilterModifyGlobalConfigurationContext();
        accessManager.applyRegisteredConstraints(globalFilterContext);
        globalConfigurationModificationPermitted = globalFilterContext.isPermitted();
    }

    @Override
    protected boolean isApplicable() {
        return target != null
                && !(target.getCurrentConfiguration() instanceof DesignTimeConfiguration)
                && !target.getCurrentConfiguration().getRootLogicalFilterComponent().getFilterComponents().isEmpty()
                && (globalConfigurationModificationPermitted || !isCurrentConfigurationAvailableForAll());
    }

    @Override
    public void execute() {
        checkTarget();

        Configuration configuration = target.getCurrentConfiguration();

        if (configuration == target.getEmptyConfiguration()) {
            openInputDialog();
        } else {
            FilterConfigurationModel configurationModel = genericFilterSupport
                    .loadFilterConfigurationModel(target, configuration.getId());

            if (configurationModel != null) {
                saveExistedConfigurationModel(configuration, configurationModel);
            } else {
                openInputDialog();
            }
        }
    }

    @Override
    protected void saveNewConfigurationModel(Configuration configuration) {
        genericFilterSupport.saveConfigurationModel(configuration, null);
        genericFilterSupport.refreshConfigurationDefaultValues(configuration);

        target.addConfiguration(configuration);
        setCurrentFilterConfiguration(configuration);
        target.getEmptyConfiguration().getRootLogicalFilterComponent().removeAll();
    }

    @Override
    protected void saveExistedConfigurationModel(Configuration configuration,
                                                 @Nullable FilterConfigurationModel existedConfigurationModel) {
        genericFilterSupport
                .saveConfigurationModel(configuration, existedConfigurationModel);
        genericFilterSupport.refreshConfigurationDefaultValues(configuration);
        setCurrentFilterConfiguration(configuration);
    }
}
