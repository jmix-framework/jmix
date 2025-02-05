/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.view.htmleditor;

import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;

@ViewController("msgtmp_HtmlEditorView")
@ViewDescriptor("html-editor-view.xml")
@DialogMode(width = "75em", height = "45em", resizable = true)
public class HtmlEditorView extends StandardView implements ReadOnlyAwareView {

    @ViewComponent
    protected CodeEditor codeEditor;
    @ViewComponent
    protected JmixButton saveButton;

    protected boolean readOnly;

    public void setHtml(String html) {
        codeEditor.setValue(html);
    }

    public String getHtml() {
        return codeEditor.getValue();
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;

            codeEditor.setReadOnly(readOnly);
            saveButton.setEnabled(!readOnly);
        }
    }

    @Subscribe("saveAction")
    public void onSaveAction(ActionPerformedEvent event) {
        close(StandardOutcome.SAVE);
    }
}
