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
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.ScreenContext;
import org.springframework.beans.factory.annotation.Autowired;

@StudioAction(target = "io.jmix.ui.component.Filter", description = "Removes current run-time filter configuration")
@ActionType(FilterRemoveAction.ID)
public class FilterRemoveAction extends FilterAction {

    public static final String ID = "filter_remove";

    protected FilterSupport filterSupport;
    protected Messages messages;

    public FilterRemoveAction() {
        this(ID);
    }

    public FilterRemoveAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.Remove");
        this.messages = messages;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.REMOVE);
    }

    @Autowired
    public void setFilterSupport(FilterSupport filterSupport) {
        this.filterSupport = filterSupport;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && filter.getCurrentConfiguration() != filter.getEmptyConfiguration()
                && !(filter.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @Override
    public void execute() {
        ScreenContext screenContext = ComponentsHelper.getScreenContext(filter);
        Dialogs dialogs = screenContext.getDialogs();
        dialogs.createOptionDialog()
                .withCaption(messages.getMessage(FilterRemoveAction.class, "confirmationDialog.caption"))
                .withMessage(messages.getMessage(FilterRemoveAction.class, "confirmationDialog.message"))
                .withActions(new DialogAction(DialogAction.Type.YES)
                                .withHandler(actionPerformedEvent ->
                                        filterSupport.removeCurrentFilterConfiguration(filter)),
                        new DialogAction(DialogAction.Type.NO))
                .show();
    }
}
