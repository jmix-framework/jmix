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

package io.jmix.flowui.event.dialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Application event that is sent after the dialog is shown.
 */
public class DialogOpenedEvent extends ApplicationEvent {

    protected Component content;
    protected List<Button> buttons;

    public DialogOpenedEvent(Dialog dialog, Component content, List<Button> singleton) {
        super(dialog);
        this.content = content;
        this.buttons = singleton;
    }

    @Override
    public Dialog getSource() {
        return ((Dialog) super.getSource());
    }

    /**
     * @return content of the opened dialog
     */
    public Component getContent() {
        return content;
    }

    /**
     * @return list of dialog buttons
     */
    public List<Button> getButtons() {
        return buttons;
    }
}
