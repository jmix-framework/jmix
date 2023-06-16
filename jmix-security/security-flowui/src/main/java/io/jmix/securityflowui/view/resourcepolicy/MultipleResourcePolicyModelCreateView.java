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

package io.jmix.securityflowui.view.resourcepolicy;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.UnknownOperationResult;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class MultipleResourcePolicyModelCreateView extends StandardView {

    public static final String SAVE_ACTION_ID = "saveAction";

    @Autowired
    private ViewValidation viewValidation;

    @Subscribe
    public void onMultipleResourcePolicyModelCreateViewInit(InitEvent event) {
        initScreenActions();
    }

    protected void initScreenActions() {
        SecuredBaseAction saveAction = createSaveAction();

        getViewActions().addAction(saveAction);
    }

    protected SecuredBaseAction createSaveAction() {
        Messages messages = getApplicationContext().getBean(Messages.class);
        UiViewProperties flowUiViewProperties = getApplicationContext().getBean(UiViewProperties.class);

        return new SecuredBaseAction(SAVE_ACTION_ID)
                .withText(messages.getMessage("actions.Ok"))
                .withIcon(ComponentUtils.convertToIcon(VaadinIcon.CHECK))
                .withVariant(ActionVariant.PRIMARY)
                .withShortcutCombination(KeyCombination.create(flowUiViewProperties.getSaveShortcut()))
                .withHandler(this::validateAndClose);
    }

    protected void validateAndClose(ActionPerformedEvent event) {
        closeWithSave();
    }

    protected abstract ValidationErrors validateView();

    protected abstract boolean hasUnsavedChanges();

    public abstract List<ResourcePolicyModel> getResourcePolicies();

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        CloseAction action = event.getCloseAction();

        if (action instanceof ChangeTrackerCloseAction
                && ((ChangeTrackerCloseAction) action).isCheckForUnsavedChanges()
                && !event.closedWith(StandardOutcome.SAVE)
                && hasUnsavedChanges()) {
            UnknownOperationResult result = new UnknownOperationResult();

            boolean useSaveConfirmation = getApplicationContext()
                    .getBean(UiViewProperties.class).isUseSaveConfirmation();

            if (useSaveConfirmation) {
                viewValidation.showSaveConfirmationDialog(this)
                        .onSave(() -> result.resume(closeWithSave()))
                        .onDiscard(() -> result.resume(closeWithDiscard()))
                        .onCancel(result::fail);
            } else {
                viewValidation.showUnsavedChangesDialog(this)
                        .onDiscard(() -> result.resume(closeWithDiscard()))
                        .onCancel(result::fail);
            }

            event.preventClose(result);
        }
    }

    protected OperationResult closeWithSave() {
        ValidationErrors validationErrors = validateView();
        if (validationErrors.isEmpty()) {
            return close(StandardOutcome.SAVE);
        } else {
            viewValidation.showValidationErrors(validationErrors);
            return OperationResult.fail();
        }
    }

    protected OperationResult closeWithDiscard() {
        return close(StandardOutcome.DISCARD);
    }
}
