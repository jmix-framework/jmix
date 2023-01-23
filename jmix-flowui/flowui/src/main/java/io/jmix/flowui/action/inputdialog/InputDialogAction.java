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

package io.jmix.flowui.action.inputdialog;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.view.ViewAction;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.View;

/**
 * Action can be used in {@link InputDialog}.
 *
 * @see InputDialog
 * @see Dialogs.InputDialogBuilder
 */
public class InputDialogAction extends ViewAction<InputDialogAction, InputDialog> {

    protected boolean validationRequired = true;
    protected InputDialog inputDialog;
    protected Component component;

    public InputDialogAction(String id) {
        super(id);
    }

    @Override
    public void actionPerform(Component component) {
        this.component = component;

        View<?> view = UiComponentUtils.findView(component);
        if (view != null) {
            inputDialog = ((InputDialog) view);
        }

        execute();
    }

    /**
     * Creates new instance of InputDialogAction.
     *
     * @param id action id
     * @return input dialog action
     */
    public static InputDialogAction action(String id) {
        return new InputDialogAction(id);
    }

    /**
     * Set to true if handler should be invoked after successful validation. False - validation won't be preformed and
     * handler will be invoked. Default value is true.
     *
     * @param validationRequired validation required option
     * @return current instance of action
     */
    public InputDialogAction withValidationRequired(boolean validationRequired) {
        this.validationRequired = validationRequired;
        return this;
    }

    /**
     * @return true if handler should be invoked after successful validation
     */
    public boolean isValidationRequired() {
        return validationRequired;
    }

    @Override
    public void execute() {
        if (!validationRequired || (inputDialog != null && inputDialog.isValid())) {
            ActionPerformedEvent event = new ActionPerformedEvent(this, component);
            getEventBus().fireEvent(event);
        }
    }
}
