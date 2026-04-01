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
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.LockStatus;
import io.jmix.flowui.view.StandardDetailView;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;

@ActionType(DetailSaveAction.ID)
public class DetailSaveAction<E> extends OperationResultViewAction<DetailSaveAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_save";

    public DetailSaveAction() {
        this(ID);
    }

    public DetailSaveAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Save");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        // Check for 'null' for backward compatibility because 'icon' can be set in
        // the 'initAction()' method which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.DETAIL_SAVE_ACTION);
        }
    }

    @Override
    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        if (shortcutCombination != null) {
            shortcutCombination.setResetFocusOnActiveElement(true);
        }

        super.setShortcutCombination(shortcutCombination);
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.save();

        super.execute();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target.getLockStatus() != LockStatus.FAILED;
    }
}
