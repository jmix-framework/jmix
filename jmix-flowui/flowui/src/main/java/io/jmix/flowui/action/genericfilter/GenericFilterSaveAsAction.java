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
import io.jmix.flowui.UiIconProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.kit.component.ComponentUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action to saves current filter configuration with a new id and name.
 */
@ActionType(GenericFilterSaveAsAction.ID)
public class GenericFilterSaveAsAction extends AbstractGenericFilterSaveAction<GenericFilterSaveAsAction> {

    public static final String ID = "genericFilter_saveAs";

    public GenericFilterSaveAsAction() {
        this(ID);
    }

    public GenericFilterSaveAsAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.GenericFilter.SaveAs");
        this.messages = messages;
    }

    @Autowired
    protected void setUiIconProperties(UiIconProperties uiIconProperties) {
        // For backward compatibility, set the default icon only if the icon is null,
        // i.e., it was not set in the 'initAction' method, which is called first.
        if (icon == null) {
            this.icon = ComponentUtils.parseIcon(uiIconProperties.getGenericFilterSaveAsActionIcon());
        }
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(target.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @Override
    public void execute() {
        openInputDialog();
    }
}

