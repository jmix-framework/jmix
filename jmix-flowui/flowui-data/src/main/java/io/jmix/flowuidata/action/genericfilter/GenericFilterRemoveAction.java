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

import com.google.common.base.Strings;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowuidata.accesscontext.FlowuiGenericFilterModifyGlobalConfigurationContext;
import io.jmix.flowuidata.entity.FilterConfiguration;
import io.jmix.flowuidata.genericfilter.FlowuiDataGenericFilterSupport;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(GenericFilterRemoveAction.ID)
public class GenericFilterRemoveAction extends GenericFilterAction<GenericFilterRemoveAction> {

    public static final String ID = "genericFilter_remove";

    protected GenericFilterSupport genericFilterSupport;
    protected Dialogs dialogs;
    protected Messages messages;

    protected boolean globalConfigurationModificationPermitted;

    public GenericFilterRemoveAction() {
        this(ID);
    }

    public GenericFilterRemoveAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.TRASH);
    }

    @Autowired
    public void setGenericFilterSupport(GenericFilterSupport genericFilterSupport) {
        this.genericFilterSupport = genericFilterSupport;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.GenericFilter.Remove");
        this.messages = messages;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
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
                && target.getCurrentConfiguration() != target.getEmptyConfiguration()
                && !(target.getCurrentConfiguration() instanceof DesignTimeConfiguration)
                && (globalConfigurationModificationPermitted || !isCurrentConfigurationAvailableForAll());
    }

    @Override
    public void execute() {
        dialogs.createOptionDialog()
                .withHeader(messages.getMessage(getClass(), "genericFilterRemoveAction.confirmationDialog.header"))
                .withText(messages.getMessage(getClass(), "genericFilterRemoveAction.confirmationDialog.text"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(actionPerformedEvent ->
                                        genericFilterSupport.removeCurrentFilterConfiguration(target)),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }

    protected boolean isCurrentConfigurationAvailableForAll() {
        Configuration currentConfiguration = target.getCurrentConfiguration();
        FilterConfiguration model = ((FlowuiDataGenericFilterSupport) genericFilterSupport)
                .loadFilterConfigurationModel(target, currentConfiguration.getId());
        return model != null && Strings.isNullOrEmpty(model.getUsername());
    }
}
