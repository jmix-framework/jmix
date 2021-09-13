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

/**
 * Makes UI-specific preparations to user substitution and performs it through callback passed as {@link SubstituteStep}.
 * Checks if there are screens that have unsaved changes and shows dialog window with options:
 * <ol>
 *     <li><b>Discard changes</b> (and close all windows, cleanups background tasks, then invoke all {@link SubstituteStep}s)</li>
 *     <li><b>Cancel</b> (and invoke all {@link CancelStep}s)</li>
 * </ol>
 */
public class SubstituteUserAction extends BaseAction {
    public static final String ID = "substituteUser";
    protected List<CancelStep> cancelSteps = new ArrayList<>();
    protected List<SubstituteStep> substituteSteps = new ArrayList<>();
    protected UserDetails newSubstitutedUser;
    protected UserDetails prevSubstitutedUser;

    protected Messages messages;
    protected Icons icons;

    public SubstituteUserAction(UserDetails newSubstitutedUser, UserDetails oldSubstitutedUser, Messages messages, Icons icons) {
        super(ID);
        this.messages = messages;
        this.icons = icons;

        setCaption(messages.getMessage("actions.Yes"));
        setIcon(icons.get(JmixIcon.DIALOG_OK));
        this.newSubstitutedUser = newSubstitutedUser;
        this.prevSubstitutedUser = oldSubstitutedUser;

    }


    @Override
    public void actionPerform(Component component) {
        if (AppUI.getCurrent().getScreens().hasUnsavedChanges()) {
            AppUI.getCurrent().getDialogs().createOptionDialog()
                    .withCaption(messages.getMessage("closeUnsaved.caption"))
                    .withMessage(messages.getMessage("discardChangesMessage"))
                    .withActions(
                            new BaseAction("discardChanges")
                                    .withCaption(messages.getMessage("discardChanges"))
                                    .withIcon(icons.get(JmixIcon.DIALOG_OK))
                                    .withHandler(event -> {
                                        doSubstituteUser();
                                    }),
                            new DialogAction(DialogAction.Type.CANCEL, Action.Status.PRIMARY)
                                    .withHandler(event -> cancel())
                    )
                    .show();
        } else {
            doSubstituteUser();
        }
    }

    protected void doSubstituteUser() {
        AppUI.getCurrent().getScreens().removeAll();
        AppUI.getCurrent().getApp().cleanupBackgroundTasks();
        for (SubstituteStep step : substituteSteps) {
            step.substitute(newSubstitutedUser);
        }
    }

    protected void cancel() {
        for (CancelStep step : cancelSteps) {
            step.cancel(prevSubstitutedUser);
        }
    }

    public SubstituteUserAction withCancelStep(CancelStep cancelStep) {
        cancelSteps.add(cancelStep);
        return this;
    }

    public SubstituteUserAction withSubstituteStep(SubstituteStep substituteStep) {
        substituteSteps.add(substituteStep);
        return this;
    }

    @FunctionalInterface
    public interface CancelStep {
        void cancel(UserDetails oldSubstitutedUser);
    }

    @FunctionalInterface
    public interface SubstituteStep {
        void substitute(UserDetails newSubstitutedUser);
    }
}
