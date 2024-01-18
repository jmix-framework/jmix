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

import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.LockStatus;
import io.jmix.flowui.view.StandardDetailView;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailSaveCloseAction.ID)
public class DetailSaveCloseAction<E>
        extends OperationResultViewAction<DetailSaveCloseAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_saveClose";

    public DetailSaveCloseAction() {
        this(ID);
    }

    public DetailSaveCloseAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.CHECK);
        this.variant = ActionVariant.PRIMARY;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Ok");
    }

    @Autowired
    protected void setUiViewProperties(UiViewProperties uiViewProperties) {
        this.shortcutCombination = KeyCombination.create(uiViewProperties.getSaveShortcut());
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

        operationResult = target.closeWithSave();

        super.execute();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target.getLockStatus() != LockStatus.FAILED;
    }
}
