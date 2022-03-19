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

package io.jmix.ui.component.impl;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.Dialogs;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.*;
import io.jmix.ui.component.inputdialog.InputDialogAction;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class InputDialogFacetImpl extends AbstractFacet implements InputDialogFacet {

    protected String caption;

    protected SizeWithUnit width;
    protected SizeWithUnit height;

    protected InputParameter[] parameters;

    protected String actionId;
    protected String buttonId;

    protected DialogActions dialogActions;
    protected Consumer<InputDialog.InputDialogResult> dialogResultHandler;

    protected Collection<DialogAction<InputDialogFacet>> actions;

    protected Function<InputDialog.ValidationContext, ValidationErrors> validator;

    protected List<Consumer<InputDialog.InputDialogCloseEvent>> closeListeners = new ArrayList<>();

    protected InputDialog inputDialog;

    @Override
    public void setCaption(@Nullable String caption) {
        this.caption = caption;
    }

    @Nullable
    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setWidth(@Nullable String width) {
        this.width = SizeWithUnit.parseStringSize(width);
    }

    @Override
    public float getWidth() {
        return width.getSize();
    }

    @Override
    public SizeUnit getWidthSizeUnit() {
        return width.getUnit();
    }

    @Override
    public void setHeight(@Nullable String height) {
        this.height = SizeWithUnit.parseStringSize(height);
    }

    @Override
    public float getHeight() {
        return height.getSize();
    }

    @Override
    public SizeUnit getHeightSizeUnit() {
        return height.getUnit();
    }

    @Nullable
    @Override
    public String getActionTarget() {
        return actionId;
    }

    @Override
    public void setActionTarget(@Nullable String actionId) {
        this.actionId = actionId;
    }

    @Nullable
    @Override
    public String getButtonTarget() {
        return buttonId;
    }

    @Override
    public void setButtonTarget(@Nullable String buttonId) {
        this.buttonId = buttonId;
    }

    @Override
    public void setDialogActions(@Nullable DialogActions dialogActions) {
        this.dialogActions = dialogActions;
    }

    @Nullable
    @Override
    public DialogActions getDialogActions() {
        return dialogActions;
    }

    @Override
    public void setParameters(InputParameter... parameters) {
        this.parameters = parameters;
    }

    @Override
    public void setActions(@Nullable Collection<DialogAction<InputDialogFacet>> actions) {
        this.actions = actions;
    }

    @Nullable
    @Override
    public Collection<DialogAction<InputDialogFacet>> getActions() {
        return actions;
    }

    @Override
    public Subscription addCloseListener(Consumer<InputDialog.InputDialogCloseEvent> closeListener) {
        closeListeners.add(closeListener);
        return () -> internalRemoveCloseListener(closeListener);
    }

    protected void internalRemoveCloseListener(Consumer<InputDialog.InputDialogCloseEvent> closeListener) {
        closeListeners.remove(closeListener);
    }

    @Override
    public void setDialogResultHandler(Consumer<InputDialog.InputDialogResult> dialogResultHandler) {
        this.dialogResultHandler = dialogResultHandler;
    }

    @Override
    public void setValidator(Function<InputDialog.ValidationContext, ValidationErrors> validator) {
        this.validator = validator;
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        if (actions == null) {
            return null;
        }
        return actions.stream()
                .filter(action -> Objects.equals(action.getId(), name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public InputDialog create() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("MessageDialog is not attached to Frame");
        }

        Dialogs.InputDialogBuilder builder = UiControllerUtils.getScreenContext(owner.getFrameOwner())
                .getDialogs()
                .createInputDialog(owner.getFrameOwner());

        if (width != null) {
            builder.withWidth(width.stringValue());
        }
        if (height != null) {
            builder.withHeight(height.stringValue());
        }

        builder.withCaption(caption)
                .withParameters(parameters)
                .withValidator(validator);

        for (Consumer<InputDialog.InputDialogCloseEvent> closeListener : closeListeners) {
            builder.withCloseListener(closeListener);
        }

        if (dialogActions == null
                && CollectionUtils.isEmpty(actions)) {
            builder.withActions(DialogActions.OK_CANCEL, dialogResultHandler);
        } else {
            if (dialogActions != null) {
                builder.withActions(dialogActions, dialogResultHandler);
            } else if (CollectionUtils.isNotEmpty(actions)) {
                builder.withActions(createActions(actions));
            }
        }

        inputDialog = builder.build();

        return inputDialog;
    }

    @Override
    public InputDialog show() {
        return (InputDialog) create().show();
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        subscribe();
    }

    protected void subscribe() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Notification is not attached to Frame");
        }

        if (isNotEmpty(actionId)
                && isNotEmpty(buttonId)) {
            throw new GuiDevelopmentException(
                    "Notification facet should have either action or button target",
                    owner.getId());
        }

        if (isNotEmpty(actionId)) {
            subscribeOnAction(owner);
        } else if (isNotEmpty(buttonId)) {
            subscribeOnButton(owner);
        }
    }

    protected void subscribeOnAction(Frame owner) {
        Action action = ComponentsHelper.findAction(owner, actionId);

        if (!(action instanceof BaseAction)) {
            throw new GuiDevelopmentException(
                    String.format("Unable to find Dialog target button with id '%s'", actionId),
                    owner.getId());
        }

        ((BaseAction) action).addActionPerformedListener(e ->
                show());
    }

    protected void subscribeOnButton(Frame owner) {
        Component component = owner.getComponent(buttonId);

        if (!(component instanceof Button)) {
            throw new GuiDevelopmentException(
                    String.format("Unable to find Dialog target button with id '%s'", buttonId),
                    owner.getId());
        }

        ((Button) component).addClickListener(e ->
                show());
    }

    protected InputDialogAction[] createActions(@Nullable Collection<DialogAction<InputDialogFacet>> actions) {
        if (actions == null) {
            return new InputDialogAction[]{};
        }
        return actions.stream()
                .map(this::createAction)
                .collect(Collectors.toList())
                .toArray(new InputDialogAction[]{});
    }

    protected InputDialogAction createAction(DialogAction<InputDialogFacet> action) {
        return new InputDialogAction(action.getId())
                .withCaption(action.getCaption())
                .withDescription(action.getDescription())
                .withIcon(action.getIcon())
                .withPrimary(action.isPrimary())
                .withHandler(inputDialogActionPerformed -> {
                    if (action.getActionHandler() != null) {
                        action.getActionHandler().accept(
                                new DialogActionPerformedEvent<>(this, action));
                    }
                });
    }
}
