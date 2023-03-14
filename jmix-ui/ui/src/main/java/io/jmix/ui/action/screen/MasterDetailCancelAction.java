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

package io.jmix.ui.action.screen;

import io.jmix.core.Messages;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.MasterDetailScreen;
import org.springframework.beans.factory.annotation.Autowired;

@StudioAction(
        target = "io.jmix.ui.screen.Screen",
        description = "Cancels editing in the master detail screen"
)
@ActionType(MasterDetailCancelAction.ID)
public class MasterDetailCancelAction<T>
        extends OperationResultScreenAction<MasterDetailCancelAction<T>, MasterDetailScreen<T>> {

    public static final String ID = "masterDetailCancel";

    public MasterDetailCancelAction() {
        this(ID);
    }

    public MasterDetailCancelAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Cancel");
        this.description = messages.getMessage("masterDetailCancelAction.description");
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.EDITOR_CANCEL);
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.discardChanges(null);

        super.execute();
    }
}
