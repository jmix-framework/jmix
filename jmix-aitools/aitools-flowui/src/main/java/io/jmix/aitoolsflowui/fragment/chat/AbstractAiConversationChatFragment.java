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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.shared.Registration;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
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
import io.jmix.core.Messages;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base of an {@link AiConversationChat} fragment. Owns everything that does not depend on how the chat is drawn:
 * binding a conversation, sending a message through {@link AiChatGenerationManager}, following the response that runs
 * on the server (its events and, after a refresh or reopen, its snapshot), the composer rule, the automatic title and
 * the rename dialog. A subclass only renders, through the protected hooks, and supplies the title components.
 */
@Experimental
public abstract class AbstractAiConversationChatFragment extends Fragment<VerticalLayout>
        implements AiConversationChat {

    private static final Logger log = LoggerFactory.getLogger(AbstractAiConversationChatFragment.class);

    /**
     * Message group of the chat's own messages, so that a subclass in another package resolves them too.
     */
    protected static final String CHAT_MESSAGES_GROUP = AbstractAiConversationChatFragment.class.getPackageName();

    @Autowired
    protected Messages messages;
    @Autowired
    protected Dialogs dialogs;
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

    @Nullable
    protected Tooltip conversationTitleTooltip;

    @Nullable
    protected AiConversation conversation;

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
     * snapshot. A subclass gets a copy through {@link #renderStatuses(List)}.
     */
    protected final List<AiToolStatusUpdate> thinkingStatuses = new ArrayList<>();

    /**
     * Whether the chat shows any message of the conversation; the first message sent into an empty one titles it.
     */
    protected boolean messagesShown;

    protected boolean readOnly;

    /**
     * Whether the host lets the user type. A response shown as running disables the composer regardless.
     */
    protected boolean messageInputEnabled = true;

    protected boolean chatUnavailableWarned;

    @Nullable
    protected AiConversationTitleMode titleModeOverride;

    @Override
    public void setConversation(@Nullable AiConversation conversation) {
        this.conversation = conversation;

        resync();
    }

    @Override
    public void setConversationId(@Nullable UUID conversationId) {
        setConversation(conversationId != null ? loadConversation(conversationId) : null);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        refreshComposerVisibility();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The fragment stores no flag of its own, so the choice survives conversation reloads.
     */
    @Override
    public void setTitleVisible(boolean titleVisible) {
        getConversationTitle().setVisible(titleVisible);
    }

    @Override
    public Registration addTitleChangeListener(ComponentEventListener<AiConversationTitleChangeEvent> listener) {
        return getEventBus().addListener(AiConversationTitleChangeEvent.class, listener);
    }

    @Override
    public void setTitleMode(@Nullable AiConversationTitleMode titleMode) {
        this.titleModeOverride = titleMode;
    }

    @Override
    public Component asComponent() {
        return this;
    }

    /**
     * Moves keyboard focus to the message composer. No-op if the composer is currently disabled (e.g. while the
     * assistant is still generating a response).
     */
    public abstract void focusMessageInput();

    /**
     * Enables or disables the message composer for the host's own reasons. Independently of this, the fragment
     * keeps the composer disabled while it shows an assistant response as running, so the end of a response never
     * enables a composer the host disabled.
     *
     * @param enabled {@code false} to disable the composer; {@code true} to enable it unless a response is running
     */
    public void setMessageInputEnabled(boolean enabled) {
        this.messageInputEnabled = enabled;
        updateMessageInputEnabled();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The response itself runs on the server and is not lost when the host view closes.
     */
    @Override
    public boolean isAwaitingResponse() {
        return responseShown;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Persists the message, starts the assistant's response on the server, shows the message and the thinking
     * indicator and disables the composer. Used by the composer's submit handler and by hosts that start a
     * conversation with an initial prompt (the chat-hub flow).
     */
    @Override
    public void sendMessage(@Nullable String userMessage) {
        if (isAwaitingResponse()) {
            log.debug("Ignoring sendMessage call while awaiting an assistant response");
            return;
        }
        if (conversation == null) {
            log.warn("Cannot submit message — no conversation bound to the fragment");
            return;
        }
        if (userMessage == null || userMessage.isBlank()) {
            return;
        }
        if (!chatService.isAvailable()) {
            log.warn("Cannot submit message — AI is not configured");
            return;
        }

        String trimmedMessage = userMessage.trim();
        boolean firstMessage = !messagesShown;
        AiChatMessage savedUserMessage;
        try {
            savedUserMessage = generationManager.start(conversation, trimmedMessage);
        } catch (AiChatGenerationRejectedException e) {
            log.debug("Not sent: a response is already running in conversation {}", e.getConversationId());
            resync();
            notifications.create(getMessage("aiChatFragment.alreadyResponding"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        } catch (RuntimeException e) {
            log.error("Failed to start an assistant response", e);
            resync();
            notifications.create(getMessage("aiChatFragment.errorProcessingMessage"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        // Auto-titling happens on the first message only.
        // It is optional and must never break sending.
        if (firstMessage) {
            try {
                applyAutoTitle(trimmedMessage);
            } catch (RuntimeException e) {
                log.warn("Automatic conversation titling failed; continuing with the message", e);
            }
        }

        clearComposer();
        appendUserMessage(savedUserMessage);
        messagesShown = true;
        showThinkingIndicator();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        initComposer(this::sendMessage);
        getRenameConversationButton().addClickListener(clickEvent -> openTitleEditDialog());

        resync();
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
     * Returns the component that shows the conversation title.
     *
     * @return the title component
     */
    protected abstract H3 getConversationTitle();

    /**
     * Returns the button that opens the conversation rename dialog.
     *
     * @return the rename button
     */
    protected abstract JmixButton getRenameConversationButton();

    /**
     * Wires the composer's submit to the given handler.
     *
     * @param submitHandler handler receiving the submitted text
     */
    protected abstract void initComposer(Consumer<String> submitHandler);

    /**
     * Shows the given messages in place of everything shown so far. Called only after the running response, if any,
     * stopped being shown ({@link #hideResponseRunning()}).
     *
     * @param messages messages of the bound conversation, oldest first
     */
    protected abstract void renderMessages(Collection<AiChatMessage> messages);

    /**
     * Adds a user message just sent from this chat after the shown messages.
     *
     * @param message the saved user message
     */
    protected abstract void appendUserMessage(AiChatMessage message);

    /**
     * Shows the response as running ("thinking") after the shown messages, without statuses yet.
     */
    protected abstract void showResponseRunning();

    /**
     * Shows the tool statuses of the running response.
     *
     * @param statuses the folded statuses, oldest first
     */
    protected abstract void renderStatuses(List<AiToolStatusUpdate> statuses);

    /**
     * Stops showing the response as running.
     */
    protected abstract void hideResponseRunning();

    /**
     * Tells the user that the response failed. Called after the running response is no longer shown.
     */
    protected abstract void showResponseFailed();

    /**
     * Called after a completed response's answer has been loaded with the other messages.
     *
     * @param assistantMessageId id of the persisted answer
     */
    protected abstract void onResponseCompleted(UUID assistantMessageId);

    protected abstract void clearComposer();

    protected abstract void applyComposerEnabled(boolean enabled);

    protected abstract void applyComposerVisible(boolean visible);

    protected String getMessage(String key) {
        return messages.getMessage(CHAT_MESSAGES_GROUP, key);
    }

    protected String formatMessage(String key, Object... params) {
        return messages.formatMessage(CHAT_MESSAGES_GROUP, key, params);
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

        onResponseCompleted(assistantMessageId);

        // Another response may already be running (sent from another tab);
        // then the composer stays locked.
        if (!isAwaitingResponse()) {
            focusMessageInput();
        }
    }

    protected void showAssistantProcessingError() {
        removeThinkingIndicator();
        showResponseFailed();

        focusMessageInput();
    }

    protected void showThinkingIndicator() {
        if (responseShown) {
            return;
        }

        thinkingStatuses.clear();

        responseShown = true;
        showResponseRunning();

        updateMessageInputEnabled();
    }

    protected void removeThinkingIndicator() {
        if (responseShown) {
            responseShown = false;
            hideResponseRunning();
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

        renderStatuses(List.copyOf(thinkingStatuses));
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
            renderStatuses(List.copyOf(thinkingStatuses));

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
     * Reloads the title, the messages and the composer state. The running response, if shown, is dropped first:
     * the reloaded messages replace everything shown, the thinking indicator included.
     */
    protected void refreshAll() {
        removeThinkingIndicator();

        applyConversationTitleText(conversation != null ? conversation.getTitle() : null);

        Collection<AiChatMessage> loadedMessages = loadMessages();
        messagesShown = !loadedMessages.isEmpty();
        renderMessages(loadedMessages);

        refreshComposerVisibility();
        warnIfAiUnavailable();
    }

    protected void applyConversationTitleText(@Nullable String title) {
        String text = title != null ? title : "";
        H3 titleComponent = getConversationTitle();
        titleComponent.setText(text);
        if (conversationTitleTooltip == null) {
            conversationTitleTooltip = Tooltip.forComponent(titleComponent);
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
        applyComposerEnabled(messageInputEnabled && !isAwaitingResponse());
    }

    protected void refreshComposerVisibility() {
        boolean show = conversation != null && !readOnly && chatService.isAvailable();
        applyComposerVisible(show);
        getRenameConversationButton().setVisible(show);
    }

    @Nullable
    protected AiConversation loadConversation(UUID conversationId) {
        return conversationService.loadConversation(conversationId);
    }

    protected void openTitleEditDialog() {
        if (conversation == null) {
            return;
        }
        String currentTitle = conversation.getTitle() == null ? "" : conversation.getTitle();

        View<?> view = UiComponentUtils.findView(this);
        if (view == null) {
            throw new IllegalStateException("Fragment is not attached to a view");
        }

        dialogs.createInputDialog(view)
                .withHeader(getMessage("aiChatFragment.editConversationTitleDialog.header"))
                .withLabelsPosition(Dialogs.InputDialogBuilder.LabelsPosition.TOP)
                .withParameters(
                        InputParameter.stringParameter("title")
                                .withLabel(getMessage("aiChatFragment.editConversationTitleDialog.titleField"))
                                .withRequired(true)
                                .withDefaultValue(currentTitle)
                )
                .withValidator(this::validateTitle)
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (!closeEvent.closedWith(DialogOutcome.OK)) {
                        return;
                    }
                    String updatedTitle = closeEvent.getValue("title");
                    if (updatedTitle == null || updatedTitle.isBlank()) {
                        return;
                    }
                    if (conversation == null) {
                        return;
                    }
                    conversation.setTitle(updatedTitle.trim());
                    // Persist only the title and refresh just the header.
                    // Reloading conversation while awaiting LLM answer
                    // may break UI.
                    conversation = conversationService.save(conversation);
                    applyConversationTitleText(Objects.requireNonNull(conversation).getTitle());
                })
                .open();
    }

    protected ValidationErrors validateTitle(InputDialog.ValidationContext context) {
        String title = context.getValue("title");
        if (title != null && title.trim().length() > AiConversation.TITLE_MAX_LENGTH) {
            return ValidationErrors.of(formatMessage(
                    "aiChatFragment.editConversationTitleDialog.titleTooLong", AiConversation.TITLE_MAX_LENGTH));
        }
        return ValidationErrors.none();
    }
}
