/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.inputdialog;

import io.jmix.ui.Dialogs;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.Window;
import io.jmix.ui.icon.Icons;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Action can be used in {@link InputDialog}. It can handle specific {@link InputDialogActionPerformed} event for
 * managing opened dialog.
 *
 * @see InputDialog
 * @see Dialogs.InputDialogBuilder
 */
public class InputDialogAction extends AbstractAction {

    protected boolean validationRequired = true;

    public InputDialogAction(String id) {
        super(id);
    }

    @Override
    public void actionPerform(Component component) {
        if (eventHub != null) {

            InputDialog inputDialog = null;
            if (component instanceof Component.BelongToFrame) {
                Window window = ComponentsHelper.getWindow((Component.BelongToFrame) component);
                if (window != null) {
                    inputDialog = (InputDialog) window.getFrameOwner();
                }
            }

            if (!validationRequired || (inputDialog != null && inputDialog.isValid())) {
                InputDialogActionPerformed event = new InputDialogActionPerformed(this, component, inputDialog);
                eventHub.publish(InputDialogActionPerformed.class, event);
            }
        }
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
     * Set caption using fluent API method.
     *
     * @param caption caption
     * @return current instance of action
     */
    public InputDialogAction withCaption(@Nullable String caption) {
        this.caption = caption;
        return this;
    }

    /**
     * Set description using fluent API method.
     *
     * @param description description
     * @return current instance of action
     */
    public InputDialogAction withDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    /**
     * Set icon using fluent API method.
     *
     * @param icon icon
     * @return current instance of action
     */
    public InputDialogAction withIcon(@Nullable String icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Sets icon from icons set to the action (e.g. JmixIcon.DIALOG_OK).
     *
     * @param iconKey icon
     * @return current instance of action
     */
    public InputDialogAction withIcon(Icons.Icon iconKey) {
        this.icon = iconKey.source();
        return this;
    }

    /**
     * Set shortcut using fluent API method.
     *
     * @param shortcut shortcut
     * @return current instance of action
     */
    public InputDialogAction withShortcut(@Nullable String shortcut) {
        if (shortcut != null) {
            this.shortcut = KeyCombination.create(shortcut);
        }
        return this;
    }

    /**
     * Set action performed event handler using fluent API method. Can be used instead of subclassing BaseAction class.
     *
     * @param handler action performed handler
     * @return current instance of action
     */
    public InputDialogAction withHandler(Consumer<InputDialogActionPerformed> handler) {
        getEventHub().subscribe(InputDialogActionPerformed.class, handler);

        return this;
    }

    /**
     * Set whether this action is primary using fluent API method. Can be used instead of subclassing BaseAction class.
     *
     * @param primary primary
     * @return current instance of action
     */
    public InputDialogAction withPrimary(boolean primary) {
        this.primary = primary;
        return this;
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

    /**
     * Describes action performed event from {@link InputDialogAction}. It contains opened {@link InputDialog}.
     */
    public static class InputDialogActionPerformed extends Action.ActionPerformedEvent {

        protected InputDialog inputDialog;

        public InputDialogActionPerformed(Action source, Component component, @Nullable InputDialog inputDialog) {
            super(source, component);

            this.inputDialog = inputDialog;
        }

        /**
         * @return opened input dialog
         */
        @Nullable
        public InputDialog getInputDialog() {
            return inputDialog;
        }
    }
}
