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

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowuiViewProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailCloseAction.ID)
public class DetailCloseAction<E> extends OperationResultViewAction<DetailCloseAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_close";

    public DetailCloseAction() {
        this(ID);
    }

    public DetailCloseAction(String id) {
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

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.close(ViewControllerUtils.isSaveActionPerformed(target)
                ? StandardOutcome.SAVE
                : StandardOutcome.CLOSE);

        super.execute();
    }
}
