/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.component.mainwindow.impl;

import io.jmix.core.Messages;
import io.jmix.core.security.UserSubstitutionManager;
import io.jmix.ui.AppUI;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Makes UI-specific preparations to user substitution and performs it using {@link UserSubstitutionManager}
 * Checks if there are screens that have unsaved changes and shows dialog window with options:
 * <ol>
 *     <li><b>Discard changes</b> (and close all windows, cleanups background tasks, then performs substitution and recreates main window)</li>
 *     <li><b>Cancel</b> (invokes all {@code cancelAction}s)</li>
 * </ol>
 */
public class SubstituteUserAction extends BaseAction {

    public static final String ID = "substituteUser";
    protected List<Consumer<UserDetails>> cancelActions = new ArrayList<>();
    protected UserDetails newSubstitutedUser;
    protected UserDetails prevSubstitutedUser;

    protected Messages messages;
    protected Icons icons;
    protected UserSubstitutionManager substitutionManager;

    public SubstituteUserAction(UserDetails newSubstitutedUser,
                                UserDetails oldSubstitutedUser,
                                Messages messages,
                                Icons icons,
                                UserSubstitutionManager substitutionManager) {
        super(ID);
        this.messages = messages;
        this.icons = icons;
        this.substitutionManager = substitutionManager;

        setCaption(messages.getMessage("actions.Yes"));
        setIcon(icons.get(JmixIcon.DIALOG_OK));
        this.newSubstitutedUser = newSubstitutedUser;
        this.prevSubstitutedUser = oldSubstitutedUser;

    }


    @Override
    public void actionPerform(Component component) {

        AppUI currentUI = AppUI.getCurrent();
        if (currentUI == null)
            return;

        if (currentUI.getScreens().hasUnsavedChanges()) {
            currentUI.getDialogs().createOptionDialog()
                    .withCaption(messages.getMessage("closeUnsaved.caption"))
                    .withMessage(messages.getMessage("discardChangesMessage"))
                    .withActions(
                            new BaseAction("discardChanges")
                                    .withCaption(messages.getMessage("discardChanges"))
                                    .withIcon(icons.get(JmixIcon.DIALOG_OK))
                                    .withHandler(event -> {
                                        doSubstituteUser(currentUI);
                                    }),
                            new DialogAction(DialogAction.Type.CANCEL, Action.Status.PRIMARY)
                                    .withHandler(event -> cancel())
                    )
                    .show();
        } else {
            doSubstituteUser(currentUI);
        }
    }

    protected void doSubstituteUser(AppUI currentUI) {
        currentUI.getApp().removeAllWindows();
        currentUI.getApp().cleanupBackgroundTasks();
        substitutionManager.substituteUser(newSubstitutedUser.getUsername());
        currentUI.getApp().createTopLevelWindow(currentUI);
    }

    protected void cancel() {
        for (Consumer<UserDetails> action : cancelActions) {
            action.accept(prevSubstitutedUser);
        }
    }

    public SubstituteUserAction withCancelAction(Consumer<UserDetails> cancelStep) {
        cancelActions.add(cancelStep);
        return this;
    }
}
