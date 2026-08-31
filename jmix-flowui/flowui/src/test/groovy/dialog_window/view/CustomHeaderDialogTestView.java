/*
 * Copyright 2026 Haulmont.
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

package dialog_window.view;

import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.DialogWindowHeader;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;

@ViewController("CustomHeaderDialogTestView")
@DialogMode(width = "30em", height = "20em", resizable = true, draggable = true, maximizable = true)
public class CustomHeaderDialogTestView extends StandardView {

    public static final String CUSTOM_HEADER_BUTTON_ID = "customHeaderButton";

    @Override
    protected void configureDialogWindowHeader(DialogWindowHeader header) {
        JmixButton button = new JmixButton();
        button.setText("Custom");
        button.setId(CUSTOM_HEADER_BUTTON_ID);

        header.add(button);
    }
}
