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
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.StandardListView;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(LookupDiscardAction.ID)
public class LookupDiscardAction<E> extends OperationResultViewAction<LookupDiscardAction<E>, StandardListView<E>> {

    public static final String ID = "lookup_discard";

    public LookupDiscardAction() {
        this(ID);
    }

    public LookupDiscardAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.BAN);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Cancel");
    }

    @Autowired
    protected void setUiViewProperties(UiViewProperties viewProperties) {
        this.shortcutCombination = KeyCombination.create(viewProperties.getCloseShortcut());
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.closeWithDiscard();

        super.execute();
    }
}
