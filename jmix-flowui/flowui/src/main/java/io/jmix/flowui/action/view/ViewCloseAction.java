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

import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

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

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Cancel");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        // Check for 'null' for backward compatibility because 'icon' can be set in
        // the 'initAction()' method which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.VIEW_CLOSE_ACTION);
        }
    }

    @Autowired
    protected void setUiViewProperties(UiViewProperties viewProperties) {
        this.shortcutCombination = KeyCombination.create(viewProperties.getCloseShortcut());
    }

    @Override
    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        if (shortcutCombination != null) {
            shortcutCombination.setResetFocusOnActiveElement(true);
        }

        super.setShortcutCombination(shortcutCombination);
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
