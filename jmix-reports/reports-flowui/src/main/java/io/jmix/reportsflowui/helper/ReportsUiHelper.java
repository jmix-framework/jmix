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

package io.jmix.reportsflowui.helper;

import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.reportsflowui.view.scripteditor.ScriptEditorView;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component("reportsflowui_ReportsUiHelper")
public class ReportsUiHelper {

    public static final String FIELD_ICON_CLASS_NAME = "reports-field-icon";
    public static final String FIELD_ICON_SIZE_CLASS_NAME = "reports-field-icon-size";

    protected final UiComponents uiComponents;
    protected final Messages messages;
    protected final DialogWindows dialogWindows;

    public ReportsUiHelper(UiComponents uiComponents, Messages messages, DialogWindows dialogWindows) {
        this.uiComponents = uiComponents;
        this.messages = messages;
        this.dialogWindows = dialogWindows;
    }

    public Builder showScriptEditorDialog(View<?> parentView) {
        DialogWindow<ScriptEditorView> scriptEditorDialogWindow = dialogWindows.view(parentView, ScriptEditorView.class)
                .build();

        return new Builder(scriptEditorDialogWindow);
    }

    public static class Builder {

        private String title;
        private String value;
        private CodeEditorMode mode;
        private Consumer<String> closeConsumer;
        private Procedure helpProcedure;

        private final DialogWindow<ScriptEditorView> scriptEditorViewDialogWindow;

        public Builder(DialogWindow<ScriptEditorView> scriptEditorViewDialogWindow) {
            this.scriptEditorViewDialogWindow = scriptEditorViewDialogWindow;
        }

        public Builder withEditorMode(CodeEditorMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withCloseOnClick(Consumer<String> closeOnClickConsumer) {
            this.closeConsumer = closeOnClickConsumer;
            return this;
        }

        public Builder withHelpOnClick(Procedure procedure) {
            this.helpProcedure =  procedure;
            return this;
        }

        public void open() {
            ScriptEditorView scriptEditorView = scriptEditorViewDialogWindow.getView();
            scriptEditorView.setEditorMode(mode);
            scriptEditorView.setValue(value);
            scriptEditorView.setOkButtonConsumer(value -> closeConsumer.accept(value));

            if (title != null) {
                scriptEditorView.setTitle(title);
            }

            if (helpProcedure != null) {
                scriptEditorView.setHelpBtnClickListener(event -> helpProcedure.invoke());
            }

            scriptEditorViewDialogWindow.open();
        }
    }
}
