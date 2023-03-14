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
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.StandardEditor;
import org.springframework.beans.factory.annotation.Autowired;

@StudioAction(
        target = "io.jmix.ui.screen.Screen",
        description = "Closes the editor screen"
)
@ActionType(EditorCloseAction.ID)
public class EditorCloseAction<T> extends OperationResultScreenAction<EditorCloseAction<T>, StandardEditor<T>> {

    public static final String ID = "editorClose";

    public EditorCloseAction() {
        this(ID);
    }

    public EditorCloseAction(String id) {
        super(id);
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.EDITOR_CANCEL);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Cancel");
    }

    @Autowired
    public void setUiScreenProperties(UiScreenProperties uiScreenProperties) {
        setShortcut(uiScreenProperties.getCloseShortcut());
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.cancel(null);

        super.execute();
    }
}
