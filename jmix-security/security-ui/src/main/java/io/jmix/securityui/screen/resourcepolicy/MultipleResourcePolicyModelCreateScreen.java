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

package io.jmix.securityui.screen.resourcepolicy;

import io.jmix.core.Messages;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.component.Window;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenValidation;
import io.jmix.ui.screen.StandardCloseAction;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.util.UnknownOperationResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * A base class for screens that create multiple database resource policies ({@link EntityResourcePolicyModelCreate} and
 * {@link EntityAttributeResourcePolicyModelCreate})
 */
public abstract class MultipleResourcePolicyModelCreateScreen extends Screen {

    public static final String COMMIT_ACTION_ID = "commit";
    public static final String CANCEL_ACTION_ID = "cancel";
    @Autowired
    private ScreenValidation screenValidation;

    @Subscribe
    public void onMultipleResourcePolicyModelCreateScreenInit(InitEvent event) {
        initScreenActions();
    }

    protected void initScreenActions() {
        Window window = getWindow();

        Messages messages = getApplicationContext().getBean(Messages.class);
        Icons icons = getApplicationContext().getBean(Icons.class);

        String commitShortcut = getApplicationContext().getBean(UiScreenProperties.class).getCommitShortcut();

        Action commitAndCloseAction = new BaseAction(COMMIT_ACTION_ID)
                .withCaption(messages.getMessage("actions.Ok"))
                .withIcon(icons.get(JmixIcon.EDITOR_OK))
                .withPrimary(true)
                .withShortcut(commitShortcut)
                .withHandler(actionPerformedEvent -> {
                    ValidationErrors validationErrors = validateScreen();
                    if (!validationErrors.isEmpty()) {
                        screenValidation.showValidationErrors(this, validationErrors);
                    } else {
                        close(new StandardCloseAction(COMMIT_ACTION_ID));
                    }
                });

        window.addAction(commitAndCloseAction);
        Action closeAction = new BaseAction(CANCEL_ACTION_ID)
                .withIcon(icons.get(JmixIcon.EDITOR_CANCEL))
                .withCaption(messages.getMessage("actions.Cancel"))
                .withHandler(actionPerformedEvent -> {
                    if (this.hasUnsavedChanges()) {
                        UnknownOperationResult result = new UnknownOperationResult();
                        screenValidation.showSaveConfirmationDialog(MultipleResourcePolicyModelCreateScreen.this,
                                        new StandardCloseAction(Window.CLOSE_ACTION_ID))
                                .onCommit(() -> result.resume(close(WINDOW_COMMIT_AND_CLOSE_ACTION)))
                                .onDiscard(() -> result.resume(close(WINDOW_DISCARD_AND_CLOSE_ACTION)))
                                .onCancel(result::fail);
                    } else {
                        close(new StandardCloseAction(CANCEL_ACTION_ID));
                    }
                });

        window.addAction(closeAction);
    }

    protected abstract ValidationErrors validateScreen();

    public abstract List<ResourcePolicyModel> getResourcePolicies();

    public abstract boolean hasUnsavedChanges();
}
