/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.screen;

import com.google.common.collect.Iterables;
import io.jmix.core.Messages;
import io.jmix.core.validation.group.UiCrossFieldChecks;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.UiScreenProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@Component("ui_ScreenValidation")
public class ScreenValidation {

    @Autowired
    protected UiScreenProperties screenProperties;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Icons icons;
    @Autowired
    protected Validator validator;

    /**
     * Validates UI components by invoking their {@link Validatable#validate()}.
     *
     * @param components components collection
     * @return validation errors
     */
    public ValidationErrors validateUiComponents(Collection<io.jmix.ui.component.Component> components) {
        ValidationErrors errors = new ValidationErrors();

        for (io.jmix.ui.component.Component component : components) {
            if (component instanceof Validatable) {
                Validatable validatable = (Validatable) component;
                if (validatable.isValidateOnCommit()) {
                    validate(validatable, errors);
                }
            }
        }
        return errors;
    }

    /**
     * Validates UI components by invoking their {@link Validatable#validate()}.
     *
     * @param container components container
     * @return validation errors
     */
    public ValidationErrors validateUiComponents(ComponentContainer container) {
        ValidationErrors errors = new ValidationErrors();

        ComponentsHelper.traverseValidatable(container,
                v -> validate(v, errors)
        );
        return errors;
    }

    protected void validate(Validatable validatable, ValidationErrors errors) {
        try {
            validatable.validate();
        } catch (ValidationException e) {
            Logger log = LoggerFactory.getLogger(Screen.class);

            if (log.isTraceEnabled()) {
                log.trace("Validation failed", e);
            } else if (log.isDebugEnabled()) {
                log.debug("Validation failed: " + e);
            }

            ComponentsHelper.fillErrorMessages(validatable, e, errors);
        }
    }

    /**
     * Show validation alert with passed errors and first problem UI component.
     *
     * @param origin screen controller
     * @param errors validation error
     */
    public void showValidationErrors(FrameOwner origin, ValidationErrors errors) {
        checkNotNullArgument(origin);
        checkNotNullArgument(errors);

        if (errors.isEmpty()) {
            return;
        }

        StringBuilder buffer = new StringBuilder();
        for (ValidationErrors.Item error : errors.getAll()) {
            buffer.append(error.description).append("\n");
        }

        String validationNotificationType = screenProperties.getValidationNotificationType();
        if (validationNotificationType.endsWith("_HTML")) {
            // HTML validation notification types are not supported
            validationNotificationType = validationNotificationType.replace("_HTML", "");
        }

        Notifications notifications = getScreenContext(origin).getNotifications();

        notifications.create(NotificationType.valueOf(validationNotificationType))
                .withCaption(messages.getMessage("validationFail.caption"))
                .withDescription(buffer.toString())
                .show();

        focusProblemComponent(errors);
    }

    protected void focusProblemComponent(ValidationErrors errors) {
        io.jmix.ui.component.Component component = null;
        if (!errors.getAll().isEmpty()) {
            component = errors.getFirstComponent();
        }
        if (component != null) {
            ComponentsHelper.focusComponent(component);
        }
    }

    /**
     * Validate cross-field BeanValidation rules.
     *
     * @param origin screen controller
     * @param item   item to validate
     * @return validation errors
     */
    public ValidationErrors validateCrossFieldRules(@SuppressWarnings("unused") @Nullable FrameOwner origin, Object item) {
        ValidationErrors errors = new ValidationErrors();

        Set<ConstraintViolation<Object>> violations = validator.validate(item, UiCrossFieldChecks.class);

        violations.stream()
                .filter(violation -> {
                    Path propertyPath = violation.getPropertyPath();

                    Path.Node lastNode = Iterables.getLast(propertyPath);
                    return lastNode.getKind() == ElementKind.BEAN;
                })
                .forEach(violation -> errors.add(violation.getMessage()));

        return errors;
    }

    /**
     * Shows standard unsaved changes dialog with Discard and Cancel actions.
     *
     * @param origin screen controller
     * @param closeAction close action
     * @return result
     */
    public UnsavedChangesDialogResult showUnsavedChangesDialog(FrameOwner origin,
                                                               @SuppressWarnings("unused") CloseAction closeAction) {
        UnsavedChangesDialogResult result = new UnsavedChangesDialogResult();

        Dialogs dialogs = getScreenContext(origin).getDialogs();
        dialogs.createOptionDialog()
                .withCaption(messages.getMessage("closeUnsaved.caption"))
                .withMessage(messages.getMessage("closeUnsaved"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> {

                                    result.discard();
                                }),
                        new DialogAction(DialogAction.Type.NO, Action.Status.PRIMARY)
                                .withHandler(e -> {
                                    Frame frame = UiControllerUtils.getFrame(origin);
                                    ComponentsHelper.focusChildComponent(frame);

                                    result.cancel();
                                })
                )
                .show();

        return result;
    }

    /**
     * Shows standard save confirmation dialog with Save, Discard and Cancel actions.
     *
     * @param origin screen controller
     * @param closeAction close action
     * @return result
     */
    public SaveChangesDialogResult showSaveConfirmationDialog(FrameOwner origin,
                                                              @SuppressWarnings("unused") CloseAction closeAction) {
        SaveChangesDialogResult result = new SaveChangesDialogResult();

        Dialogs dialogs = getScreenContext(origin).getDialogs();
        dialogs.createOptionDialog()
                .withCaption(messages.getMessage("closeUnsaved.caption"))
                .withMessage(messages.getMessage("saveUnsaved"))
                .withActions(
                        new DialogAction(DialogAction.Type.OK, Action.Status.PRIMARY)
                                .withCaption(messages.getMessage("closeUnsaved.save"))
                                .withHandler(e -> {

                                    result.commit();
                                }),
                        new BaseAction("discard")
                                .withIcon(icons.get(JmixIcon.DIALOG_CANCEL))
                                .withCaption(messages.getMessage("closeUnsaved.discard"))
                                .withHandler(e -> {

                                    result.discard();
                                }),
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withIcon(null)
                                .withHandler(e -> {
                                    Frame frame = UiControllerUtils.getFrame(origin);
                                    ComponentsHelper.focusChildComponent(frame);

                                    result.cancel();
                                })
                )
                .show();

        return result;
    }

    /**
     * Callbacks holder for unsaved changes dialog.
     */
    public static class UnsavedChangesDialogResult {
        protected Runnable discardHandler;
        protected Runnable cancelHandler;

        public UnsavedChangesDialogResult() {
        }

        public UnsavedChangesDialogResult onDiscard(Runnable discardHandler) {
            this.discardHandler = discardHandler;
            return this;
        }

        public UnsavedChangesDialogResult onCancel(Runnable cancelHandler) {
            this.cancelHandler = cancelHandler;
            return this;
        }

        public void discard() {
            if (discardHandler != null) {
                discardHandler.run();
            }
        }

        public void cancel() {
            if (cancelHandler != null) {
                cancelHandler.run();
            }
        }
    }

    /**
     * Callbacks holder for save changes dialog.
     */
    public static class SaveChangesDialogResult {
        protected Runnable commitHandler;
        protected Runnable discardHandler;
        protected Runnable cancelHandler;

        public SaveChangesDialogResult() {
        }

        public SaveChangesDialogResult onCommit(Runnable commitHandler) {
            this.commitHandler = commitHandler;
            return this;
        }

        public SaveChangesDialogResult onDiscard(Runnable discardHandler) {
            this.discardHandler = discardHandler;
            return this;
        }

        public SaveChangesDialogResult onCancel(Runnable cancelHandler) {
            this.cancelHandler = cancelHandler;
            return this;
        }

        public void commit() {
            if (commitHandler != null) {
                commitHandler.run();
            }
        }

        public void discard() {
            if (discardHandler != null) {
                discardHandler.run();
            }
        }

        public void cancel() {
            if (cancelHandler != null) {
                cancelHandler.run();
            }
        }
    }
}
