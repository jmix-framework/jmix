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

package io.jmix.flowui.view;

import com.google.common.collect.Iterables;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.exception.CompositeValidationException;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.ActionVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import java.util.*;

@org.springframework.stereotype.Component("flowui_ViewValidation")
public class ViewValidation {

    protected Messages messages;
    protected Validator validator;
    protected Dialogs dialogs;
    protected Notifications notifications;
    protected UiViewProperties viewProperties;

    @Autowired
    public ViewValidation(Messages messages,
                          Validator validator,
                          Dialogs dialogs,
                          UiViewProperties viewProperties,
                          Notifications notifications) {
        this.messages = messages;
        this.validator = validator;
        this.dialogs = dialogs;
        this.viewProperties = viewProperties;
        this.notifications = notifications;
    }

    public ValidationErrors validateUiComponents(Component container) {
        Preconditions.checkNotNullArgument(container);
        return validateUiComponents(UiComponentUtils.getComponents(container));
    }

    public ValidationErrors validateUiComponents(Collection<Component> components) {
        Preconditions.checkNotNullArgument(components);

        ValidationErrors errors = new ValidationErrors();

        for (Component component : components) {
            if (component instanceof SupportsValidation) {
                validate((SupportsValidation<?>) component).forEach(errors::add);
            }
        }
        return errors;
    }

    public ValidationErrors validateUiComponent(Component component) {
        Preconditions.checkNotNullArgument(component);

        if (component instanceof SupportsValidation) {
            ValidationErrors errors = new ValidationErrors();
            validate((SupportsValidation<?>) component).forEach(errors::add);
            return errors;
        } else {
            return ValidationErrors.none();
        }
    }

    public ValidationErrors validateBeanGroup(Class<?> groupClass, Object item) {
        ValidationErrors errors = new ValidationErrors();

        Set<ConstraintViolation<Object>> violations = validator.validate(item, groupClass);

        violations.stream()
                .filter(violation -> {
                    Path propertyPath = violation.getPropertyPath();

                    Path.Node lastNode = Iterables.getLast(propertyPath);
                    return lastNode.getKind() == ElementKind.BEAN;
                })
                .forEach(violation -> errors.add(violation.getMessage()));

        return errors;
    }

    public void showValidationErrors(ValidationErrors errors) {
        showValidationErrorsNotification(errors,
                viewProperties.getValidationNotificationDuration(),
                Notification.Position.valueOf(viewProperties.getValidationNotificationPosition()),
                Notifications.Type.valueOf(viewProperties.getValidationNotificationType()));
    }

    protected void showValidationErrorsNotification(ValidationErrors errors,
                                                    int duration,
                                                    Notification.Position position,
                                                    Notifications.Type type) {
        Preconditions.checkNotNullArgument(errors);

        if (errors.isEmpty()) {
            return;
        }

        String message = getValidationErrorsMessage(errors);
        String title = messages.getMessage("validationFail.title");

        notifications.create(title, message)
                .withType(type)
                .withPosition(position)
                .withDuration(duration)
                .show();
    }

    public void focusProblemComponent(ValidationErrors errors) {
        Preconditions.checkNotNullArgument(errors);

        Component component = null;
        if (!errors.getAll().isEmpty()) {
            component = errors.getFirstComponent();
        }
        if (component != null) {
            UiComponentUtils.focusComponent(component);
        }
    }

    public static String getValidationErrorsMessage(ValidationErrors errors) {
        Preconditions.checkNotNullArgument(errors);

        StringBuilder buffer = new StringBuilder();
        for (ValidationErrors.Item error : errors.getAll()) {
            buffer.append(error.getDescription()).append("\n");
        }
        return buffer.toString();
    }

    protected Collection<ValidationErrors.Item> validate(SupportsValidation<?> component) {
        try {
            component.executeValidators();
            return Collections.emptyList();
        } catch (ValidationException e) {
            Logger log = LoggerFactory.getLogger(View.class);

            if (log.isTraceEnabled()) {
                log.trace("Validation failed", e);
            } else if (log.isDebugEnabled()) {
                log.debug("Validation failed: " + e);
            }

            return collectValidationErrorItems(e, (Component) component);
        }
    }

    protected Collection<ValidationErrors.Item> collectValidationErrorItems(ValidationException e, Component component) {
        if (e instanceof ValidationException.HasRelatedComponent) {
            Component relatedComponent = ((ValidationException.HasRelatedComponent) e).getComponent();
            return Collections.singletonList(new ValidationErrors.Item(relatedComponent, e.getMessage()));
        } else if (e instanceof CompositeValidationException) {
            List<ValidationErrors.Item> errors = new ArrayList<>(((CompositeValidationException) e).getCauses().size());
            for (CompositeValidationException.ViolationCause cause : ((CompositeValidationException) e).getCauses()) {
                errors.add(new ValidationErrors.Item(component, cause.getMessage()));
            }
            return errors;
        } else {
            return Collections.singletonList(new ValidationErrors.Item(component, e.getMessage()));
        }
    }

    public UnsavedChangesDialogResult showUnsavedChangesDialog(View<?> origin) {
        UnsavedChangesDialogResult result = new UnsavedChangesDialogResult();

        dialogs.createOptionDialog()
                .withHeader(messages.getMessage("dialogs.closeUnsaved.title"))
                .withText(messages.getMessage("dialogs.closeUnsaved.message"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(__ -> result.discard()),
                        new DialogAction(DialogAction.Type.NO)
                                .withHandler(__ -> {
                                    UiComponentUtils.findFocusComponent(origin)
                                            .ifPresent(Focusable::focus);

                                    result.cancel();
                                })
                                .withVariant(ActionVariant.PRIMARY)
                )
                .open();

        return result;
    }

    public SaveChangesDialogResult showSaveConfirmationDialog(View<?> origin) {
        SaveChangesDialogResult result = new SaveChangesDialogResult();

        dialogs.createOptionDialog()
                .withHeader(messages.getMessage("dialogs.closeUnsaved.title"))
                .withText(messages.getMessage("dialogs.saveUnsaved.message"))
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withText(messages.getMessage("dialogs.closeUnsaved.save"))
                                .withHandler(__ -> result.save())
                                .withVariant(ActionVariant.PRIMARY),
                        new DialogAction(DialogAction.Type.CLOSE)
                                .withText(messages.getMessage("dialogs.closeUnsaved.discard"))
                                .withHandler(__ -> result.discard()),
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withIcon((Icon) null)
                                .withHandler(__ -> {
                                    UiComponentUtils.findFocusComponent(origin)
                                            .ifPresent(Focusable::focus);

                                    result.cancel();
                                })
                )
                .open();

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
        protected Runnable saveHandler;
        protected Runnable discardHandler;
        protected Runnable cancelHandler;

        public SaveChangesDialogResult() {
        }

        public SaveChangesDialogResult onSave(Runnable saveHandler) {
            this.saveHandler = saveHandler;
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

        public void save() {
            if (saveHandler != null) {
                saveHandler.run();
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
