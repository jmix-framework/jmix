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

package io.jmix.flowui.testassist.dialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.List;

/**
 * POJO class for storing dialog information.
 */
public class DialogInfo {

    protected final Dialog dialog;

    protected Component content;
    protected List<Button> buttons;

    /**
     * Creates a {@link DialogInfo} of the passed {@link Dialog}.
     *
     * @param dialog {@link Dialog} to create {@link DialogInfo}
     */
    public DialogInfo(Dialog dialog) {
        this.dialog = dialog;
    }

    /**
     * Sets the {@code content} of the {@link Dialog}.
     *
     * @param content content to set
     * @return this
     */
    protected DialogInfo withContent(Component content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the {@code buttons} list of the {@link Dialog}.
     *
     * @param buttons list of the buttons to set
     * @return this
     */
    protected DialogInfo withButtons(List<Button> buttons) {
        this.buttons = buttons;
        return this;
    }

    /**
     * @return {@link Dialog} instance
     */
    public Dialog getDialog() {
        return dialog;
    }

    /**
     * @return the {@link Dialog} content
     */
    public Component getContent() {
        return content;
    }

    /**
     * @return list of the {@link Dialog} buttons
     */
    public List<Button> getButtons() {
        return buttons;
    }
}
