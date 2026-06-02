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

package io.jmix.aitoolsflowui.view.aiconversation;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitools.service.AiConversationChatService;
import io.jmix.aitools.service.AiConversationService;
import io.jmix.core.MetadataTools;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.asynctask.UiAsyncTasks;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Route(value = "aitols-ai-conversations/:id", layout = DefaultMainViewParent.class)
@ViewController("aitols_AiConversation.detail")
@ViewDescriptor("ai-conversation-detail-view.xml")
@EditedEntityContainer("aiConversationDc")
public class AiConversationDetailView extends StandardDetailView<AiConversation> {

    private static final Logger log = LoggerFactory.getLogger(AiConversationDetailView.class);

    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private UiAsyncTasks uiAsyncTasks;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private Dialogs dialogs;

    @Autowired
    private AiConversationService aiConversationService;
    @Autowired
    private AiConversationChatService aiConversationChatService;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private InstanceContainer<AiConversation> aiConversationDc;

    @ViewComponent
    private VerticalLayout chatPanel;

    protected MessageList messageList;
    protected MessageInput messageInput;
    protected ProgressBar progressBar;

    @Subscribe
    public void onInit(final InitEvent event) {
        setShowSaveNotification(false);

        initChatComponents();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {

    }

    @Subscribe(id = "aiConversationDl", target = Target.DATA_LOADER)
    public void onAiConversationDlPostLoad(final InstanceLoader.PostLoadEvent<AiConversation> event) {
        refreshMessages();
    }

    @Subscribe("editConversationTitleBtn")
    public void onEditConversationTitleBtnClick(final ClickEvent<JmixButton> event) {
        AiConversation conversation = aiConversationDc.getItemOrNull();
        if (conversation == null) {
            return;
        }

        String currentTitle = conversation.getTitle() == null ? "" : conversation.getTitle();

        dialogs.createInputDialog(this)
                .withHeader(messageBundle.getMessage("editConversationTitleDialog.header"))
                .withLabelsPosition(Dialogs.InputDialogBuilder.LabelsPosition.TOP)
                .withParameters(
                        InputParameter.stringParameter("title")
                                .withLabel(messageBundle.getMessage("editConversationTitleDialog.titleField"))
                                .withRequired(true)
                                .withDefaultValue(currentTitle)
                )
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (!closeEvent.closedWith(DialogOutcome.OK)) {
                        return;
                    }

                    String updatedTitle = closeEvent.getValue("title");
                    if (updatedTitle == null || updatedTitle.isBlank()) {
                        return;
                    }

                    String sanitizedTitle = updatedTitle.trim();
                    AiConversation editableConversation = aiConversationDc.getItemOrNull();
                    if (editableConversation == null) {
                        return;
                    }

                    editableConversation.setTitle(sanitizedTitle);
                    getViewData().getDataContext().save();

                    reloadData();
                })
                .open();
    }

    protected void initChatComponents() {
        messageList = uiComponents.create(MessageList.class);
        messageList.setSizeFull();
        messageList.setMarkdown(true);
        messageList.addClassName("ai-conversation-message-list");

        messageInput = uiComponents.create(MessageInput.class);
        messageInput.setWidthFull();
        messageInput.addSubmitListener(this::onMessageSubmit);

        progressBar = uiComponents.create(ProgressBar.class);
        progressBar.setWidthFull();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        if (messageList.getParent().isEmpty()) {
            chatPanel.add(messageList, progressBar, messageInput);
            chatPanel.setFlexGrow(1, messageList);
        }
    }

    private void onMessageSubmit(MessageInput.SubmitEvent event) {
        String userMessage = event.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        AiConversation conversation = aiConversationDc.getItemOrNull();
        if (conversation == null) {
            log.warn("Cannot submit message: {} item is null or has no ID", AiConversation.class.getSimpleName());
            return;
        }

        ChatMessage savedUserMessage = aiConversationService.createUserMessage(conversation, userMessage);
        UUID userMessageId = savedUserMessage.getId();

        messageList.addItem(userMessageListItem(savedUserMessage.getContent(), now()));

        progressBar.setVisible(true);
        messageInput.setEnabled(false);

        uiAsyncTasks.supplierConfigurer(() -> aiConversationChatService.process(userMessageId, null))
                /*TODO: pinyazhin, application property*/
                .withTimeout(5, TimeUnit.MINUTES)
                .withResultHandler(response -> {
                    messageList.addItem(assistantMessageListItem(response, now()));
                    reloadData();
                    focusInput();
                })
                .withExceptionHandler(e -> {
                    log.error("Error processing message async", e);
                    messageList.addItem(assistantMessageListItem(messageBundle.getMessage("errorProcessingMessage"), now()));
                    focusInput();
                })
                .supplyAsync();
    }

    private MessageListItem createMessageListItem(ChatMessage message) {
        ChatMessageType messageType = message.getType();
        if (ChatMessageType.ASSISTANT.equals(messageType)) {
            return assistantMessageListItem(message.getContent(), message.getCreatedDate());
        }
        return userMessageListItem(message.getContent(), message.getCreatedDate());
    }

    protected MessageListItem userMessageListItem(String content, OffsetDateTime createdAt) {
        UserDetails user = currentAuthentication.getUser();
        String userName = resolveCurrentActorName();
        MessageListItem item = new MessageListItem(content, createdAt.toInstant(), userName);
        item.setUserAbbreviation(user.getUsername().substring(0, 1));
        item.setUserColorIndex(1);
        return item;
    }

    protected MessageListItem assistantMessageListItem(String content, @Nullable OffsetDateTime createdAt) {
        Instant date = createdAt == null ? Instant.EPOCH : createdAt.toInstant();
        MessageListItem item = new MessageListItem(content, date, messageBundle.getMessage("assistantName"));
        item.setUserColorIndex(2);
        return item;
    }

    protected String resolveCurrentActorName() {
        UserDetails user = currentAuthentication.getUser();
        String userName = metadataTools.getInstanceName(user);
        if (!userName.isBlank()) {
            return userName;
        }
        return !user.getUsername().isBlank()
                ? user.getUsername()
                : "User";
    }

    protected void refreshMessages() {
        AiConversation conversation = aiConversationDc.getItemOrNull();
        if (messageList == null || conversation == null) {
            return;
        }

        List<MessageListItem> messageListItems = conversation.getMessages().stream()
                .map(this::createMessageListItem)
                .toList();

        messageList.setItems(messageListItems);
    }

    protected void reloadData() {
        getViewData().loadAll();
        refreshMessages();
    }

    protected OffsetDateTime now() {
        return timeSource.now().toOffsetDateTime();
    }

    private void focusInput() {
        progressBar.setVisible(false);
        messageInput.setEnabled(true);
        messageInput.focus();
    }
}
