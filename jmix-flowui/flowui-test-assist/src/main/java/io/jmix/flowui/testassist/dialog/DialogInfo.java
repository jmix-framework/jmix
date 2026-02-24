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
import io.jmix.flowui.Dialogs.SideDialogBuilder;

import java.util.List;

/**
 * POJO class for storing dialog information.
 */
public class DialogInfo {

    protected final Dialog dialog;

    protected Component content;
    protected List<Button> buttons;

    protected List<Component> headerComponents;
    protected List<Component> contentComponents;
    protected List<Component> footerComponents;

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
     * Sets the {@code headerComponents} list of the {@link Dialog}.
     *
     * @param headerComponents list of the header components to set
     * @return this
     */
    protected DialogInfo withHeaderComponents(List<Component> headerComponents) {
        this.headerComponents = headerComponents;
        return this;
    }

    /**
     * Sets the {@code contentComponents} list of the {@link Dialog}.
     *
     * @param contentComponents list of the content components to set
     * @return this
     */
    protected DialogInfo withContentComponents(List<Component> contentComponents) {
        this.contentComponents = contentComponents;
        this.content = contentComponents.isEmpty() ? null : contentComponents.get(0);
        return this;
    }

    /**
     * Sets the {@code footerComponents} list of the {@link Dialog}.
     *
     * @param footerComponents list of the footer components to set
     * @return this
     */
    protected DialogInfo withFooterComponents(List<Component> footerComponents) {
        this.footerComponents = footerComponents;
        return this;
    }

    /**
     * @return {@link Dialog} instance
     */
    public Dialog getDialog() {
        return dialog;
    }

    /**
     * Returns the {@link Dialog} content component (e.g., for message dialog, option dialog, etc.).
     * If the dialog content includes multiple components, the first component is returned.
     *
     * @return the {@link Dialog} content
     */
    public Component getContent() {
        return content;
    }

    /**
     * Returns the button list if the dialog footer contains only buttons (e.g., message dialog, option dialog, etc.).
     *
     * @return list of the {@link Dialog} buttons
     */
    public List<Button> getButtons() {
        return buttons;
    }

    /**
     * The list of header components of the {@link Dialog} if the builder supports setting components to the header
     * (e.g. {@link SideDialogBuilder}).
     *
     * @return list of header components
     */
    public List<Component> getHeaderComponents() {
        return headerComponents;
    }

    /**
     * The list of content components of the {@link Dialog}.
     *
     * @return list of content components
     */
    public List<Component> getContentComponents() {
        return contentComponents;
    }

    /**
     * The list of footer components of the {@link Dialog}.
     *
     * @return list of footer components
     */
    public List<Component> getFooterComponents() {
        return footerComponents;
    }
}
