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

package io.jmix.flowui.action.genericfilter;

import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(GenericFilterResetAction.ID)
public class GenericFilterResetAction extends GenericFilterAction<GenericFilterResetAction> {

    public static final String ID = "filter_reset";

    public GenericFilterResetAction() {
        this(ID);
    }

    public GenericFilterResetAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.genericFilter.Reset");
    }

    @Override
    public void execute() {
        checkTarget();

        Configuration configuration = target.getEmptyConfiguration();
        configuration.getRootLogicalFilterComponent().removeAll();
        configuration.setModified(false);

        FilterUtils.setCurrentConfiguration(target, configuration, true);

        target.apply();
    }
}
