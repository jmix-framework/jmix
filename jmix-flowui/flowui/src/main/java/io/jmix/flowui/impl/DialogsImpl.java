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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
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
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.event.dialog.DialogClosedEvent;
import io.jmix.flowui.event.dialog.DialogOpenedEvent;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public DialogsImpl(ApplicationContext applicationContext, Messages messages,
                       UiViewProperties flowUiViewProperties,
                       DialogWindows dialogWindows, UiComponents uiComponents, BackgroundWorker backgroundWorker) {
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

    protected Button createButton(Action action, Dialog dialog) {
        Button button = new Button();

        if (action instanceof DialogAction dialogAction) {
            DialogAction.Type type = dialogAction.getType();

            button.setId(type.getId());
            button.addClassName(type.getId() + BUTTON_CLASS_NAMES_POSTFIX);
            button.setText(messages.getMessage(type.getMsgKey()));
            button.setIcon(type.getVaadinIcon().create());
        }

        button.setEnabled(action.isEnabled());

        if (StringUtils.isNotEmpty(action.getText())) {
            button.setText(action.getText());
        }

        if (action.getIcon() != null) {
            button.setIcon(action.getIcon());
        }

        button.addClickListener(event -> {
            action.actionPerform(dialog);
            dialog.close();
        });

        return button;
    }

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

        @Nullable
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
        public InputDialogBuilder withParameter(InputParameter parameter) {
            inputDialog.setParameter(parameter);
            return this;
        }

        @Override
        public InputDialogBuilder withParameters(InputParameter... parameters) {
            inputDialog.setParameters(parameters);
            return this;
        }

        public Collection<InputParameter> getParameters() {
            return inputDialog.getParameters();
        }

        public InputDialogBuilder withResponsiveSteps(List<ResponsiveStep> responsiveSteps) {
            inputDialog.setResponsiveSteps(responsiveSteps);
            return this;
        }

        public List<ResponsiveStep> getResponsiveSteps() {
            return inputDialog.getResponsiveSteps();
        }

        public InputDialogBuilder withLabelsPosition(LabelsPosition labelsPosition) {
            inputDialog.setLabelsPosition(labelsPosition);
            return this;
        }

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
            cancelButton.setIcon(new Icon(VaadinIcon.BAN));
            cancelButton.addClickListener(this::onCancelButtonClick);
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
}
