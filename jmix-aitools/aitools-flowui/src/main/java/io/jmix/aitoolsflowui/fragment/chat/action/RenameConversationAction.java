/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitoolsflowui.fragment.chat.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableConsumer;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Renames a conversation: asks the user for a new title in a dialog and saves it. A listener added to the action
 * replaces that default behaviour.
 */
@ActionType(RenameConversationAction.ID)
public class RenameConversationAction extends BaseAction<RenameConversationAction> {

    public static final String ID = "aitls_renameConversation";

    private static final String MESSAGE_GROUP = "io.jmix.aitoolsflowui.fragment.chat";
    protected static final String TITLE_PARAMETER = "title";

    protected Messages messages;
    protected Dialogs dialogs;
    protected AiConversationService conversationService;

    @Nullable
    protected AiConversation conversation;
    @Nullable
    protected SerializableConsumer<AiConversation> afterSaveHandler;

    public RenameConversationAction() {
        this(ID);
    }

    public RenameConversationAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage(MESSAGE_GROUP, "aiChatFragment.editConversationTitleAction");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.PENCIL);
        }
    }

    @Autowired
    protected void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    protected void setConversationService(AiConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Sets the conversation to rename. Without one the action does nothing.
     *
     * @param conversation the conversation, or {@code null}
     */
    public void setConversation(@Nullable AiConversation conversation) {
        this.conversation = conversation;
    }

    /**
     * Sets the handler that receives the conversation once its new title is saved.
     *
     * @param afterSaveHandler receives the saved conversation, or {@code null} to remove the handler
     */
    public void setAfterSaveHandler(@Nullable SerializableConsumer<AiConversation> afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void actionPerform(Component component) {
        if (!hasListener(ActionPerformedEvent.class)) {
            execute(component);
        } else {
            super.actionPerform(component);
        }
    }

    protected void execute(Component component) {
        if (conversation == null || !isEnabled() || !isVisible()) {
            return;
        }

        View<?> view = UiComponentUtils.findView(component);
        if (view == null) {
            throw new IllegalStateException("Component is not attached to a view");
        }

        String currentTitle = conversation.getTitle() == null ? "" : conversation.getTitle();
        dialogs.createInputDialog(view)
                .withHeader(getMessage("aiChatFragment.editConversationTitleDialog.header"))
                .withLabelsPosition(Dialogs.InputDialogBuilder.LabelsPosition.TOP)
                .withParameters(
                        InputParameter.stringParameter(TITLE_PARAMETER)
                                .withLabel(getMessage("aiChatFragment.editConversationTitleDialog.titleField"))
                                .withRequired(true)
                                .withDefaultValue(currentTitle)
                )
                .withValidator(this::validateTitle)
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        saveTitle(closeEvent.getValue(TITLE_PARAMETER));
                    }
                })
                .open();
    }

    protected void saveTitle(@Nullable String title) {
        if (conversation == null || title == null || title.isBlank()) {
            return;
        }

        conversation.setTitle(title.trim());
        // Persist only the title: the caller refreshes what shows it.
        AiConversation saved = conversationService.save(conversation);
        conversation = saved;

        if (afterSaveHandler != null) {
            afterSaveHandler.accept(saved);
        }
    }

    protected ValidationErrors validateTitle(InputDialog.ValidationContext context) {
        String title = context.getValue(TITLE_PARAMETER);
        if (title != null && title.trim().length() > AiConversation.TITLE_MAX_LENGTH) {
            return ValidationErrors.of(messages.formatMessage(MESSAGE_GROUP,
                    "aiChatFragment.editConversationTitleDialog.titleTooLong", AiConversation.TITLE_MAX_LENGTH));
        }
        return ValidationErrors.none();
    }

    private String getMessage(String key) {
        return messages.getMessage(MESSAGE_GROUP, key);
    }
}
