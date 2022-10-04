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

package io.jmix.flowui.action.view;

import io.jmix.flowui.FlowuiViewProperties;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(ViewCloseAction.ID)
public class ViewCloseAction extends OperationResultViewAction<ViewCloseAction, View> {

    public static final String ID = "view_close";

    protected StandardOutcome outcome = StandardOutcome.CLOSE;

    public ViewCloseAction() {
        this(ID);
    }

    public ViewCloseAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.BAN);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Cancel");
    }

    @Autowired
    protected void setFlowUiViewProperties(FlowuiViewProperties flowUiViewProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiViewProperties.getCloseShortcut());
    }

    public void setOutcome(StandardOutcome outcome) {
        this.outcome = outcome;
    }

    public ViewCloseAction withOutcome(StandardOutcome outcome) {
        Preconditions.checkNotNullArgument(outcome);
        setOutcome(outcome);
        return this;
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.close(outcome);

        super.execute();
    }
}
