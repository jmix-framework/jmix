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

package io.jmix.flowuidata.action.genericfilter;

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowuidata.accesscontext.FlowuiGenericFilterModifyGlobalConfigurationContext;
import io.jmix.flowuidata.entity.FilterConfiguration;
import io.jmix.flowuidata.genericfilter.FlowuiDataGenericFilterSupport;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(GenericFilterSaveAction.ID)
public class GenericFilterSaveAction extends AbstractGenericFilterSaveAction<GenericFilterSaveAction> {

    public static final String ID = "genericFilter_save";

    protected boolean globalConfigurationModificationPermitted;

    public GenericFilterSaveAction() {
        this(ID);
    }

    public GenericFilterSaveAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.GenericFilter.Save");
        this.messages = messages;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        FlowuiGenericFilterModifyGlobalConfigurationContext globalFilterContext =
                new FlowuiGenericFilterModifyGlobalConfigurationContext();
        accessManager.applyRegisteredConstraints(globalFilterContext);
        globalConfigurationModificationPermitted = globalFilterContext.isPermitted();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(target.getCurrentConfiguration() instanceof DesignTimeConfiguration)
                && target.getCurrentConfiguration().isModified()
                && (globalConfigurationModificationPermitted || !isCurrentConfigurationAvailableForAll());
    }

    @Override
    public void execute() {
        checkTarget();

        Configuration configuration = target.getCurrentConfiguration();

        if (configuration == target.getEmptyConfiguration()) {
            openInputDialog();
        } else {
            FilterConfiguration configurationModel = ((FlowuiDataGenericFilterSupport) genericFilterSupport)
                    .loadFilterConfigurationModel(target, configuration.getId());

            if (configurationModel != null) {
                saveExistedConfigurationModel(configuration, configurationModel);
            } else {
                openInputDialog();
            }
        }
    }
}
