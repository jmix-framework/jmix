package io.jmix.flowui.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.FlowuiViewProperties;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.EnumSet;

@org.springframework.stereotype.Component("flowui_Dialogs")
public class DialogsImpl implements Dialogs {

    protected static final String WIDTH = "25em";

    protected Messages messages;
    protected FlowuiViewProperties flowUiViewProperties;

    public DialogsImpl(Messages messages, FlowuiViewProperties flowUiViewProperties) {
        this.messages = messages;
        this.flowUiViewProperties = flowUiViewProperties;
    }

    @Override
    public OptionDialogBuilder createOptionDialog() {
        return new OptionDialogBuilderImpl();
    }

    @Override
    public MessageDialogBuilder createMessageDialog() {
        return new MessageDialogBuilderImpl();
    }

    protected Button createButton(Action action, Dialog dialog) {
        Button button = new Button();

        if (action instanceof DialogAction) {
            DialogAction.Type type = ((DialogAction) action).getType();
            button.setText(messages.getMessage(type.getMsgKey()));
            button.setIcon(type.getVaadinIcon().create());
        }

        button.setEnabled(action.isEnabled());

        if (StringUtils.isNotEmpty(action.getText())) {
            button.setText(action.getText());
        }

        button.setIcon(action.getIcon());

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
            dialog.setWidth(WIDTH);
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
            if (this.content != null) {
                dialog.remove(this.content);
            }

            this.content = new Paragraph(text);
            dialog.add(this.content);
            return this;
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
        public void open() {
            DialogAction firstCommitAction = findFirstActionWithType(actions,
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
                }

                initKeyCombination(firstCommitAction, firstDeclineAction, action, button);

                if (focusComponent == null) {
                    focusComponent = button;
                }

                dialog.getFooter().add(button);
            }

            if (!hasPrimaryAction && focusComponent != null) {
                focusComponent.focus();
            }

            dialog.open();
        }

        protected void initKeyCombination(@Nullable DialogAction firstCommitAction,
                                          @Nullable DialogAction firstDeclineAction,
                                          Action action, Button button) {
            if (action == firstCommitAction) {
                KeyCombination commitShortcut = KeyCombination.create(flowUiViewProperties.getCommitShortcut());
                if (commitShortcut != null) {
                    button.addClickShortcut(commitShortcut.getKey(), commitShortcut.getKeyModifiers());
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
            dialog.setWidth(WIDTH);

            HorizontalLayout buttonsContainer = new HorizontalLayout();
            buttonsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

            DialogAction okAction = new DialogAction(DialogAction.Type.OK);
            okButton = createButton(okAction, dialog);

            KeyCombination commitShortcut = KeyCombination.create(flowUiViewProperties.getCommitShortcut());
            if (commitShortcut != null) {
                okButton.addClickShortcut(commitShortcut.getKey(), commitShortcut.getKeyModifiers());
            }

            dialog.getFooter().add(okButton);
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
            if (this.content != null) {
                dialog.remove(this.content);
            }

            this.content = new Paragraph(text);
            dialog.add(this.content);
            return this;
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
        public void open() {
            dialog.open();
        }
    }
}
