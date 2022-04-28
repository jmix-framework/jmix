package io.jmix.flowui.impl;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.FlowUiScreenProperties;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.EnumSet;

@Component("flowui_Dialogs")
public class DialogsImpl implements Dialogs {

    protected Messages messages;
    protected FlowUiScreenProperties flowUiScreenProperties;

    public DialogsImpl(Messages messages, FlowUiScreenProperties flowUiScreenProperties) {
        this.messages = messages;
        this.flowUiScreenProperties = flowUiScreenProperties;
    }

    @Override
    public OptionDialogBuilder createOptionDialog() {
        return new OptionDialogBuilderImpl();
    }

    @Override
    public MessageDialogBuilder createMessageDialog() {
        return new MessageDialogBuilderImpl();
    }

    // todo gd Use ActionBinder instead?
    protected Button createButton(Action action) {
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

        if (action.getIcon() == null) {
            button.setIcon(null);
        } else {
            button.setIcon(new Icon(action.getIcon()));
        }

        return button;
    }

    public class OptionDialogBuilderImpl implements OptionDialogBuilder {

        protected Dialog dialog;
        protected H3 header;
        protected com.vaadin.flow.component.Component text;
        protected HorizontalLayout buttonsContainer;

        protected Action[] actions;

        public OptionDialogBuilderImpl() {
            dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setWidth("460px"); // todo: use theme variable

            header = new H3();
            header.setWidthFull();

            buttonsContainer = new HorizontalLayout();
            buttonsContainer.setSpacing(true);
            buttonsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

            dialog.add(header, buttonsContainer);
        }

        @Override
        public OptionDialogBuilder withHeader(String header) {
            this.header.setText(header);
            return this;
        }

        @Nullable
        @Override
        public String getHeader() {
            return header.getText();
        }

        @Override
        public OptionDialogBuilder withText(String text) {
            if (this.text != null) {
                dialog.remove(this.text);
            }

            this.text = new Paragraph(text);
            dialog.addComponentAtIndex(1, this.text);
            return this;
        }

        @Nullable
        @Override
        public String getText() {
            return text instanceof Paragraph ? ((Paragraph) text).getText() : null;
        }

        @Override
        public OptionDialogBuilder withContent(com.vaadin.flow.component.Component content) {
            if (text != null) {
                dialog.remove(text);
            }

            dialog.addComponentAtIndex(1, content);
            return this;
        }

        @Nullable
        @Override
        public com.vaadin.flow.component.Component getContent() {
            return text;
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

        @Override
        public String getThemeName() {
            return dialog.getThemeName();
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
            for (Action action : actions) {
                Button button = createButton(action);
                button.addClickListener(event -> {
                    action.actionPerform(dialog);
                    dialog.close();
                });

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

                buttonsContainer.add(button);


                if (action == firstCommitAction) {
                    KeyCombination commitShortcut = KeyCombination.create(flowUiScreenProperties.getCommitShortcut());
                    button.addClickShortcut(commitShortcut.getKey(), commitShortcut.getKeyModifiers());
                } else if (action == firstDeclineAction) {
                    KeyCombination closeShortcut = KeyCombination.create(flowUiScreenProperties.getCloseShortcut());
                    button.addClickShortcut(closeShortcut.getKey(), closeShortcut.getKeyModifiers());
                }
            }

            if (!hasPrimaryAction && actions.length > 0) {
                ((Focusable<?>) buttonsContainer.getComponentAt(0)).focus();
            }

            dialog.open();
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
        protected H3 header;
        protected Button okButton;
        protected com.vaadin.flow.component.Component text;

        public MessageDialogBuilderImpl() {
            dialog = new Dialog();
            dialog.setDraggable(true);
            dialog.setWidth("460px"); // todo: use theme variable

            header = new H3();
            header.setWidthFull();

            HorizontalLayout buttonsContainer = new HorizontalLayout();
            buttonsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

            DialogAction okAction = new DialogAction(DialogAction.Type.OK);
            okButton = createButton(okAction);
            okButton.addClickListener(clickEvent -> {
                okAction.actionPerform(dialog);
                dialog.close();
            });
            KeyCombination commitShortcut = KeyCombination.create(flowUiScreenProperties.getCommitShortcut());
            okButton.addClickShortcut(commitShortcut.getKey(), commitShortcut.getKeyModifiers());
            buttonsContainer.add(okButton);

            dialog.add(header, buttonsContainer);
        }

        @Override
        public MessageDialogBuilder withHeader(String header) {
            this.header.setText(header);
            return this;
        }

        @Nullable
        @Override
        public String getHeader() {
            return header.getText();
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
            if (this.text != null) {
                dialog.remove(this.text);
            }

            this.text = new Paragraph(text);
            dialog.addComponentAtIndex(1, this.text);
            return this;
        }

        @Nullable
        @Override
        public String getText() {
            return text instanceof Paragraph ? ((Paragraph) text).getText() : null;
        }

        @Override
        public MessageDialogBuilder withContent(com.vaadin.flow.component.Component content) {
            if (text != null) {
                dialog.remove(text);
            }

            dialog.addComponentAtIndex(1, content);
            return this;
        }

        @Nullable
        @Override
        public com.vaadin.flow.component.Component getContent() {
            return text;
        }

        @Override
        public MessageDialogBuilder withThemeName(String themeName) {
            dialog.setThemeName(themeName);
            return this;
        }

        @Override
        public String getThemeName() {
            return dialog.getThemeName();
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
