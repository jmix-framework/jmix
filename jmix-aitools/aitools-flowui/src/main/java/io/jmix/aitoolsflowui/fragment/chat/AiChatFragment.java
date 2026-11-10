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

package io.jmix.aitoolsflowui.fragment.chat;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.fragment.chat.action.RenameConversationAction;
import io.jmix.aitoolsflowui.fragment.chat.action.ShowAssistantMemoryAction;
import io.jmix.aitoolsflowui.fragment.chat.datablock.AiChatDataBlock;
import io.jmix.aitoolsflowui.fragment.chat.datablock.HasAiChatDataBlock;
import io.jmix.aitoolsflowui.fragment.chat.memory.AssistantMemoryFragment;
import io.jmix.aitoolsflowui.fragment.chat.prompt.AiSuggestedPromptContext;
import io.jmix.aitoolsflowui.fragment.chat.prompt.AiSuggestedPromptManager;
import io.jmix.aitoolsflowui.fragment.chat.support.ConversationTitleSupport;
import io.jmix.aitoolsflowui.generation.AiChatGeneration;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.AiChatGenerationRejectedException;
import io.jmix.aitoolsflowui.generation.AiToolStatusUtils;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCancelledEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCompletedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationProgressEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.service.AiConversationAutoTitleService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.aitoolsflowui.service.KeepInContextResult;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelCloseEvent;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.*;

/**
 * The AI chat of one {@link AiConversation}. Embed it in a view or fragment and bind a conversation to it.
 * <p>
 * The fragment sends messages and follows the response that runs on the server, including after a refresh or reopen.
 * It also titles the conversation and shows the title row. The conversation itself is drawn by an
 * {@link AiChatPanel} from the first {@link AiChatPanelProvider} by order. By default, this is the add-on's timeline,
 * and another add-on can replace it with its own chat.
 * <p>
 * The title row has a menu with the conversation's actions: renaming it and, when the panel can show it
 * ({@link HasKeepInContext}), the assistant memory, i.e. the messages the assistant keeps in every reply.
 */
@Experimental
@FragmentDescriptor("ai-chat-fragment.xml")
public class AiChatFragment extends Fragment<VerticalLayout> {

    private static final Logger log = LoggerFactory.getLogger(AiChatFragment.class);

    @Autowired
    protected Messages messages;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected AiChatMessageService messageService;
    @Autowired
    protected AiConversationService conversationService;
    @Autowired
    protected AiChatGenerationManager generationManager;
    @Autowired
    protected AiChatService chatService;
    @Autowired
    protected AiToolsFlowuiProperties properties;
    @Autowired
    protected ConversationTitleSupport titleSupport;
    @Autowired
    protected AiConversationAutoTitleService autoTitleService;
    @Autowired
    protected AiChatPanelFactory chatPanelFactory;
    @Autowired
    protected Icons icons;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected Actions actions;
    @Autowired
    protected AiSuggestedPromptManager suggestedPromptManager;

    @ViewComponent
    protected H3 conversationTitle;
    @ViewComponent
    protected DropdownButton chatActionsBtn;
    @ViewComponent
    protected VerticalLayout chatPanelContainer;
    @ViewComponent
    protected SidePanelLayout sidePanel;
    @ViewComponent
    protected VerticalLayout sidePanelContent;

    @Nullable
    protected AssistantMemoryFragment assistantMemory;

    @Nullable
    protected AiChatPanel chatPanel;

    @Nullable
    protected SerializableSupplier<Component> aiAvatarIconSupplier;

    @Nullable
    protected Tooltip conversationTitleTooltip;

    @Nullable
    protected AiConversation conversation;

    @Nullable
    protected RenameConversationAction renameAction;
    @Nullable
    protected ShowAssistantMemoryAction assistantMemoryAction;

    /**
     * Whether the chat shows a response as running ("thinking").
     */
    protected boolean responseShown;

    /**
     * The response the thinking indicator stands for; {@code null} until its started event arrives.
     */
    @Nullable
    protected UUID shownGenerationId;

    /**
     * Sequence number of the last status update the indicator shows, so that a progress event already included in a
     * snapshot is not applied twice.
     */
    protected long shownStatusSequence;

    /**
     * Folded statuses of the shown response, the source of truth: they fold by the same rule as the server's
     * snapshot. The panel gets a copy through {@link AiChatPanel#showStatuses(List)}.
     */
    protected final List<AiToolStatusUpdate> thinkingStatuses = new ArrayList<>();

    /**
     * Whether the chat shows any message of the conversation; the first message sent into an empty one titles it.
     */
    protected boolean messagesShown;

    protected boolean readOnly;

    /**
     * Whether the host lets the user type. A response shown as running disables the message input regardless.
     */
    protected boolean messageInputEnabled = true;

    protected boolean chatUnavailableWarned;

    @Nullable
    protected AiConversationTitleMode titleModeOverride;

    @Nullable
    private AiChatDataBlock dataBlock;
    private List<String> dataBlockPrompts = List.of();

    private boolean dataBlockUnsupportedWarned;

    /**
     * Whether the chat starts a new conversation with the first message, see {@link #isNewConversation()}.
     */
    private boolean newConversation;

    /**
     * Binds a conversation to the chat.
     *
     * @param conversation conversation to show, or {@code null} to clear the chat
     */
    public void setConversation(@Nullable AiConversation conversation) {
        // Entities are equal by id, so a reloaded instance of
        // the same conversation is not a change.
        boolean conversationChanged = !Objects.equals(this.conversation, conversation);

        this.conversation = conversation;
        this.newConversation = false;
        if (renameAction != null) {
            renameAction.setConversation(conversation);
        }

        // The memory of the previous conversation must not
        // stay open over the new one. Re-binding the same
        // conversation (after each response) keeps it open.
        if (conversationChanged) {
            sidePanel.closeSidePanel();
        }

        resync();
    }

    /**
     * Binds a conversation to the chat by id.
     *
     * @param conversationId id of the conversation to show; {@code null} or an id that does not resolve clears the chat
     */
    public void setConversationId(@Nullable UUID conversationId) {
        setConversation(conversationId != null ? loadConversation(conversationId) : null);
    }

    /**
     * INTERNAL.
     * <p>
     * Returns whether the chat is a new conversation: no conversation is bound, yet the user can type, and the first
     * message creates the conversation ({@link AiConversationCreatedEvent}). The chat becomes one when it gets a data
     * block while no conversation is bound, and stays one when the user removes the block, until a conversation is
     * bound. Nothing is stored until the first message is sent.
     *
     * @return {@code true} if the first message creates the conversation
     */
    @Internal
    public boolean isNewConversation() {
        return newConversation;
    }

    /**
     * Hides the message input and the controls that change the conversation: renaming it and changing what the
     * assistant keeps in memory. The assistant memory stays viewable.
     *
     * @param readOnly {@code true} to hide them
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        refreshInputVisibility();
    }

    /**
     * Shows or hides the conversation title, for a host that names the conversation in its own chrome. The menu
     * stays, and {@link AiConversationTitleChangeEvent} fires either way. The fragment stores no flag of its
     * own, so the choice survives conversation reloads.
     *
     * @param titleVisible {@code false} to hide the title; visible by default
     */
    public void setTitleVisible(boolean titleVisible) {
        conversationTitle.setVisible(titleVisible);
    }

    /**
     * Adds a listener notified whenever the title of the shown conversation changes.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addTitleChangeListener(ComponentEventListener<AiConversationTitleChangeEvent> listener) {
        return getEventBus().addListener(AiConversationTitleChangeEvent.class, listener);
    }

    /**
     * INTERNAL.
     * <p>
     * Adds a listener notified when the chat creates a conversation itself and binds it, which happens on the first
     * message of a new conversation ({@link #isNewConversation()}).
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    @Internal
    public Registration addConversationCreatedListener(ComponentEventListener<AiConversationCreatedEvent> listener) {
        return getEventBus().addListener(AiConversationCreatedEvent.class, listener);
    }

    /**
     * Overrides the application-level {@code jmix.aitools.ui.conversation-title-mode} for this chat.
     *
     * @param titleMode the title mode for this chat, or {@code null} to use the application-level setting
     */
    public void setTitleMode(@Nullable AiConversationTitleMode titleMode) {
        this.titleModeOverride = titleMode;
    }

    /**
     * Enables or disables the message input for the host's own reasons. The chat keeps sending blocked while it shows
     * a response as running regardless, and the end of a response never enables an input the host disabled.
     *
     * @param enabled {@code false} to disable the input; {@code true} to enable it unless a response is running
     */
    public void setMessageInputEnabled(boolean enabled) {
        this.messageInputEnabled = enabled;
        updateMessageInputEnabled();
    }

    /**
     * Returns whether the chat shows an assistant response as running in the bound conversation. The response itself
     * runs on the server and is not lost when the host view closes.
     *
     * @return {@code true} while an assistant response is shown as running
     */
    public boolean isAwaitingResponse() {
        return responseShown;
    }

    /**
     * Sends a user message to the bound conversation and starts the assistant's response. Nothing is sent when no
     * conversation is bound, the text is blank, a response is already shown as running, or the AI is not available.
     * <p>
     * Persists the message, starts the assistant's response on the server, shows the message and the thinking
     * indicator and disables the message input. Used by hosts that start a conversation with an initial prompt (the
     * chat-hub flow). The message input is not touched, so a message that is not sent is not put into it.
     *
     * @param userMessage message text to send; a {@code null} or blank value is ignored
     */
    public void sendMessage(@Nullable String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return;
        }
        startMessage(userMessage);
    }

    /**
     * Moves keyboard focus to the message input. Does nothing while the input is disabled.
     */
    public void focusMessageInput() {
        getChatPanel().focusInput();
    }

    /**
     * Sets a supplier of the avatar icon shown next to assistant messages, letting the host override the default
     * add-on icon. Applies when the panel shows avatars ({@link HasAiAvatarIcon}). The supplier must return a fresh
     * component on every call.
     *
     * @param avatarIconSupplier supplier of the assistant avatar icon, or {@code null} to use the default
     */
    public void setAiAvatarIconSupplier(@Nullable SerializableSupplier<Component> avatarIconSupplier) {
        this.aiAvatarIconSupplier = avatarIconSupplier;
        if (chatPanel instanceof HasAiAvatarIcon avatarPanel) {
            avatarPanel.setAiAvatarIconSupplier(avatarIconSupplier);
        }
    }

    /**
     * Returns the custom assistant avatar icon supplier, if one was set.
     *
     * @return the avatar icon supplier, or {@code null} when the default icon is used
     */
    @Nullable
    public SerializableSupplier<Component> getAiAvatarIconSupplier() {
        return aiAvatarIconSupplier;
    }

    /**
     * INTERNAL.
     * <p>
     * Sets the data to send with the user's next question, e.g. rows the user wants to ask about. The chat shows
     * the block with suggested questions, and picking one puts it into the message input. The question and the
     * data are sent as one message, kept in the assistant memory while it has room. After it is sent or the user
     * removes it, the block is gone.
     * <p>
     * A block set while no conversation is bound makes the chat a new conversation ({@link #isNewConversation()}).
     * While the chat is read-only or the AI is not available, the block is not shown but stays set. A panel that
     * cannot show the block gets none: the block is ignored.
     *
     * @param dataBlock data to send with the next question, or {@code null} to drop the pending one
     */
    @Internal
    public void setDataBlock(@Nullable AiChatDataBlock dataBlock) {
        if (dataBlock != null && !(getChatPanel() instanceof HasAiChatDataBlock)) {
            if (!dataBlockUnsupportedWarned) {
                dataBlockUnsupportedWarned = true;
                log.warn("Data block ignored: chat panel {} does not implement {}",
                        getChatPanel().getClass().getName(), HasAiChatDataBlock.class.getSimpleName());
            }
            return;
        }

        this.dataBlock = dataBlock;
        this.dataBlockPrompts = dataBlock != null ? resolveDataBlockPrompts(dataBlock) : List.of();

        if (dataBlock != null && conversation == null) {
            newConversation = true;
        }

        refreshInputVisibility();
    }

    /**
     * INTERNAL.
     * <p>
     * Returns the data waiting to be sent with the user's next question.
     *
     * @return the pending data block, or {@code null} if there is none
     */
    @Internal
    @Nullable
    public AiChatDataBlock getDataBlock() {
        return dataBlock;
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        initChatActions();

        resync();
    }

    @Subscribe("sidePanel")
    protected void onSidePanelClose(final SidePanelCloseEvent event) {
        assistantMemory = null;
    }

    @EventListener
    public void onGenerationStarted(AiChatGenerationStartedEvent event) {
        if (!event.belongsTo(conversation)) {
            return;
        }

        boolean otherGenerationShown = shownGenerationId != null
                && !shownGenerationId.equals(event.getGenerationId());

        if (!responseShown || otherGenerationShown) {
            // The question was sent elsewhere, e.g. from another tab, or the previous response's outcome was missed,
            // so the chat lacks the latest messages.
            reloadConversation();
            refreshAll();
        }

        if (!event.getGenerationId().equals(shownGenerationId)) {
            shownGenerationId = event.getGenerationId();
            shownStatusSequence = 0;
        }

        showThinkingIndicator();
    }

    @EventListener
    public void onGenerationProgress(AiChatGenerationProgressEvent event) {
        if (event.belongsTo(conversation)
                && responseShown
                && event.getGenerationId().equals(shownGenerationId)
                && event.getSequence() > shownStatusSequence) {
            shownStatusSequence = event.getSequence();
            appendThinkingStatusUpdate(event.getUpdate());
        }
    }

    @EventListener
    public void onGenerationCompleted(AiChatGenerationCompletedEvent event) {
        if (event.belongsTo(conversation) && isShownOrNone(event)) {
            handleAssistantResponseDone(event.getAssistantMessageId());
        }
    }

    @EventListener
    public void onGenerationFailed(AiChatGenerationFailedEvent event) {
        if (event.belongsTo(conversation) && isShownOrNone(event)) {
            showAssistantProcessingError();
        }
    }

    @EventListener
    public void onGenerationCancelled(AiChatGenerationCancelledEvent event) {
        // With no response shown, nothing was locked for one.
        if (event.belongsTo(conversation) && isAwaitingResponse() && isShownOrNone(event)) {
            removeThinkingIndicator();
            focusMessageInput();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // A re-attached fragment (e.g. a Tabbed Mode tab switch)
        // missed the events sent while it was detached.
        if (!attachEvent.isInitialAttach() && conversation != null) {
            resync();
        }
    }

    /**
     * Returns the panel the conversation is drawn with, creating it on first use.
     *
     * @return the panel
     */
    protected AiChatPanel getChatPanel() {
        if (chatPanel == null) {
            chatPanel = createChatPanel();
        }
        return chatPanel;
    }

    /**
     * Creates the panel the conversation is drawn with and wires it to this fragment.
     *
     * @return the new panel
     */
    protected AiChatPanel createChatPanel() {
        AiChatPanel createdChatPanel = chatPanelFactory.createChatPanel(this);
        createdChatPanel.setSubmitHandler(this::submitMessage);
        createdChatPanel.setStopHandler(this::stopResponse);
        createdChatPanel.setRetryHandler(this::retryResponse);
        if (aiAvatarIconSupplier != null && createdChatPanel instanceof HasAiAvatarIcon avatarPanel) {
            avatarPanel.setAiAvatarIconSupplier(aiAvatarIconSupplier);
        }
        Component chatPanelComponent = createdChatPanel.asComponent();
        if (chatPanelComponent instanceof HasSize sized) {
            sized.setSizeFull();
        }
        chatPanelContainer.add(chatPanelComponent);
        return createdChatPanel;
    }

    protected void initChatActions() {
        renameAction = createRenameAction();
        chatActionsBtn.addItem(renameAction.getId(), renameAction);

        if (getChatPanel() instanceof HasKeepInContext) {
            assistantMemoryAction = createAssistantMemoryAction();
            chatActionsBtn.addItem(assistantMemoryAction.getId(), assistantMemoryAction);
        }
    }

    protected RenameConversationAction createRenameAction() {
        RenameConversationAction action = actions.create(RenameConversationAction.ID);
        action.setConversation(conversation);
        action.setAfterSaveHandler(saved -> {
            // Refresh just the header. Reloading the conversation
            // while a response is running may break the UI.
            conversation = saved;
            applyConversationTitleText(saved.getTitle());
        });
        return action;
    }

    protected ShowAssistantMemoryAction createAssistantMemoryAction() {
        return new ShowAssistantMemoryAction(ShowAssistantMemoryAction.ID,
                keptCount -> messages.formatMessage("", "assistantMemory.menuItem", keptCount),
                this::openAssistantMemory)
                .withIcon(icons.get(JmixFontIcon.LIGHTBULB));
    }

    protected AssistantMemoryFragment createAssistantMemory() {
        AssistantMemoryFragment memory = fragments.create(this, AssistantMemoryFragment.class);
        memory.setMessageClickHandler(this::showKeptMessage);
        memory.setRemoveHandler(message -> changeKeepInContext(message, false));
        memory.setCloseHandler(sidePanel::closeSidePanel);
        memory.setReadOnly(!isEditable());
        return memory;
    }

    protected void openAssistantMemory() {
        assistantMemory = createAssistantMemory();

        // The side panel is shared: it shows one
        // content at a time and is named after it.
        sidePanelContent.removeAll();
        sidePanelContent.add(assistantMemory);
        sidePanel.setOverlayAriaLabel(messages.getMessage("assistantMemory.title"));

        // Reloaded on opening: another tab may have
        // changed the memory meanwhile.
        refreshAssistantMemory();
        sidePanel.openSidePanel();
    }

    protected void showKeptMessage(AiChatMessage message) {
        getChatPanel().scrollToMessage(message);
    }

    protected void changeKeepInContext(AiChatMessage message, boolean keep) {
        if (conversation == null) {
            return;
        }

        KeepInContextResult result;
        try {
            result = messageService.setKeepInContext(message, keep);
        } catch (AccessDeniedException e) {
            log.debug("Assistant memory not changed: conversation {} is no longer accessible", conversation.getId());
            resync();
            return;
        } catch (IllegalArgumentException e) {
            log.debug("Assistant memory not changed: message {} no longer exists", message.getId());
            resync();
            return;
        }

        if (result == KeepInContextResult.LIMIT_REACHED) {
            Collection<AiChatMessage> keptMessages = loadKeptMessages();
            applyKeptMessages(keptMessages);

            notifications.create(messages.formatMessage("", "assistantMemory.limitReached", keptMessages.size()))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        if (getChatPanel() instanceof HasKeepInContext keepPanel) {
            keepPanel.refreshKeepInContext(message);
        }

        refreshAssistantMemory();
    }

    protected void refreshAssistantMemory() {
        Collection<AiChatMessage> keptMessages = loadKeptMessages();
        applyKeptMessages(keptMessages);
    }

    protected void applyKeptMessages(Collection<AiChatMessage> keptMessages) {
        if (assistantMemoryAction != null) {
            assistantMemoryAction.setKeptCount(keptMessages.size());
        }
        if (assistantMemory != null) {
            assistantMemory.setMessages(keptMessages);
        }

        refreshDataBlockWarning();
    }

    protected Collection<AiChatMessage> loadKeptMessages() {
        if (conversation == null
                || conversation.getId() == null
                || !(getChatPanel() instanceof HasKeepInContext)) {
            return List.of();
        }
        return messageService.loadKeptInContextMessages(conversation);
    }

    /**
     * Sends a message the user submitted in the panel.
     *
     * @param context the submitted message
     * @return whether the message was sent, so that the panel can give a rejected text back to the user
     */
    protected AiChatPanel.SubmitResult submitMessage(AiChatPanel.SubmitContext context) {
        return startMessage(context.getText())
                ? AiChatPanel.SubmitResult.accepted()
                : AiChatPanel.SubmitResult.rejected();
    }

    /**
     * Sends a non-blank user message and starts the assistant's response. A pending data block goes with the
     * message, and in a new conversation the conversation is created first.
     *
     * @param userMessage the non-blank message text
     * @return {@code true} if the message was sent
     */
    protected boolean startMessage(String userMessage) {
        if (isAwaitingResponse()) {
            log.debug("Ignoring a message while awaiting an assistant response");
            return false;
        }
        if (conversation == null && !newConversation) {
            log.warn("Cannot submit message — no conversation bound to the fragment");
            return false;
        }
        if (!chatService.isAvailable()) {
            log.warn("Cannot submit message — AI is not configured");
            return false;
        }

        AiConversation targetConversation = conversation != null ? conversation : createConversation();
        if (targetConversation == null) {
            return false;
        }

        String trimmedMessage = userMessage.trim();
        AiChatDataBlock sentDataBlock = dataBlock;
        boolean firstMessage = !messagesShown;
        AiChatMessage savedUserMessage;
        try {
            if (sentDataBlock != null) {
                // Kept in the assistant memory, so that the data is not forgotten as the conversation goes on.
                String text = composeDataBlockMessage(trimmedMessage, sentDataBlock);
                savedUserMessage = generationManager.start(targetConversation, text, true);
            } else {
                savedUserMessage = generationManager.start(targetConversation, trimmedMessage);
            }
        } catch (AiChatGenerationRejectedException e) {
            log.debug("Not sent: a response is already running in conversation {}", e.getConversationId());
            resync();
            notifications.create(getMessage("aiChatFragment.alreadyResponding"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return false;
        } catch (RuntimeException e) {
            handleStartFailure(e);
            return false;
        }

        if (sentDataBlock != null) {
            setDataBlock(null);
        }

        // Auto-titling happens on the first message only, by the question without the data.
        // It is optional and must never break sending.
        if (firstMessage) {
            try {
                applyAutoTitle(trimmedMessage);
            } catch (RuntimeException e) {
                log.warn("Automatic conversation titling failed; continuing with the message", e);
            }
        }

        getChatPanel().appendUserMessage(savedUserMessage);
        messagesShown = true;
        showThinkingIndicator();
        return true;
    }

    /**
     * Starts the assistant's answer again to the last question of the bound conversation, e.g. after the previous
     * response failed. Ignored while a response is shown as running, without a bound conversation and when the AI
     * is not available. When there is nothing to answer any more (the question got its answer meanwhile, e.g. in
     * another tab) or another response already runs, the chat is brought in line with the server instead. Called by
     * the panel's Retry control.
     */
    protected void retryResponse() {
        if (isAwaitingResponse() || conversation == null || !chatService.isAvailable()) {
            return;
        }

        AiChatMessage question;
        try {
            question = generationManager.retry(conversation);
        } catch (AiChatGenerationRejectedException e) {
            log.debug("Not retried: a response is already running in conversation {}", e.getConversationId());
            resync();
            return;
        } catch (RuntimeException e) {
            handleStartFailure(e);
            return;
        }

        if (question == null) {
            resync();
            return;
        }
        // Reloading drops what the failed response left, e.g. a transient error message.
        refreshAll();
        showThinkingIndicator();
    }

    protected void stopResponse() {
        if (conversation == null) {
            return;
        }
        try {
            // The response is shown as running until its cancelled event arrives.
            generationManager.cancel(conversation);
        } catch (AccessDeniedException e) {
            log.debug("Nothing to stop: conversation {} is no longer accessible", conversation.getId());
            resync();
        }
    }

    protected String getMessage(String key) {
        // By this class rather than getClass(), so that a subclass in another package resolves them too.
        return messages.getMessage(AiChatFragment.class, key);
    }

    protected String formatMessage(String key, Object... params) {
        return messages.formatMessage(AiChatFragment.class, key, params);
    }

    /**
     * Titles the conversation by its first message according to the effective {@link AiConversationTitleMode}.
     *
     * @param trimmedMessage the trimmed first user message
     */
    protected void applyAutoTitle(String trimmedMessage) {
        if (conversation == null) {
            return;
        }
        AiConversationTitleMode mode = resolveTitleMode();
        if (mode == AiConversationTitleMode.NONE) {
            return;
        }
        String initialTitle = applyFirstMessageTitle(trimmedMessage);
        if (initialTitle != null && mode == AiConversationTitleMode.GENERATED) {
            startTitleGeneration(trimmedMessage, initialTitle);
        }
    }

    @Nullable
    protected String applyFirstMessageTitle(String trimmedMessage) {
        if (conversation == null) {
            return null;
        }
        String title = titleSupport.buildInitialTitle(trimmedMessage);
        if (title.isBlank()) {
            return null;
        }
        conversation.setTitle(title);
        conversation = conversationService.save(conversation);
        applyConversationTitleText(title);
        return title;
    }

    protected void startTitleGeneration(String trimmedMessage, String expectedTitle) {
        UUID conversationId = Objects.requireNonNull(Objects.requireNonNull(conversation).getId());

        autoTitleService.generateAndApplyAsync(conversationId, trimmedMessage, expectedTitle,
                appliedTitle -> reflectGeneratedTitle(conversationId, expectedTitle, appliedTitle));
    }

    /**
     * Shows the generated title, which is already persisted, unless the fragment was detached or the
     * conversation was renamed meanwhile.
     *
     * @param conversationId id of the conversation that was titled
     * @param expectedTitle  first-message title the conversation must still carry
     * @param appliedTitle   the generated title, or {@code null} when nothing was applied
     */
    protected void reflectGeneratedTitle(UUID conversationId, String expectedTitle, @Nullable String appliedTitle) {
        if (appliedTitle == null
                || !isAttached()
                || conversation == null
                || !conversationId.equals(conversation.getId())
                || !Objects.equals(expectedTitle, conversation.getTitle())) {
            return;
        }
        conversation.setTitle(appliedTitle);
        applyConversationTitleText(appliedTitle);
    }

    protected AiConversationTitleMode resolveTitleMode() {
        return titleModeOverride != null ? titleModeOverride : properties.getConversationTitleMode();
    }

    /**
     * An outcome is handled unless the fragment shows another response: then it is a late event of an earlier one.
     *
     * @param event outcome event of a response in the bound conversation
     * @return {@code true} unless the fragment shows a different response
     */
    protected boolean isShownOrNone(AiChatGenerationEvent event) {
        return shownGenerationId == null || shownGenerationId.equals(event.getGenerationId());
    }

    protected void handleAssistantResponseDone(UUID assistantMessageId) {
        removeThinkingIndicator();

        // Re-binding by id reloads the conversation (an automatic title may have landed) and its messages,
        // after which the new assistant message can be marked.
        if (conversation != null) {
            setConversationId(conversation.getId());
        }

        getChatPanel().onResponseCompleted(assistantMessageId);

        // Another response may already be running (sent from another tab);
        // then the message input stays locked.
        if (!isAwaitingResponse()) {
            focusMessageInput();
        }
    }

    /**
     * Reports a response that could not be started, e.g. because the question could not be saved, after bringing
     * the chat in line with the server.
     *
     * @param e the failure
     */
    protected void handleStartFailure(RuntimeException e) {
        log.error("Failed to start an assistant response", e);
        resync();
        notifications.create(getMessage("aiChatFragment.errorProcessingMessage"))
                .withType(Notifications.Type.ERROR)
                .show();
    }

    protected void showAssistantProcessingError() {
        removeThinkingIndicator();
        getChatPanel().showResponseFailed(getMessage("aiChatFragment.errorProcessingMessage"));

        focusMessageInput();
    }

    protected void showThinkingIndicator() {
        if (responseShown) {
            return;
        }

        thinkingStatuses.clear();

        responseShown = true;
        getChatPanel().showResponseRunning();

        updateMessageInputEnabled();
    }

    protected void removeThinkingIndicator() {
        if (responseShown) {
            responseShown = false;
            getChatPanel().hideResponseRunning();
        }
        shownGenerationId = null;
        shownStatusSequence = 0;
        thinkingStatuses.clear();

        updateMessageInputEnabled();
    }

    protected void appendThinkingStatusUpdate(@Nullable AiToolStatusUpdate statusUpdate) {
        if (!responseShown || !AiToolStatusUtils.fold(thinkingStatuses, statusUpdate)) {
            return;
        }

        getChatPanel().showStatuses(List.copyOf(thinkingStatuses));
    }

    /**
     * Brings the fragment in line with the server: reloads the messages and shows the response running in the
     * bound conversation, with the tool statuses it has reported so far.
     */
    protected void resync() {
        // Snapshot before loading: when no response runs any more, its answer is already saved and gets loaded.
        Optional<AiChatGeneration> generation = conversation != null
                ? generationManager.getGeneration(conversation)
                : Optional.empty();

        refreshAll();

        if (generation.isPresent()) {
            showThinkingIndicator();
            // The snapshot statuses are already folded.
            thinkingStatuses.addAll(generation.get().getStatuses());
            getChatPanel().showStatuses(List.copyOf(thinkingStatuses));

            shownGenerationId = generation.get().getGenerationId();
            shownStatusSequence = generation.get().getStatusSequence();
        }
    }

    /**
     * Re-reads the bound conversation, e.g. to pick up a title set in another tab. Keeps the current instance when
     * the conversation no longer exists.
     */
    protected void reloadConversation() {
        if (conversation == null || conversation.getId() == null) {
            return;
        }
        AiConversation reloaded = loadConversation(conversation.getId());
        if (reloaded != null) {
            conversation = reloaded;
        }
    }

    /**
     * Reloads the title, the messages and the message input state. The running response, if shown, is dropped first:
     * the reloaded messages replace everything shown, the thinking indicator included.
     */
    protected void refreshAll() {
        removeThinkingIndicator();

        applyConversationTitleText(conversation != null ? conversation.getTitle() : null);

        Collection<AiChatMessage> loadedMessages = loadMessages();
        messagesShown = !loadedMessages.isEmpty();
        getChatPanel().showMessages(loadedMessages);
        refreshAssistantMemory();

        refreshInputVisibility();
        warnIfAiUnavailable();
    }

    protected void applyConversationTitleText(@Nullable String title) {
        String text = title != null ? title : "";
        conversationTitle.setText(text);
        if (conversationTitleTooltip == null) {
            conversationTitleTooltip = Tooltip.forComponent(conversationTitle);
        }
        // An empty tooltip text is not displayed, so a cleared header shows none.
        conversationTitleTooltip.setText(text);

        fireEvent(new AiConversationTitleChangeEvent(this, title));
    }

    protected void warnIfAiUnavailable() {
        if (conversation == null || chatUnavailableWarned || chatService.isAvailable()) {
            return;
        }
        chatUnavailableWarned = true;
        notifications.create(getMessage("aiChatFragment.chatUnavailable"))
                .withType(Notifications.Type.WARNING)
                .show();
    }

    /**
     * Loads all messages of the bound conversation, oldest first. Empty when no
     * conversation is bound or it has not been persisted yet.
     *
     * @return the bound conversation's messages, oldest first; empty if none
     */
    protected Collection<AiChatMessage> loadMessages() {
        if (conversation == null || conversation.getId() == null) {
            return List.of();
        }
        return messageService.loadMessages(conversation);
    }

    protected void updateMessageInputEnabled() {
        getChatPanel().setInputEnabled(messageInputEnabled
                && !(getChatPanel().isInputLockedWhileResponding() && isAwaitingResponse()));
    }

    protected void refreshInputVisibility() {
        boolean show = isEditable();
        getChatPanel().setInputVisible(isInputAvailable());
        if (getChatPanel() instanceof HasKeepInContext keepPanel) {
            keepPanel.setKeepInContextHandler(show ? this::changeKeepInContext : null);
        }
        if (assistantMemory != null) {
            assistantMemory.setReadOnly(!show);
        }
        refreshChatActions(show);
        refreshDataBlock();
        refreshSuggestedPrompts();
    }

    protected boolean isEditable() {
        return conversation != null && isInputAvailable();
    }

    protected boolean isInputAvailable() {
        return (conversation != null || newConversation) && !readOnly && chatService.isAvailable();
    }

    protected void refreshSuggestedPrompts() {
        if (!(getChatPanel() instanceof HasAiChatSuggestedPrompts promptsPanel)) {
            return;
        }

        List<String> prompts = isInputAvailable() ? getSuggestedPrompts() : List.of();
        promptsPanel.setSuggestedPromptHandler(prompts.isEmpty() ? null : this::onSuggestedPromptPicked);
        promptsPanel.setSuggestedPrompts(prompts);
    }

    /**
     * Handles the question the user picks below the message input: puts it into the input for the user to edit and
     * send. Nothing is sent.
     *
     * @param prompt the picked question
     */
    protected void onSuggestedPromptPicked(String prompt) {
        AiChatPanel chatPanel = getChatPanel();
        chatPanel.setInputText(prompt);
        chatPanel.focusInput();
    }

    /**
     * Returns the questions to offer below the message input.
     *
     * @return questions to offer, or an empty list to offer none
     */
    protected List<String> getSuggestedPrompts() {
        return dataBlock != null ? dataBlockPrompts : List.of();
    }

    protected void refreshChatActions(boolean editable) {
        if (renameAction != null) {
            renameAction.setVisible(editable);
        }
        if (assistantMemoryAction != null) {
            assistantMemoryAction.setVisible(conversation != null);
        }
        chatActionsBtn.setVisible(chatActionsBtn.getItems().stream().anyMatch(DropdownButtonItem::isVisible));
    }

    @Nullable
    protected AiConversation loadConversation(UUID conversationId) {
        return conversationService.loadConversation(conversationId);
    }

    private String composeDataBlockMessage(String trimmedQuestion, AiChatDataBlock block) {
        return trimmedQuestion + "\n\n" + block.getContent();
    }

    /**
     * Creates a conversation for the first message and binds it, telling the host through
     * {@link AiConversationCreatedEvent}. A failure is reported to the user.
     *
     * @return the created conversation, or {@code null} if it could not be created
     */
    @Nullable
    private AiConversation createConversation() {
        AiConversation created;
        try {
            created = conversationService.create();
        } catch (RuntimeException e) {
            log.error("Failed to create a conversation for the message", e);
            notifications.create(getMessage("aiChatFragment.errorProcessingMessage"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return null;
        }

        setConversation(created);

        fireEvent(new AiConversationCreatedEvent(this, created));

        return created;
    }

    private List<String> resolveDataBlockPrompts(AiChatDataBlock dataBlock) {
        // The prompts the host chose for the block win over the suggested ones.
        if (!dataBlock.getSuggestedPrompts().isEmpty()) {
            return dataBlock.getSuggestedPrompts();
        }

        return suggestedPromptManager.getPrompts(
                new AiSuggestedPromptContext(conversation, dataBlock.getMetaClass(), dataBlock.getView()));
    }

    private void refreshDataBlock() {
        if (!(getChatPanel() instanceof HasAiChatDataBlock blockPanel)) {
            return;
        }

        AiChatDataBlock shownBlock = isInputAvailable() ? dataBlock : null;
        blockPanel.setDataBlockRemoveHandler(shownBlock != null ? this::removeDataBlock : null);
        if (shownBlock != null) {
            blockPanel.showDataBlock(shownBlock.getCaption(), getDataBlockWarning());
        } else {
            blockPanel.hideDataBlock();
        }
    }

    private void refreshDataBlockWarning() {
        if (dataBlock != null
                && isInputAvailable()
                && getChatPanel() instanceof HasAiChatDataBlock blockPanel) {
            blockPanel.showDataBlock(dataBlock.getCaption(), getDataBlockWarning());
        }
    }

    @Nullable
    private String getDataBlockWarning() {
        return conversation != null && messageService.isKeepInContextLimitReached(conversation)
                ? messages.getMessage("askAiAboutData.memoryFull")
                : null;
    }

    private void removeDataBlock() {
        setDataBlock(null);
    }
}
