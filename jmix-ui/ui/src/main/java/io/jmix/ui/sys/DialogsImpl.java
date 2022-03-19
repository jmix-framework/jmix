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

package io.jmix.ui.sys;

import com.vaadin.server.Sizeable;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.ui.AppUI;
import io.jmix.ui.Dialogs;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.app.backgroundwork.BackgroundWorkDialog;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.*;
import io.jmix.ui.component.inputdialog.InputDialogAction;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.sanitizer.HtmlSanitizer;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.widget.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.ui.component.ComponentsHelper.setClickShortcut;
import static io.jmix.ui.component.impl.WrapperUtils.*;
import static io.jmix.ui.theme.ThemeClassNames.PRIMARY_ACTION;

@UIScope
@Component("ui_Dialogs")
public class DialogsImpl implements Dialogs {

    @Autowired
    protected Messages messages;
    @Autowired
    protected BackgroundWorker backgroundWorker;
    @Autowired
    protected IconResolver iconResolver;
    @Autowired
    protected Icons icons;
    @Autowired
    protected UiComponentProperties componentProperties;
    @Autowired
    protected UiScreenProperties screenProperties;
    @Autowired
    protected HtmlSanitizer htmlSanitizer;
    @Autowired
    protected ApplicationContext applicationContext;

    protected AppUI ui;
    protected ScreenBuilders screenBuilders;

    @Autowired
    public void setAppUi(AppUI ui) {
        this.ui = ui;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public OptionDialogBuilder createOptionDialog() {
        backgroundWorker.checkUIAccess();

        return new OptionDialogBuilderImpl();
    }

    @Override
    public MessageDialogBuilder createMessageDialog() {
        backgroundWorker.checkUIAccess();

        return new MessageDialogBuilderImpl();
    }

    @Override
    public ExceptionDialogBuilder createExceptionDialog() {
        backgroundWorker.checkUIAccess();

        return new ExceptionDialogBuilderImpl();
    }

    @Override
    public InputDialogBuilder createInputDialog(FrameOwner owner) {
        return new InputDialogBuilderImpl(owner);
    }

    @Override
    public BackgroundWorkDialogBuilder createBackgroundWorkDialog(FrameOwner owner, BackgroundTask backgroundTask) {
        return new BackgroundWorkDialogBuilderImpl(owner, backgroundTask);
    }

    public JmixButton createButton(Action action) {
        JmixButton button = new JmixButton();

        if (action instanceof DialogAction) {
            DialogAction.Type type = ((DialogAction) action).getType();
            if (type != null) {
                button.setCaption(messages.getMessage(type.getMsgKey()));
                String iconPath = icons.get(type.getIconKey());
                button.setIcon(iconResolver.getIconResource(iconPath));
            }
            button.setStyleName(((DialogAction) action).getStyleName());
        }

        button.setEnabled(action.isEnabled());

        if (StringUtils.isNotEmpty(action.getCaption())) {
            button.setCaption(action.getCaption());
        }
        if (StringUtils.isNotEmpty(action.getDescription())) {
            button.setDescription(action.getDescription());
        }
        if (StringUtils.isNotEmpty(action.getIcon())) {
            button.setIcon(iconResolver.getIconResource(action.getIcon()));
        }

        return button;
    }

    public class OptionDialogBuilderImpl implements OptionDialogBuilder {

        protected JmixWindow window;
        protected JmixLabel messageLabel;
        protected VerticalLayout layout;
        protected HorizontalLayout buttonsContainer;

        protected boolean htmlSanitizerEnabled = componentProperties.isHtmlSanitizerEnabled();

        protected Action[] actions;

        public OptionDialogBuilderImpl() {
            window = new JmixWindow();

            window.setModal(true);
            window.setClosable(false);
            window.setResizable(false);

            messageLabel = new JmixLabel();
            messageLabel.setWidth(100, Sizeable.Unit.PERCENTAGE);

            layout = new VerticalLayout();
            layout.setStyleName("jmix-app-option-dialog");
            layout.setMargin(false);
            layout.setSpacing(true);

            buttonsContainer = new HorizontalLayout();
            buttonsContainer.setMargin(false);
            buttonsContainer.setSpacing(true);

            layout.addComponent(messageLabel);
            layout.addComponent(buttonsContainer);

            layout.setExpandRatio(messageLabel, 1);
            layout.setComponentAlignment(buttonsContainer, Alignment.BOTTOM_RIGHT);

            window.setContent(layout);

            ThemeConstants theme = ui.getApp().getThemeConstants();
            window.setWidth(theme.get("jmix.ui.WebWindowManager.optionDialog.width"));
        }

        @Override
        public OptionDialogBuilder withCaption(String caption) {
            window.setCaption(caption);
            return this;
        }

        @Nullable
        @Override
        public String getCaption() {
            return window.getCaption();
        }

        @Override
        public OptionDialogBuilder withMessage(String message) {
            messageLabel.setValue(message);
            return this;
        }

        @Override
        public String getMessage() {
            return messageLabel.getValue();
        }

        @Override
        public OptionDialogBuilder withContentMode(ContentMode contentMode) {
            messageLabel.setContentMode(toVaadinContentMode(contentMode));
            return this;
        }

        @Override
        public ContentMode getContentMode() {
            return toContentMode(messageLabel.getContentMode());
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
        public OptionDialogBuilder withWidth(String width) {
            window.setWidth(width);

            if (getWidth() < 0) {
                messageLabel.setWidthUndefined();
                layout.setWidthUndefined();
            } else {
                messageLabel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
            }

            return this;
        }

        @Override
        public float getWidth() {
            return window.getWidth();
        }

        @Override
        public SizeUnit getWidthSizeUnit() {
            return toSizeUnit(window.getWidthUnits());
        }

        @Override
        public OptionDialogBuilder withHeight(String height) {
            window.setHeight(height);

            if (getHeight() < 0) {
                messageLabel.setHeightUndefined();
                layout.setExpandRatio(messageLabel, 0);
                layout.setHeightUndefined();
            } else {
                messageLabel.setHeight(100, Sizeable.Unit.PERCENTAGE);
                layout.setHeight(100, Sizeable.Unit.PERCENTAGE);
                layout.setExpandRatio(messageLabel, 1);
            }

            return this;
        }

        @Override
        public float getHeight() {
            return window.getHeight();
        }

        @Override
        public SizeUnit getHeightSizeUnit() {
            return toSizeUnit(window.getHeightUnits());
        }

        @Override
        public OptionDialogBuilder withWindowMode(WindowMode windowMode) {
            window.setWindowMode(toVaadinWindowMode(windowMode));
            return this;
        }

        @Override
        public WindowMode getWindowMode() {
            return fromVaadinWindowMode(window.getWindowMode());
        }

        @Override
        public OptionDialogBuilder withStyleName(String styleName) {
            window.setStyleName(styleName);
            return this;
        }

        @Override
        public String getStyleName() {
            return window.getStyleName();
        }

        @Override
        public OptionDialogBuilder withHtmlSanitizer(boolean htmlSanitizerEnabled) {
            this.htmlSanitizerEnabled = htmlSanitizerEnabled;
            return this;
        }

        @Override
        public boolean isHtmlSanitizerEnabled() {
            return htmlSanitizerEnabled;
        }

        @Override
        public void show() {
            // find OK / CANCEL shortcut actions
            DialogAction firstCommitAction = findFirstActionWithType(actions,
                    EnumSet.of(DialogAction.Type.YES, DialogAction.Type.OK)
            );
            DialogAction firstDeclineAction = findFirstActionWithType(actions,
                    EnumSet.of(DialogAction.Type.CANCEL, DialogAction.Type.CLOSE, DialogAction.Type.NO)
            );

            boolean hasPrimaryAction = false;
            for (Action action : actions) {
                JmixButton button = createButton(action);
                button.setClickHandler(mouseEventDetails -> {
                    try {
                        action.actionPerform(ui.getTopLevelWindow());
                    } finally {
                        ui.removeWindow(window);
                    }
                });

                if (action instanceof AbstractAction
                        && ((AbstractAction) action).isPrimary()) {
                    button.addStyleName(PRIMARY_ACTION);
                    button.focus();

                    hasPrimaryAction = true;
                }

                buttonsContainer.addComponent(button);

                if (ui.isTestMode()) {
                    button.setJTestId("optionDialog_" + action.getId());
                }
                if (ui.isPerformanceTestMode()) {
                    button.setId(ui.getTestIdManager().getTestId("optionDialog_" + action.getId()));
                }

                if (action == firstCommitAction) {
                    setClickShortcut(button, screenProperties.getCommitShortcut());
                } else if (action == firstDeclineAction) {
                    setClickShortcut(button, screenProperties.getCloseShortcut());
                }
            }

            if (!hasPrimaryAction && actions.length > 0) {
                ((com.vaadin.ui.Component.Focusable) buttonsContainer.getComponent(0)).focus();
            }

            if (ui.isTestMode()) {
                window.setJTestId("optionDialog");
                messageLabel.setJTestId("optionDialogLabel");
            }
            if (ui.isPerformanceTestMode()) {
                window.setId(ui.getTestIdManager().getTestId("optionDialog"));
            }

            if (getContentMode() == ContentMode.HTML
                    && isHtmlSanitizerEnabled()) {
                String sanitizedValue = htmlSanitizer.sanitize(messageLabel.getValue());
                messageLabel.setValue(sanitizedValue);
            }

            ui.addWindow(window);
            window.center();
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
    }

    public class MessageDialogBuilderImpl implements MessageDialogBuilder {
        protected JmixWindow window;
        protected JmixLabel messageLabel;
        protected VerticalLayout layout;
        protected JmixButton okButton;

        protected boolean htmlSanitizerEnabled = componentProperties.isHtmlSanitizerEnabled();

        public MessageDialogBuilderImpl() {
            window = new JmixWindow();

            window.setModal(true);
            window.setResizable(false);

            layout = new VerticalLayout();
            layout.setStyleName("jmix-app-message-dialog");
            layout.setMargin(false);
            layout.setSpacing(true);

            messageLabel = new JmixLabel();
            messageLabel.setStyleName("jmix-app-message-dialog-text");
            messageLabel.setWidth(100, Sizeable.Unit.PERCENTAGE);

            DialogAction action = new DialogAction(DialogAction.Type.OK);
            okButton = createButton(action);
            okButton.setClickHandler(me -> {
                try {
                    action.actionPerform(ui.getTopLevelWindow());
                } finally {
                    ui.removeWindow(window);
                }
            });

            layout.addComponent(messageLabel);

            layout.addComponent(okButton);
            layout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);

            window.setContent(layout);

            ThemeConstants theme = ui.getApp().getThemeConstants();
            window.setWidth(theme.get("jmix.ui.WebWindowManager.messageDialog.width"));
        }

        @Override
        public MessageDialogBuilder withCaption(String caption) {
            window.setCaption(caption);
            return this;
        }

        @Override
        public String getCaption() {
            return window.getCaption();
        }

        @Override
        public MessageDialogBuilder withMessage(String message) {
            messageLabel.setValue(message);
            return this;
        }

        @Override
        public String getMessage() {
            return messageLabel.getValue();
        }

        @Override
        public MessageDialogBuilder withContentMode(ContentMode contentMode) {
            messageLabel.setContentMode(toVaadinContentMode(contentMode));
            return this;
        }

        @Override
        public ContentMode getContentMode() {
            return toContentMode(messageLabel.getContentMode());
        }

        @Override
        public MessageDialogBuilder withWidth(String width) {
            window.setWidth(width);

            if (getWidth() < 0) {
                messageLabel.setWidthUndefined();
                layout.setWidthUndefined();
            } else {
                messageLabel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
            }

            return this;
        }

        @Override
        public float getWidth() {
            return window.getWidth();
        }

        @Override
        public SizeUnit getWidthSizeUnit() {
            return toSizeUnit(window.getWidthUnits());
        }

        @Override
        public MessageDialogBuilder withHeight(String height) {
            window.setHeight(height);

            if (getHeight() < 0) {
                messageLabel.setHeightUndefined();
                layout.setExpandRatio(messageLabel, 0);
                layout.setHeightUndefined();
            } else {
                messageLabel.setHeight(100, Sizeable.Unit.PERCENTAGE);
                layout.setHeight(100, Sizeable.Unit.PERCENTAGE);
                layout.setExpandRatio(messageLabel, 1);
            }

            return this;
        }

        @Override
        public float getHeight() {
            return window.getHeight();
        }

        @Override
        public SizeUnit getHeightSizeUnit() {
            return toSizeUnit(window.getHeightUnits());
        }

        @Override
        public boolean isModal() {
            return window.isModal();
        }

        @Override
        public MessageDialogBuilder withModal(boolean modal) {
            window.setModal(modal);
            return this;
        }

        @Override
        public MessageDialogBuilder modal() {
            return withModal(true);
        }

        @Override
        public MessageDialogBuilder withWindowMode(WindowMode windowMode) {
            window.setWindowMode(toVaadinWindowMode(windowMode));
            return this;
        }

        @Override
        public WindowMode getWindowMode() {
            return fromVaadinWindowMode(window.getWindowMode());
        }

        @Override
        public boolean isCloseOnClickOutside() {
            return window.getCloseOnClickOutside();
        }

        @Override
        public MessageDialogBuilder withCloseOnClickOutside(boolean closeOnClickOutside) {
            window.setCloseOnClickOutside(closeOnClickOutside);
            return this;
        }

        @Override
        public MessageDialogBuilder closeOnClickOutside() {
            return withCloseOnClickOutside(true);
        }

        @Override
        public MessageDialogBuilder withStyleName(String styleName) {
            window.setStyleName(styleName);
            return this;
        }

        @Override
        public String getStyleName() {
            return window.getStyleName();
        }

        @Override
        public MessageDialogBuilder withHtmlSanitizer(boolean htmlSanitizerEnabled) {
            this.htmlSanitizerEnabled = htmlSanitizerEnabled;
            return this;
        }

        @Override
        public boolean isHtmlSanitizerEnabled() {
            return htmlSanitizerEnabled;
        }

        @Override
        public void show() {
            initShortcuts();

            if (ui.isTestMode()) {
                window.setJTestId("messageDialog");
                messageLabel.setJTestId("messageDialogLabel");
                okButton.setJTestId("messageDialogOk");
            }

            if (ui.isPerformanceTestMode()) {
                window.setId(ui.getTestIdManager().getTestId("messageDialog"));
            }

            if (!window.isModal()) {
                for (com.vaadin.ui.Window w : ui.getWindows()) {
                    if (w.isModal()) {
                        window.setModal(true);
                        break;
                    }
                }
            }

            if (getContentMode() == ContentMode.HTML
                    && isHtmlSanitizerEnabled()) {
                String sanitizedValue = htmlSanitizer.sanitize(messageLabel.getValue());
                messageLabel.setValue(sanitizedValue);
            }

            ui.addWindow(window);
            window.center();
            window.bringToFront();

            okButton.focus();
        }

        protected void initShortcuts() {
            String closeShortcut = screenProperties.getCloseShortcut();
            KeyCombination closeCombination = KeyCombination.create(closeShortcut);

            window.addAction(
                    new ShortcutListenerDelegate("Esc",
                            closeCombination.getKey().getCode(),
                            KeyCombination.Modifier.codes(closeCombination.getModifiers())
                    ).withHandler((sender, target) ->
                            window.close()
                    ));

            window.addAction(new ShortcutListenerDelegate("Enter", com.vaadin.event.ShortcutAction.KeyCode.ENTER, null)
                    .withHandler((sender, target) ->
                            window.close()
                    ));
        }
    }

    public class ExceptionDialogBuilderImpl implements ExceptionDialogBuilder {
        protected String message;
        protected String caption;
        protected Throwable throwable;

        @Override
        public ExceptionDialogBuilder withThrowable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        @Override
        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public ExceptionDialogBuilder withCaption(String caption) {
            this.caption = caption;
            return this;
        }

        @Nullable
        @Override
        public String getCaption() {
            return caption;
        }

        @Override
        public ExceptionDialogBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public void show() {
            if (throwable == null) {
                throw new IllegalStateException("throwable should not be null");
            }

            Throwable rootCause = ExceptionUtils.getRootCause(throwable);
            if (rootCause == null) {
                rootCause = throwable;
            }

            ExceptionDialog dialog = new ExceptionDialog(rootCause, caption, message, applicationContext);
            for (com.vaadin.ui.Window window : ui.getWindows()) {
                if (window.isModal()) {
                    dialog.setModal(true);
                    break;
                }
            }
            ui.addWindow(dialog);
            dialog.focus();
        }
    }

    public class InputDialogBuilderImpl implements InputDialogBuilder {

        protected InputDialog inputDialog;

        public InputDialogBuilderImpl(FrameOwner owner) {
            inputDialog = screenBuilders.screen(owner)
                    .withScreenClass(InputDialog.class)
                    .withOpenMode(OpenMode.DIALOG)
                    .build();
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

        @Override
        public InputDialogBuilder withCloseListener(Consumer<InputDialog.InputDialogCloseEvent> listener) {
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
        public InputDialogBuilder withCaption(String caption) {
            inputDialog.getDialogWindow().setCaption(caption);
            return this;
        }

        @Override
        public InputDialogBuilder withWidth(String width) {
            inputDialog.getDialogWindow().setDialogWidth(width);
            return this;
        }

        public float getWidth() {
            return inputDialog.getDialogWindow().getDialogWidth();
        }

        @Override
        public SizeUnit getWidthSizeUnit() {
            return inputDialog.getDialogWindow().getWidthSizeUnit();
        }

        @Override
        public InputDialogBuilder withHeight(String height) {
            inputDialog.getDialogWindow().setDialogHeight(height);
            return this;
        }

        public float getHeight() {
            return inputDialog.getDialogWindow().getDialogHeight();
        }

        @Override
        public SizeUnit getHeightSizeUnit() {
            return inputDialog.getDialogWindow().getHeightSizeUnit();
        }

        @Nullable
        @Override
        public String getCaption() {
            return inputDialog.getDialogWindow().getCaption();
        }

        @Override
        public InputDialog show() {
            InputDialog dialog = build();
            dialog.show();
            return dialog;
        }

        @Override
        public InputDialog build() {
            return inputDialog;
        }
    }

    public class BackgroundWorkDialogBuilderImpl<T extends Number, V> implements BackgroundWorkDialogBuilder<T, V> {

        protected BackgroundWorkDialog<T, V> backgroundWorkDialog;

        public BackgroundWorkDialogBuilderImpl(FrameOwner owner, BackgroundTask<T, V> task) {
            //noinspection unchecked
            backgroundWorkDialog = screenBuilders.screen(owner)
                    .withScreenClass(BackgroundWorkDialog.class)
                    .withOpenMode(OpenMode.DIALOG)
                    .build();
            backgroundWorkDialog.setTask(task);
        }

        @Override
        public BackgroundWorkDialogBuilder<T, V> withCancelAllowed(boolean cancelAllowed) {
            backgroundWorkDialog.setCancelAllowed(cancelAllowed);
            return this;
        }

        @Override
        public boolean isCancelAllowed() {
            return backgroundWorkDialog.isCancelAllowed();
        }

        @Override
        public BackgroundWorkDialogBuilder<T, V> withTotal(Number total) {
            backgroundWorkDialog.setTotal(total);
            return this;
        }

        @Override
        public Number getTotal() {
            return backgroundWorkDialog.getTotal();
        }

        @Override
        public BackgroundWorkDialogBuilder<T, V> withShowProgressInPercentage(boolean percentProgress) {
            backgroundWorkDialog.setShowProgressInPercentage(percentProgress);
            return this;
        }

        @Override
        public boolean isShowProgressInPercentage() {
            return backgroundWorkDialog.isShowProgressInPercentage();
        }

        @Override
        public BackgroundWorkDialogBuilder<T, V> withCaption(String caption) {
            backgroundWorkDialog.getWindow().setCaption(caption);
            return this;
        }

        @Nullable
        @Override
        public String getCaption() {
            return backgroundWorkDialog.getWindow().getCaption();
        }

        @Override
        public BackgroundWorkDialogBuilder<T, V> withMessage(String message) {
            backgroundWorkDialog.setMessage(message);
            return this;
        }

        @Override
        public String getMessage() {
            return backgroundWorkDialog.getMessage();
        }

        @Override
        public BackgroundWorkDialog<T, V> show() {
            backgroundWorkDialog.show();
            return backgroundWorkDialog;
        }

        @Override
        public BackgroundWorkDialog<T, V> build() {
            return backgroundWorkDialog;
        }
    }
}
