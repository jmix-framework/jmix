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

package io.jmix.flowui.impl;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.*;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.inputdialog.InputDialogAction;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundTaskHandler;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.LocalizedTaskWrapper;
import io.jmix.flowui.component.sidedialog.SideDialog;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.event.dialog.DialogClosedEvent;
import io.jmix.flowui.event.dialog.DialogOpenedEvent;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.sidedialog.SideDialogPlacement;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementation of the {@link Dialogs} interface, providing methods for creating different types of dialogs.
 */
@org.springframework.stereotype.Component("flowui_Dialogs")
public class DialogsImpl implements Dialogs {

    protected static final String MIN_WIDTH = "25em";
    protected static final String BUTTON_CLASS_NAMES_POSTFIX = "-button";

    protected Messages messages;
    protected UiViewProperties flowUiViewProperties;
    protected DialogWindows dialogWindows;
    protected UiComponents uiComponents;
    protected BackgroundWorker backgroundWorker;
    protected ApplicationContext applicationContext;

    public DialogsImpl(ApplicationContext applicationContext,
                       Messages messages,
                       UiViewProperties flowUiViewProperties,
                       DialogWindows dialogWindows,
                       UiComponents uiComponents,
                       BackgroundWorker backgroundWorker) {
        this.messages = messages;
        this.flowUiViewProperties = flowUiViewProperties;
        this.dialogWindows = dialogWindows;
        this.uiComponents = uiComponents;
        this.backgroundWorker = backgroundWorker;
        this.applicationContext = applicationContext;
    }

    @Override
    public OptionDialogBuilder createOptionDialog() {
        return new OptionDialogBuilderImpl();
    }

    @Override
    public MessageDialogBuilder createMessageDialog() {
        return new MessageDialogBuilderImpl();
    }

    @Override
    public InputDialogBuilder createInputDialog(View<?> origin) {
        return new InputDialogBuilderImpl(origin);
    }

    @Override
    public <T extends Number, V> BackgroundTaskDialogBuilder<T, V> createBackgroundTaskDialog(
            BackgroundTask<T, V> backgroundTask) {
        return new BackgroundTaskDialogBuilderImpl<>(backgroundTask);
    }

    @Override
    public SideDialogBuilder createSideDialog() {
        return new SideDialogBuilderImpl();
    }

    protected Button createButton(Action action, Dialog dialog) {
        Button button = uiComponents.create(Button.class);

        if (action instanceof DialogAction dialogAction) {
            DialogAction.Type type = dialogAction.getType();

            button.setId(type.getId());
            button.addClassName(type.getId() + BUTTON_CLASS_NAMES_POSTFIX);
            button.setText(messages.getMessage(type.getMsgKey()));
            button.setIcon(type.getIcon());
        }

        button.setEnabled(action.isEnabled());

        if (StringUtils.isNotEmpty(action.getText())) {
            button.setText(action.getText());
        }

        if (action.getIconComponent() != null) {
            button.setIcon(action.getIconComponent());
        }

        button.addClickListener(event -> {
            action.actionPerform(dialog);
            dialog.close();
        });

        return button;
    }

    /**
     * Implementation of the {@link OptionDialogBuilder} that enables configuring and building option dialogs.
     * This class provides a variety of methods to customize the appearance and behavior of the dialog,
     * including setting headers, content, dimensions, actions, and event listeners.
     */
    public class OptionDialogBuilderImpl implements OptionDialogBuilder {

        protected Dialog dialog;
        protected Component content;

        protected Action[] actions;
        protected List<Button> actionButtons = new ArrayList<>(4);

        public OptionDialogBuilderImpl() {
            dialog = createDialog();
            initDialog(dialog);
        }

        protected Dialog createDialog() {
            return new Dialog();
        }

        protected void initDialog(Dialog dialog) {
            dialog.setDraggable(true);
            dialog.setCloseOnOutsideClick(false);
            dialog.setCloseOnEsc(false);
            dialog.setMinWidth(MIN_WIDTH);

            if (applicationContext.getBean(UiComponentProperties.class).isDialogsOpenedChangeEventsEnabled()) {
                dialog.addOpenedChangeListener(this::fireDialogOpenedChangeEvent);
            }
        }

        @Override
        public OptionDialogBuilder withHeader(String header) {
            dialog.setHeaderTitle(header);
            return this;
        }

        @Nullable
        @Override
        public String getHeader() {
            return dialog.getHeaderTitle();
        }

        @Override
        public OptionDialogBuilder withText(String text) {
            return withContent(new Paragraph(text));
        }

        @Nullable
        @Override
        public String getText() {
            return content instanceof Paragraph ? ((Paragraph) content).getText() : null;
        }

        @Override
        public OptionDialogBuilder withContent(Component content) {
            if (this.content != null) {
                dialog.remove(this.content);
            }

            dialog.add(content);
            this.content = content;
            return this;
        }

        @Nullable
        @Override
        public Component getContent() {
            return content;
        }

        @Override
        public OptionDialogBuilder withWidth(String width) {
            dialog.setWidth(width);
            return this;
        }

        @Override
        public String getWidth() {
            return dialog.getWidth();
        }

        @Override
        public OptionDialogBuilder withHeight(String height) {
            dialog.setHeight(height);
            return this;
        }

        @Override
        public String getHeight() {
            return dialog.getHeight();
        }

        @Override
        public OptionDialogBuilder withThemeName(String themeName) {
            dialog.setThemeName(themeName);
            return this;
        }

        @Nullable
        @Override
        public String getThemeName() {
            return dialog.getThemeName();
        }

        @Override
        public OptionDialogBuilder withClassName(@Nullable String className) {
            dialog.setClassName(className);
            return this;
        }

        @Nullable
        @Override
        public String getClassName() {
            return dialog.getClassName();
        }

        @Override
        public OptionDialogBuilder withDraggable(boolean draggable) {
            dialog.setDraggable(draggable);
            return this;
        }

        @Override
        public boolean isDraggable() {
            return dialog.isDraggable();
        }

        @Override
        public OptionDialogBuilder withDraggedListener(ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
            dialog.addDraggedListener(listener);
            return this;
        }

        @Override
        public OptionDialogBuilder withResizeListener(ComponentEventListener<Dialog.DialogResizeEvent> listener) {
            dialog.addResizeListener(listener);
            return this;
        }

        @Override
        public OptionDialogBuilder withResizable(boolean resizable) {
            dialog.setResizable(resizable);
            return this;
        }

        @Override
        public boolean isResizable() {
            return dialog.isResizable();
        }

        @Override
        public OptionDialogBuilder withMinWidth(String minWidth) {
            dialog.setMinWidth(minWidth);
            return this;
        }

        @Override
        public String getMinWidth() {
            return dialog.getMinWidth();
        }

        @Override
        public OptionDialogBuilder withMinHeight(String minHeight) {
            dialog.setMinHeight(minHeight);
            return this;
        }

        @Override
        public String getMinHeight() {
            return dialog.getMinHeight();
        }

        @Override
        public OptionDialogBuilder withMaxWidth(String maxWidth) {
            dialog.setMaxWidth(maxWidth);
            return this;
        }

        @Override
        public String getMaxWidth() {
            return dialog.getMaxWidth();
        }

        @Override
        public OptionDialogBuilder withMaxHeight(String maxHeight) {
            dialog.setMaxHeight(maxHeight);
            return this;
        }

        @Override
        public String getMaxHeight() {
            return dialog.getMaxHeight();
        }

        @Override
        public String getLeft() {
            return dialog.getLeft();
        }

        @Override
        public OptionDialogBuilder withLeft(String left) {
            dialog.setLeft(left);
            return this;
        }

        @Override
        public String getTop() {
            return dialog.getTop();
        }

        @Override
        public OptionDialogBuilder withTop(String top) {
            dialog.setTop(top);
            return this;
        }

        @Override
        public OptionDialogBuilder withActions(Action... actions) {
            this.actions = actions;
            return this;
        }

        @Override
        public Action[] getActions() {
            return actions;
        }

        @Override
        public Dialog build() {
            actionButtons.forEach(dialog.getFooter()::remove);
            actionButtons.clear();

            DialogAction firstOkAction = findFirstActionWithType(actions,
                    EnumSet.of(DialogAction.Type.YES, DialogAction.Type.OK)
            );
            DialogAction firstDeclineAction = findFirstActionWithType(actions,
                    EnumSet.of(DialogAction.Type.CANCEL, DialogAction.Type.CLOSE, DialogAction.Type.NO)
            );

            boolean hasPrimaryAction = false;
            Focusable<?> focusComponent = null;
            for (Action action : actions) {
                Button button = createButton(action, dialog);

                switch (action.getVariant()) {
                    case PRIMARY:
                        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                        button.focus();
                        hasPrimaryAction = true;
                        break;
                    case DANGER:
                        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
                        break;
                    case SUCCESS:
                        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                        break;
                    default:
                }

                initKeyCombination(firstOkAction, firstDeclineAction, action, button);

                if (focusComponent == null) {
                    focusComponent = button;
                }

                actionButtons.add(button);
                dialog.getFooter().add(button);
            }

            if (!hasPrimaryAction && focusComponent != null) {
                focusComponent.focus();
            }

            return dialog;
        }

        @Override
        public Dialog open() {
            Dialog dialog = build();
            dialog.open();
            return dialog;
        }

        protected void initKeyCombination(@Nullable DialogAction firstOkAction,
                                          @Nullable DialogAction firstDeclineAction,
                                          Action action, Button button) {
            if (action == firstOkAction) {
                KeyCombination saveShortcut = KeyCombination.create(flowUiViewProperties.getSaveShortcut());
                if (saveShortcut != null) {
                    button.addClickShortcut(saveShortcut.getKey(), saveShortcut.getKeyModifiers());
                }
            } else if (action == firstDeclineAction) {
                KeyCombination closeShortcut = KeyCombination.create(flowUiViewProperties.getCloseShortcut());
                if (closeShortcut != null) {
                    button.addClickShortcut(closeShortcut.getKey(), closeShortcut.getKeyModifiers());
                }
            }
        }

        @Nullable
        protected DialogAction findFirstActionWithType(Action[] actions, EnumSet<DialogAction.Type> types) {
            for (Action action : actions) {
                if (action instanceof DialogAction) {
                    DialogAction.Type actionType = ((DialogAction) action).getType();
                    if (types.contains(actionType)) {
                        return (DialogAction) action;
                    }
                }
            }
            return null;
        }

        protected void fireDialogOpenedChangeEvent(Dialog.OpenedChangeEvent openedChangeEvent) {
            if (openedChangeEvent.isOpened()) {
                DialogOpenedEvent dialogOpenedEvent =
                        new DialogOpenedEvent(dialog, content, Collections.unmodifiableList(actionButtons));

                applicationContext.publishEvent(dialogOpenedEvent);
            } else {
                applicationContext.publishEvent(new DialogClosedEvent(dialog));
            }
        }
    }

    /**
     * Implementation of the {@link MessageDialogBuilder} interface that provides functionality
     * for building and configuring a message dialog in a fluent manner.
     */
    public class MessageDialogBuilderImpl implements MessageDialogBuilder {

        protected Dialog dialog;
        protected Button okButton;
        protected Component content;

        public MessageDialogBuilderImpl() {
            dialog = createDialog();
            initDialog(dialog);
        }

        protected Dialog createDialog() {
            return new Dialog();
        }

        protected void initDialog(Dialog dialog) {
            dialog.setDraggable(true);
            dialog.setCloseOnOutsideClick(false);
            dialog.setMinWidth(MIN_WIDTH);

            if (applicationContext.getBean(UiComponentProperties.class).isDialogsOpenedChangeEventsEnabled()) {
                dialog.addOpenedChangeListener(this::fireDialogOpenedChangeEvent);
            }
        }

        @Override
        public MessageDialogBuilder withHeader(String header) {
            dialog.setHeaderTitle(header);
            return this;
        }

        @Override
        public String getHeader() {
            return dialog.getHeaderTitle();
        }

        @Override
        public MessageDialogBuilder withWidth(String width) {
            dialog.setWidth(width);
            return this;
        }

        @Override
        public String getWidth() {
            return dialog.getWidth();
        }

        @Override
        public MessageDialogBuilder withHeight(String height) {
            dialog.setHeight(height);
            return this;
        }

        @Override
        public String getHeight() {
            return dialog.getHeight();
        }

        @Override
        public MessageDialogBuilder withText(String text) {
            return withContent(new Paragraph(text));
        }

        @Nullable
        @Override
        public String getText() {
            return content instanceof Paragraph ? ((Paragraph) content).getText() : null;
        }

        @Override
        public MessageDialogBuilder withContent(Component content) {
            if (this.content != null) {
                dialog.remove(this.content);
            }

            dialog.add(content);
            this.content = content;
            return this;
        }

        @Nullable
        @Override
        public Component getContent() {
            return content;
        }

        @Override
        public MessageDialogBuilder withThemeName(String themeName) {
            dialog.setThemeName(themeName);
            return this;
        }

        @Nullable
        @Override
        public String getThemeName() {
            return dialog.getThemeName();
        }

        @Override
        public MessageDialogBuilder withClassName(@Nullable String className) {
            dialog.setClassName(className);
            return this;
        }

        @Nullable
        @Override
        public String getClassName() {
            return dialog.getClassName();
        }

        @Override
        public MessageDialogBuilder withCloseOnOutsideClick(boolean closeOnOutsideClick) {
            dialog.setCloseOnOutsideClick(closeOnOutsideClick);
            return this;
        }

        @Override
        public boolean isCloseOnOutsideClick() {
            return dialog.isCloseOnOutsideClick();
        }

        @Override
        public MessageDialogBuilder withCloseOnEsc(boolean closeOnEsc) {
            dialog.setCloseOnEsc(closeOnEsc);
            return this;
        }

        @Override
        public boolean isCloseOnEsc() {
            return dialog.isCloseOnEsc();
        }

        @Override
        public MessageDialogBuilder withModal(boolean modal) {
            dialog.setModal(modal);
            return this;
        }

        @Override
        public boolean isModal() {
            return dialog.isModal();
        }

        @Override
        public MessageDialogBuilder withDraggable(boolean draggable) {
            dialog.setDraggable(draggable);
            return this;
        }

        @Override
        public boolean isDraggable() {
            return dialog.isDraggable();
        }

        @Override
        public MessageDialogBuilder withDraggedListener(ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
            dialog.addDraggedListener(listener);
            return this;
        }

        @Override
        public MessageDialogBuilder withResizeListener(ComponentEventListener<Dialog.DialogResizeEvent> listener) {
            dialog.addResizeListener(listener);
            return this;
        }

        @Override
        public MessageDialogBuilder withResizable(boolean resizable) {
            dialog.setResizable(resizable);
            return this;
        }

        @Override
        public boolean isResizable() {
            return dialog.isResizable();
        }

        @Override
        public MessageDialogBuilder withMinWidth(String minWidth) {
            dialog.setMinWidth(minWidth);
            return this;
        }

        @Override
        public String getMinWidth() {
            return dialog.getMinWidth();
        }

        @Override
        public MessageDialogBuilder withMinHeight(String minHeight) {
            dialog.setMinHeight(minHeight);
            return this;
        }

        @Override
        public String getMinHeight() {
            return dialog.getMinHeight();
        }

        @Override
        public MessageDialogBuilder withMaxWidth(String maxWidth) {
            dialog.setMaxWidth(maxWidth);
            return this;
        }

        @Override
        public String getMaxWidth() {
            return dialog.getMaxWidth();
        }

        @Override
        public MessageDialogBuilder withMaxHeight(String maxHeight) {
            dialog.setMaxHeight(maxHeight);
            return this;
        }

        @Override
        public String getMaxHeight() {
            return dialog.getMaxHeight();
        }

        @Override
        public String getLeft() {
            return dialog.getLeft();
        }

        @Override
        public MessageDialogBuilder withLeft(String left) {
            dialog.setLeft(left);
            return this;
        }

        @Override
        public String getTop() {
            return dialog.getTop();
        }

        @Override
        public MessageDialogBuilder withTop(String top) {
            dialog.setTop(top);
            return this;
        }

        @Override
        public Dialog build() {
            dialog.getFooter().removeAll();

            DialogAction okAction = new DialogAction(DialogAction.Type.OK);
            okButton = createButton(okAction, dialog);

            KeyCombination saveShortcut = KeyCombination.create(flowUiViewProperties.getSaveShortcut());
            if (saveShortcut != null) {
                okButton.addClickShortcut(saveShortcut.getKey(), saveShortcut.getKeyModifiers());
            }

            dialog.getFooter().add(okButton);

            return dialog;
        }

        @Override
        public Dialog open() {
            build();
            dialog.open();
            return dialog;
        }

        protected void fireDialogOpenedChangeEvent(Dialog.OpenedChangeEvent openedChangeEvent) {
            if (openedChangeEvent.isOpened()) {
                DialogOpenedEvent dialogOpenedEvent =
                        new DialogOpenedEvent(dialog, content, Collections.singletonList(okButton));

                applicationContext.publishEvent(dialogOpenedEvent);
            } else {
                applicationContext.publishEvent(new DialogClosedEvent(dialog));
            }
        }
    }

    /**
     * Implementation of the {@link InputDialogBuilder} interface designed to facilitate the creation and customization
     * of {@link InputDialog} components. This class provides methods to configure dialog properties such as dimensions,
     * position, input parameters, actions, and other features.
     */
    public class InputDialogBuilderImpl implements InputDialogBuilder {

        protected InputDialog inputDialog;
        protected DialogWindow<InputDialog> dialogBuild;

        public InputDialogBuilderImpl(View<?> origin) {
            dialogBuild = dialogWindows.view(origin, InputDialog.class)
                    .build();
            dialogBuild.setWidth("35em");

            inputDialog = dialogBuild.getView();
        }

        @Override
        public InputDialogBuilder withHeader(String header) {
            inputDialog.setPageTitle(header);
            return this;
        }

        @Nullable
        @Override
        public String getHeader() {
            return inputDialog.getPageTitle();
        }

        @Override
        public InputDialogBuilder withWidth(String width) {
            dialogBuild.setWidth(width);
            return this;
        }

        @Override
        public String getWidth() {
            return dialogBuild.getWidth();
        }

        @Override
        public InputDialogBuilder withHeight(String height) {
            dialogBuild.setHeight(height);
            return this;
        }

        @Override
        public String getHeight() {
            return dialogBuild.getHeight();
        }

        @Override
        public String getLeft() {
            return dialogBuild.getLeft();
        }

        @Override
        public InputDialogBuilder withLeft(String left) {
            dialogBuild.setLeft(left);
            return this;
        }

        @Override
        public String getTop() {
            return dialogBuild.getTop();
        }

        @Override
        public InputDialogBuilder withTop(String top) {
            dialogBuild.setTop(top);
            return this;
        }

        @Override
        public InputDialogBuilder withDraggable(boolean draggable) {
            dialogBuild.setDraggable(draggable);
            return this;
        }

        @Override
        public boolean isDraggable() {
            return dialogBuild.isDraggable();
        }

        @Override
        public InputDialogBuilder withDraggedListener(ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
            dialogBuild.addDraggedListener(listener);
            return this;
        }

        @Override
        public InputDialogBuilder withParameter(InputParameter parameter) {
            inputDialog.setParameter(parameter);
            return this;
        }

        @Override
        public InputDialogBuilder withParameters(InputParameter... parameters) {
            inputDialog.setParameters(parameters);
            return this;
        }

        /**
         * Returns the collection of {@link InputParameter} objects associated with the {@code InputDialog}.
         *
         * @return a collection of {@link InputParameter} objects
         */
        public Collection<InputParameter> getParameters() {
            return inputDialog.getParameters();
        }

        @Override
        public InputDialogBuilder withResponsiveSteps(List<ResponsiveStep> responsiveSteps) {
            inputDialog.setResponsiveSteps(responsiveSteps);
            return this;
        }

        /**
         * Returns a list of responsive steps used in the {@code FormLayout} of the {@code InputDialog}.
         *
         * @return a {@code List} of {@link ResponsiveStep} objects representing the responsive steps
         */
        public List<ResponsiveStep> getResponsiveSteps() {
            return inputDialog.getResponsiveSteps();
        }

        @Override
        public InputDialogBuilder withLabelsPosition(LabelsPosition labelsPosition) {
            inputDialog.setLabelsPosition(labelsPosition);
            return this;
        }

        /**
         * Returns the position of labels for components in the {@link InputDialog}.
         *
         * @return the {@link LabelsPosition} of the labels
         */
        public LabelsPosition getLabelsPosition() {
            return inputDialog.getLabelsPosition();
        }

        @Override
        public InputDialogBuilder withCloseListener(ComponentEventListener<InputDialog.InputDialogCloseEvent> listener) {
            inputDialog.addCloseListener(listener);
            return this;
        }

        @Override
        public InputDialogBuilder withActions(InputDialogAction... actions) {
            inputDialog.setActions(actions);
            return this;
        }

        /**
         * Returns the collection of {@link Action} objects associated with the {@link InputDialog}.
         *
         * @return a collection of {@link Action} objects
         */
        public Collection<Action> getActions() {
            return inputDialog.getActions();
        }

        @Override
        public InputDialogBuilder withActions(DialogActions actions) {
            inputDialog.setDialogActions(actions);
            return this;
        }

        @Override
        public InputDialogBuilder withActions(DialogActions actions, Consumer<InputDialog.InputDialogResult> resultHandler) {
            inputDialog.setDialogActions(actions);
            inputDialog.setResultHandler(resultHandler);
            return this;
        }

        /**
         * Returns the predefined dialog actions associated with the input dialog.
         * By default, this is {@link DialogActions#OK_CANCEL}.
         *
         * @return the predefined dialog actions
         */
        public DialogActions getDialogActions() {
            return inputDialog.getDialogActions();
        }

        @Nullable
        public Consumer<InputDialog.InputDialogResult> getResultHandler() {
            return inputDialog.getResultHandler();
        }

        @Override
        public InputDialogBuilder withValidator(Function<InputDialog.ValidationContext, ValidationErrors> validator) {
            inputDialog.setValidator(validator);
            return this;
        }

        /**
         * Returns the validator function associated with the input dialog.
         *
         * @return the validator function associated with the input dialog
         */
        public Function<InputDialog.ValidationContext, ValidationErrors> getValidator() {
            return inputDialog.getValidator();
        }

        @Override
        public InputDialog open() {
            DialogWindow<InputDialog> build = build();
            build.open();
            return build.getView();
        }

        @Override
        public DialogWindow<InputDialog> build() {
            return dialogBuild;
        }
    }

    /**
     * Implementation of {@link BackgroundTaskDialogBuilder} that facilitates creating and managing
     * dialogs for running background tasks with progress display, cancel functionality,
     * and customizable UI elements.
     *
     * @param <T> the type of the progress update values provided by the task, extending {@link Number}
     * @param <V> the result type of the background task
     */
    public class BackgroundTaskDialogBuilderImpl<T extends Number, V> implements BackgroundTaskDialogBuilder<T, V> {

        protected Dialog dialog;
        protected VerticalLayout layout;
        protected Component content;

        protected Span progressTextSpan;
        protected ProgressBar progressBar;
        protected Button cancelButton;

        protected BackgroundTask<T, V> backgroundTask;
        protected Number total;
        protected boolean showProgressInPercentage;
        protected boolean cancelAllowed = false;

        protected Registration openedRegistration;

        protected BackgroundTaskHandler<V> taskHandler;

        public BackgroundTaskDialogBuilderImpl(BackgroundTask<T, V> backgroundTask) {
            this.backgroundTask = backgroundTask;

            dialog = createDialog();
            initDialog(dialog);
            initDialogContent(dialog);
        }

        protected Dialog createDialog() {
            return new Dialog();
        }

        protected void initDialog(Dialog dialog) {
            dialog.setHeaderTitle(messages.getMessage("backgroundWorkProgressDialog.headerTitle"));
            dialog.setDraggable(true);
            dialog.setCloseOnOutsideClick(false);
            dialog.setCloseOnEsc(false);
            dialog.setMinWidth(MIN_WIDTH);

            if (applicationContext.getBean(UiComponentProperties.class).isDialogsOpenedChangeEventsEnabled()) {
                dialog.addOpenedChangeListener(this::fireDialogOpenedChangeEvent);
            }
        }

        protected void initDialogContent(Dialog dialog) {
            layout = new VerticalLayout();
            layout.setPadding(false);

            content = uiComponents.create(Span.class);
            ((Span) content).setText(messages.getMessage("backgroundWorkProgressDialog.messageSpan.text"));
            layout.add(content);

            progressTextSpan = uiComponents.create(Span.class);
            layout.add(progressTextSpan);

            progressBar = uiComponents.create(ProgressBar.class);
            layout.add(progressBar);

            dialog.add(layout);

            cancelButton = uiComponents.create(Button.class);
            cancelButton.setText(messages.getMessage("actions.Cancel"));
            cancelButton.addClickListener(this::onCancelButtonClick);
            Icons icons = applicationContext.getBean(Icons.class);
            cancelButton.setIcon(icons.get(JmixFontIcon.DIALOG_CANCEL));
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withCancelAllowed(boolean cancelAllowed) {
            if (this.cancelAllowed != cancelAllowed) {
                this.cancelAllowed = cancelAllowed;
                if (cancelAllowed) {
                    dialog.getFooter().add(cancelButton);
                } else {
                    dialog.getFooter().remove(cancelButton);
                }
            }
            return this;
        }

        @Override
        public boolean isCancelAllowed() {
            return cancelAllowed;
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withTotal(Number total) {
            this.total = total;
            return this;
        }

        @Override
        public Number getTotal() {
            return total;
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withShowProgressInPercentage(boolean percentProgress) {
            this.showProgressInPercentage = percentProgress;
            return this;
        }

        @Override
        public boolean isShowProgressInPercentage() {
            return showProgressInPercentage;
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withHeader(String header) {
            dialog.setHeaderTitle(header);
            return this;
        }

        @Nullable
        @Override
        public String getHeader() {
            return dialog.getHeaderTitle();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withText(String text) {
            Span span = uiComponents.create(Span.class);
            span.setText(text);
            return withContent(span);
        }

        @Nullable
        @Override
        public String getText() {
            return (content instanceof Span) ? ((Span) content).getText() : null;
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withThemeName(String themeName) {
            dialog.setThemeName(themeName);
            return this;
        }

        @Nullable
        @Override
        public String getThemeName() {
            return dialog.getThemeName();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withClassName(@Nullable String className) {
            dialog.setClassName(className);
            return this;
        }

        @Nullable
        @Override
        public String getClassName() {
            return dialog.getClassName();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withDraggable(boolean draggable) {
            dialog.setDraggable(draggable);
            return this;
        }

        @Override
        public boolean isDraggable() {
            return dialog.isDraggable();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withDraggedListener(
                ComponentEventListener<Dialog.DialogDraggedEvent> listener) {
            dialog.addDraggedListener(listener);
            return this;
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withResizeListener(
                ComponentEventListener<Dialog.DialogResizeEvent> listener) {
            dialog.addResizeListener(listener);
            return this;
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withContent(Component content) {
            if (this.content != null) {
                layout.remove(this.content);
            }
            layout.addComponentAtIndex(0, content);
            this.content = content;
            return this;
        }

        @Override
        public Component getContent() {
            return content;
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withResizable(boolean resizable) {
            dialog.setResizable(resizable);
            return this;
        }

        @Override
        public boolean isResizable() {
            return dialog.isResizable();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withMinWidth(String minWidth) {
            dialog.setMinWidth(minWidth);
            return this;
        }

        @Override
        public String getMinWidth() {
            return dialog.getMinWidth();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withMinHeight(String minHeight) {
            dialog.setMinHeight(minHeight);
            return this;
        }

        @Override
        public String getMinHeight() {
            return dialog.getMinHeight();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withMaxWidth(String maxWidth) {
            dialog.setMaxWidth(maxWidth);
            return this;
        }

        @Override
        public String getMaxWidth() {
            return dialog.getMaxWidth();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withMaxHeight(String maxHeight) {
            dialog.setMaxHeight(maxHeight);
            return this;
        }

        @Override
        public String getMaxHeight() {
            return dialog.getMaxHeight();
        }

        @Override
        public String getLeft() {
            return dialog.getLeft();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withLeft(String left) {
            dialog.setLeft(left);
            return this;
        }

        @Override
        public String getTop() {
            return dialog.getTop();
        }

        @Override
        public BackgroundTaskDialogBuilder<T, V> withTop(String top) {
            dialog.setTop(top);
            return this;
        }

        /**
         * Constructs and returns a {@link Dialog} instance, configured based on the current state of the builder.
         *
         * @return the configured {@link Dialog} instance
         */
        public Dialog build() {
            if (isIndeterminateMode()) {
                progressTextSpan.setVisible(false);
                progressBar.setIndeterminate(true);
            } else {
                progressTextSpan.setVisible(true);
            }
            updateProgress(0);

            // removing the previous listener, makes sense in case of dialog rebuilding
            if (openedRegistration != null) {
                openedRegistration.remove();
            }

            openedRegistration = dialog.addOpenedChangeListener(event -> {
                // self-remove
                event.unregisterListener();

                if (event.isOpened()) {
                    startExecution();
                }
            });

            return dialog;
        }

        @Override
        public Dialog open() {
            build();
            dialog.open();
            return dialog;
        }

        protected boolean isIndeterminateMode() {
            return total == null;
        }

        @SuppressWarnings("unchecked")
        protected void startExecution() {
            LocalizedTaskWrapper<T, V> taskWrapper = applicationContext.getBean(LocalizedTaskWrapper.class, backgroundTask);
            taskWrapper.setCloseViewHandler(this::handleTaskWrapperCloseView);
            taskWrapper.addProgressListener(new BackgroundTask.ProgressListenerAdapter<>() {
                @Override
                public void onProgress(List<T> changes) {
                    if (!changes.isEmpty()) {
                        Number lastProcessedValue = changes.get(changes.size() - 1);
                        updateProgress(lastProcessedValue);
                    }
                }
            });

            taskHandler = backgroundWorker.handle(taskWrapper);
            taskHandler.execute();
        }

        protected void updateProgress(Number processedValue) {
            if (isIndeterminateMode()) {
                return;
            }

            double currentProgressValue = processedValue.doubleValue() / total.doubleValue();
            progressBar.setValue(currentProgressValue);
            if (!showProgressInPercentage) {
                progressTextSpan.setText(messages.formatMessage(StringUtils.EMPTY,
                        "backgroundWorkProgressDialog.progressTextSpan.textFormat", processedValue, total));
            } else {
                int percentValue = (int) Math.ceil(currentProgressValue * 100);
                progressTextSpan.setText(messages.formatMessage(StringUtils.EMPTY,
                        "backgroundWorkProgressDialog.progressTextSpan.percentFormat", percentValue));
            }
        }

        protected void onCancelButtonClick(ClickEvent<Button> event) {
            taskHandler.cancel();
            dialog.close();
        }

        protected void handleTaskWrapperCloseView(LocalizedTaskWrapper.CloseViewContext event) {
            dialog.close();
        }

        protected void fireDialogOpenedChangeEvent(Dialog.OpenedChangeEvent openedChangeEvent) {
            if (openedChangeEvent.isOpened()) {
                DialogOpenedEvent dialogOpenedEvent =
                        new DialogOpenedEvent(dialog, content,
                                cancelAllowed ? Collections.singletonList(cancelButton) : Collections.emptyList());

                applicationContext.publishEvent(dialogOpenedEvent);
            } else {
                applicationContext.publishEvent(new DialogClosedEvent(dialog));
            }
        }
    }

    /**
     * Implementation of {@link SideDialogBuilder} that facilitates creating and managing side dialogs.
     */
    public class SideDialogBuilderImpl implements SideDialogBuilder {

        protected SideDialog sideDialog;

        protected List<Consumer<SideDialog>> componentConsumers = new ArrayList<>();

        protected List<Component> headerComponents;
        protected List<Component> contentComponents;
        protected List<Component> footerComponents;

        public SideDialogBuilderImpl() {
            sideDialog = createSideDialog();
            initDialog(sideDialog);
        }

        @Override
        public SideDialogBuilder withHeaderComponents(Component... components) {
            componentConsumers.add((sideDialog) -> {
                headerComponents().addAll(Arrays.asList(components));
                sideDialog.getHeader().add(components);
            });

            return this;
        }

        @Override
        public SideDialogBuilder withHeaderProvider(Function<SideDialog, Component> headerProvider) {
            Preconditions.checkNotNullArgument(headerProvider, "headerProvider cannot be null");

            componentConsumers.add((sideDialog) -> {
                Component headerComponent = headerProvider.apply(sideDialog);
                if (headerComponent != null) {
                    headerComponents().add(headerComponent);
                    sideDialog.getHeader().add(headerComponent);
                }
            });

            return this;
        }

        @Override
        public SideDialogBuilder withContentComponents(Component... components) {
            componentConsumers.add((sideDialog) -> {
                contentComponents().addAll(Arrays.asList(components));
                sideDialog.add(components);
            });

            return this;
        }

        @Override
        public SideDialogBuilder withContentProvider(Function<SideDialog, Component> contentProvider) {
            Preconditions.checkNotNullArgument(contentProvider, "contentProvider cannot be null");

            componentConsumers.add((sideDialog) -> {
                Component contentComponent = contentProvider.apply(sideDialog);
                if (contentComponent != null) {
                    contentComponents().add(contentComponent);
                    sideDialog.add(contentComponent);
                }
            });

            return this;
        }

        @Override
        public SideDialogBuilder withFooterComponents(Component... components) {
            componentConsumers.add((sideDialog) -> {
                footerComponents().addAll(Arrays.asList(components));
                sideDialog.getFooter().add(components);
            });

            return this;
        }

        @Override
        public SideDialogBuilder withFooterProvider(Function<SideDialog, Component> footerProvider) {
            Preconditions.checkNotNullArgument(footerProvider, "footerProvider cannot be null");

            componentConsumers.add(sideDialog -> {
                Component footerComponent = footerProvider.apply(sideDialog);
                if (footerComponent != null) {
                    footerComponents().add(footerComponent);
                    sideDialog.getFooter().add(footerComponent);
                }
            });

            return this;
        }

        @Override
        public SideDialogBuilder withOpenedChangeListener(ComponentEventListener<Dialog.OpenedChangeEvent> listener) {
            sideDialog.addOpenedChangeListener(listener);
            return this;
        }

        @Override
        public SideDialogBuilder withCloseActionListener(ComponentEventListener<Dialog.DialogCloseActionEvent> listener) {
            sideDialog.addDialogCloseActionListener(listener);
            return this;
        }

        @Nullable
        @Override
        public String getWidth() {
            return sideDialog.getWidth();
        }

        @Override
        public SideDialogBuilder withWidth(@Nullable String value) {
            sideDialog.setWidth(value);
            return this;
        }

        @Nullable
        @Override
        public String getMaxWidth() {
            return sideDialog.getMaxWidth();
        }

        @Override
        public SideDialogBuilder withMaxWidth(@Nullable String value) {
            sideDialog.setMaxWidth(value);
            return this;
        }

        @Nullable
        @Override
        public String getMinWidth() {
            return sideDialog.getMinWidth();
        }

        @Nullable

        @Override
        public SideDialogBuilder withMinWidth(@Nullable String value) {
            sideDialog.setMinWidth(value);
            return this;
        }

        @Nullable
        @Override
        public String getHeight() {
            return sideDialog.getHeight();
        }

        @Override
        public SideDialogBuilder withHeight(@Nullable String value) {
            sideDialog.setHeight(value);
            return this;
        }

        @Nullable
        @Override
        public String getMaxHeight() {
            return sideDialog.getMaxHeight();
        }

        @Override
        public SideDialogBuilder withMaxHeight(@Nullable String value) {
            sideDialog.setMaxHeight(value);
            return this;
        }

        @Nullable
        @Override
        public String getMinHeight() {
            return sideDialog.getMinHeight();
        }

        @Override
        public SideDialogBuilder withMinHeight(@Nullable String value) {
            sideDialog.setMinHeight(value);
            return this;
        }

        @Override
        public boolean isFullscreenOnSmallDevices() {
            return sideDialog.isFullscreenOnSmallDevices();
        }

        @Override
        public SideDialogBuilder withFullscreenOnSmallDevices(boolean fullscreen) {
            sideDialog.setFullscreenOnSmallDevices(fullscreen);
            return this;
        }

        @Override
        public SideDialogPlacement getSideDialogPlacement() {
            return sideDialog.getSideDialogPlacement();
        }

        @Override
        public SideDialogBuilder withSideDialogPlacement(SideDialogPlacement placement) {
            sideDialog.setSideDialogPlacement(placement);
            return this;
        }

        @Override
        public SideDialog open() {
            sideDialog = build();
            sideDialog.open();

            return sideDialog;
        }

        @Override
        public SideDialog build() {
            sideDialog.removeAll();
            sideDialog.getHeader().removeAll();
            sideDialog.getFooter().removeAll();

            headerComponents().clear();
            contentComponents().clear();
            footerComponents().clear();

            for (Consumer<SideDialog> consumer : componentConsumers) {
                consumer.accept(sideDialog);
            }

            return sideDialog;
        }

        @Override
        public boolean isCloseOnOutsideClick() {
            return sideDialog.isCloseOnOutsideClick();
        }

        @Override
        public SideDialogBuilder withCloseOnOutsideClick(boolean closeOnOutsideClick) {
            sideDialog.setCloseOnOutsideClick(closeOnOutsideClick);
            return this;
        }

        @Override
        public boolean isCloseOnEsc() {
            return sideDialog.isCloseOnEsc();
        }

        @Override
        public SideDialogBuilder withCloseOnEsc(boolean closeOnEsc) {
            sideDialog.setCloseOnEsc(closeOnEsc);
            return this;
        }

        @Override
        public SideDialogBuilder withHeader(String header) {
            sideDialog.setHeaderTitle(header);
            return this;
        }

        @Nullable
        @Override
        public String getHeader() {
            return sideDialog.getHeaderTitle();
        }

        @Override
        public SideDialogBuilder withClassName(@Nullable String className) {
            sideDialog.setClassName(className);
            return this;
        }

        @Nullable
        @Override
        public String getClassName() {
            return sideDialog.getClassName();
        }

        protected SideDialog createSideDialog() {
            return uiComponents.create(SideDialog.class);
        }

        protected void initDialog(SideDialog dialog) {
            if (applicationContext.getBean(UiComponentProperties.class).isDialogsOpenedChangeEventsEnabled()) {
                dialog.addOpenedChangeListener(this::fireDialogOpenedChangeEvent);
            }
        }

        protected List<Component> headerComponents() {
            if (headerComponents == null) {
                headerComponents = new ArrayList<>();
            }
            return headerComponents;
        }

        protected List<Component> contentComponents() {
            if (contentComponents == null) {
                contentComponents = new ArrayList<>();
            }
            return contentComponents;
        }

        protected List<Component> footerComponents() {
            if (footerComponents == null) {
                footerComponents = new ArrayList<>();
            }
            return footerComponents;
        }

        protected void fireDialogOpenedChangeEvent(Dialog.OpenedChangeEvent openedChangeEvent) {
            if (openedChangeEvent.isOpened()) {
                List<Component> header = headerComponents == null ? Collections.emptyList() : headerComponents;
                List<Component> content = contentComponents == null ? Collections.emptyList() : contentComponents;
                List<Component> footer = footerComponents == null ? Collections.emptyList() : footerComponents;

                DialogOpenedEvent dialogOpenedEvent = new DialogOpenedEvent(sideDialog, header, content, footer);
                applicationContext.publishEvent(dialogOpenedEvent);
            } else {
                applicationContext.publishEvent(new DialogClosedEvent(sideDialog));
            }
        }
    }
}
