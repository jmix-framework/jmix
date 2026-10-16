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

package io.jmix.aitoolsflowui.view.chat;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.generation.AiChatGeneration;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.generation.AiChatGenerationRejectedException;
import io.jmix.aitoolsflowui.generation.AiToolStatusFolding;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCancelledEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationCompletedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationFailedEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationProgressEvent;
import io.jmix.aitoolsflowui.generation.event.AiChatGenerationStartedEvent;
import io.jmix.aitoolsflowui.model.*;
import io.jmix.aitoolsflowui.service.*;
import io.jmix.aitoolsflowui.view.chat.support.*;
import io.jmix.aitoolsflowui.view.chat.timeline.*;
import io.jmix.aitoolsflowui.view.input.AiChatInputFragment;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Encapsulates the "AI chat panel" UI — title row, message timeline, thinking
 * indicator and the {@link AiChatInputFragment} composer — for an {@link AiConversation}.
 * Designed to be embedded into any host view (the standard detail view, a
 * side dialog, a chat hub view, custom layouts).
 * <p>
 * <b>Ownership.</b> The host supplies the conversation to display via
 * {@link #setConversation(AiConversation)} or {@link #setConversationId(UUID)};
 * the fragment loads that conversation's {@link AiChatMessage}s itself — it does
 * not rely on the {@code messages} collection being pre-fetched by the host —
 * and reads/writes directly through {@link DataManager}.
 */
@Experimental
@FragmentDescriptor("ai-chat-fragment.xml")
@JsModule("./aitools/ai-chat-timeline-scroll.js")
public class AiChatFragment extends Fragment<VerticalLayout> {

    private static final Logger log = LoggerFactory.getLogger(AiChatFragment.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected TimeSource timeSource;
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
    protected TimelineItemFactory timelineItemFactory;
    @Autowired
    protected AiChatService chatService;
    @Autowired
    protected AiToolsFlowuiProperties properties;
    @Autowired
    protected ConversationTitleSupport titleSupport;
    @Autowired
    protected AiConversationAutoTitleService autoTitleService;

    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected H3 conversationTitle;
    @ViewComponent
    protected JmixButton editConversationTitleBtn;
    @ViewComponent
    protected VerticalLayout composerContainer;
    @ViewComponent
    protected JmixVirtualList<TimelineItem> timelineList;

    @ViewComponent
    protected CollectionContainer<TimelineItem> timelineItemsDc;

    @ViewComponent
    protected AiChatInputFragment composerFragment;

    @Nullable
    protected Tooltip conversationTitleTooltip;

    @Nullable
    protected AiConversation conversation;
    @Nullable
    protected TimelineItem activeThinkingItem;

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

    protected final List<AiToolStatusUpdate> thinkingStatuses = new ArrayList<>();

    protected boolean readOnly;

    protected boolean chatUnavailableWarned;

    @Nullable
    protected AiConversationTitleMode titleModeOverride;

    @Nullable
    protected SerializableSupplier<Component> aiAvatarIconSupplier;

    /**
     * Binds the fragment to a conversation.
     *
     * @param conversation conversation to bind
     */
    public void setConversation(@Nullable AiConversation conversation) {
        this.conversation = conversation;
        this.activeThinkingItem = null;
        this.shownGenerationId = null;
        this.shownStatusSequence = 0;
        this.thinkingStatuses.clear();
        // A response shown as running in the previous conversation had disabled the composer.
        setMessageInputEnabled(true);

        resync();
    }

    /**
     * Binds the fragment to a conversation by id. A {@code null} id (or an id that no longer resolves)
     * clears the fragment.
     *
     * @param conversationId conversation id to set
     */
    public void setConversationId(@Nullable UUID conversationId) {
        setConversation(conversationId != null ? loadConversation(conversationId) : null);
    }

    /**
     * Sets a supplier of the avatar icon shown next to assistant messages, letting the host override
     * the default add-on icon. The supplier must return a fresh component on every call.
     *
     * @param avatarIconSupplier supplier of the assistant avatar icon, or {@code null} to use the default
     */
    public void setAiAvatarIconSupplier(@Nullable SerializableSupplier<Component> avatarIconSupplier) {
        this.aiAvatarIconSupplier = avatarIconSupplier;
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
     * Hides the composer and the title-edit button.
     *
     * @param readOnly {@code true} to hide the composer and the title-edit button
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        refreshComposerVisibility();
    }

    /**
     * Hides the conversation title, for a host that already names the conversation in its own chrome — a
     * view title, a tab, a dialog header — and would otherwise show the same name twice. The title row
     * itself stays, carrying the title-edit button.
     * <p>
     * {@link TitleChangeEvent} fires either way, so a hidden title still keeps the host's own title in sync
     * with renames. The fragment stores no flag of its own, so the choice survives conversation reloads.
     *
     * @param titleVisible {@code false} to hide the conversation title; visible by default
     */
    public void setTitleVisible(boolean titleVisible) {
        conversationTitle.setVisible(titleVisible);
    }

    /**
     * Adds a listener notified whenever the displayed conversation title changes — when a conversation is
     * bound and after it is renamed. Hosts use it to keep their own title in sync (page title, tab title).
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addTitleChangeListener(
            ComponentEventListener<TitleChangeEvent> listener) {
        return getEventBus().addListener(TitleChangeEvent.class, listener);
    }

    /**
     * Sets the generation title mode for this chat. Overrides the application-level
     * {@code jmix.aitools.ui.conversation-title-mode}.
     *
     * @param titleMode the title mode for this chat, or {@code null} to use the application-level setting
     */
    public void setTitleMode(@Nullable AiConversationTitleMode titleMode) {
        this.titleModeOverride = titleMode;
    }

    /**
     * Moves keyboard focus to the message composer. No-op if the composer is
     * currently disabled (e.g. while the assistant is still generating a
     * response).
     */
    public void focusMessageInput() {
        composerFragment.focus();
    }

    /**
     * Enables or disables the message composer. The fragment disables it
     * while it shows an assistant response as running and re-enables it when
     * the response ends; hosts can call this for additional read-only states.
     *
     * @param enabled {@code true} to enable the composer, {@code false} to disable it
     */
    public void setMessageInputEnabled(boolean enabled) {
        composerFragment.setInputEnabled(enabled);
    }

    /**
     * Whether the fragment shows an assistant response as running in the bound
     * conversation. The response itself runs on the server and is not lost when
     * the host view closes.
     *
     * @return {@code true} while an assistant response is shown as running
     */
    public boolean isAwaitingResponse() {
        return activeThinkingItem != null;
    }

    /**
     * Programmatic entry point to send a user message: persists it, starts the
     * assistant's response on the server, appends the message to the timeline,
     * shows the thinking indicator and disables the composer. Used by the
     * composer's submit handler and by hosts
     * that start a conversation with an initial prompt (the chat-hub flow).
     *
     * @param userMessage message text to send; a {@code null} or blank value is ignored
     */
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
        boolean firstMessage = timelineItemsDc.getItems().isEmpty();
        AiChatMessage savedUserMessage;
        try {
            savedUserMessage = generationManager.start(conversation, trimmedMessage);
        } catch (AiChatGenerationRejectedException e) {
            log.debug("Not sent: a response is already running in conversation {}", e.getConversationId());
            resync();
            notifications.create(messageBundle.getMessage("aiChatFragment.alreadyResponding"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        } catch (RuntimeException e) {
            log.error("Failed to start an assistant response", e);
            resync();
            notifications.create(messageBundle.getMessage("aiChatFragment.errorProcessingMessage"))
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

        composerFragment.clear();
        appendTimelineItem(timelineItemFactory.createUserItem(savedUserMessage));
        showThinkingIndicator();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        composerFragment.setSubmitHandler(this::sendMessage);

        resync();
    }

    @Subscribe(id = "editConversationTitleBtn", subject = "clickListener")
    public void onEditConversationTitleBtnClick(final ClickEvent<JmixButton> event) {
        openTitleEditDialog();
    }

    @EventListener
    public void onGenerationStarted(AiChatGenerationStartedEvent event) {
        if (!event.belongsTo(conversation)) {
            return;
        }

        boolean otherGenerationShown = shownGenerationId != null
                && !shownGenerationId.equals(event.getGenerationId());

        if (activeThinkingItem == null || otherGenerationShown) {
            // The question was sent elsewhere, e.g. from another tab, or the previous response's outcome was missed,
            // so the timeline lacks the latest messages.
            removeThinkingIndicator();
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
                && activeThinkingItem != null
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
            forceMessageInputFocus();
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
        activeThinkingItem = null;
        shownGenerationId = null;
        shownStatusSequence = 0;
        thinkingStatuses.clear();

        // Re-binding by id reloads the conversation (an automatic title may have landed) and its messages,
        // after which the new assistant row can be stamped.
        if (conversation != null) {
            setConversationId(conversation.getId());
        }

        markFreshAssistantItem(assistantMessageId);

        // Another response may already be running (sent from another tab);
        // then the composer stays locked.
        if (!isAwaitingResponse()) {
            forceMessageInputFocus();
        }
    }

    /**
     * Locates the assistant item that just landed in the container after the
     * post-LLM reload and stamps it with the "fresh" flag, triggering a
     * single-row re-render via {@link CollectionContainer#replaceItem}.
     *
     * @param assistantMessageId id of the assistant message to mark as fresh
     */
    protected void markFreshAssistantItem(UUID assistantMessageId) {
        timelineItemsDc.getItems().stream()
                .filter(item -> item.getType() == TimelineItemType.ASSISTANT
                        && item.getMessage() != null
                        && assistantMessageId.equals(item.getMessage().getId()))
                .findFirst()
                .ifPresent(item -> {
                    item.setFresh(true);
                    timelineItemsDc.replaceItem(item);
                });
    }

    protected void showAssistantProcessingError() {
        removeThinkingIndicator();
        appendTimelineItem(timelineItemFactory.createAssistantItem(createTransientAssistantMessage(
                messageBundle.getMessage("aiChatFragment.errorProcessingMessage"))));

        forceMessageInputFocus();
    }

    protected void showThinkingIndicator() {
        if (activeThinkingItem != null) {
            return;
        }

        thinkingStatuses.clear();

        AiChatMessage placeholder = createTransientAssistantMessage("");
        activeThinkingItem = timelineItemFactory.createThinkingItem(placeholder);
        appendTimelineItem(activeThinkingItem);

        setMessageInputEnabled(false);
    }

    protected void removeThinkingIndicator() {
        if (activeThinkingItem == null) {
            return;
        }
        timelineItemsDc.getMutableItems().remove(activeThinkingItem);
        activeThinkingItem = null;
        shownGenerationId = null;
        shownStatusSequence = 0;
        thinkingStatuses.clear();
    }

    protected void appendThinkingStatusUpdate(@Nullable AiToolStatusUpdate statusUpdate) {
        if (activeThinkingItem == null || !AiToolStatusFolding.fold(thinkingStatuses, statusUpdate)) {
            return;
        }

        renderThinkingStatuses();
        scrollToBottom(false);
    }

    protected void renderThinkingStatuses() {
        if (activeThinkingItem == null) {
            return;
        }

        List<TimelineItemStatus> statuses = thinkingStatuses.stream()
                .map(this::createTimelineStatus)
                .collect(Collectors.toCollection(ArrayList::new));

        activeThinkingItem.setStatusUpdates(statuses);
        refreshTimelineItem(activeThinkingItem);
    }

    /**
     * Brings the fragment in line with the server: reloads the messages and shows the response running in the
     * bound conversation, with the tool statuses it has reported so far.
     */
    protected void resync() {
        boolean wasAwaitingResponse = isAwaitingResponse();
        // Snapshot before loading: when no response runs any more, its answer is already saved and gets loaded.
        Optional<AiChatGeneration> generation = conversation != null
                ? generationManager.getGeneration(conversation)
                : Optional.empty();

        removeThinkingIndicator();
        refreshAll();

        if (generation.isPresent()) {
            showThinkingIndicator();
            // The snapshot statuses are already folded.
            thinkingStatuses.addAll(generation.get().getStatuses());
            renderThinkingStatuses();

            shownGenerationId = generation.get().getGenerationId();
            shownStatusSequence = generation.get().getStatusSequence();
        } else if (wasAwaitingResponse) {
            // The shown response ended unseen, e.g. while the fragment was detached.
            setMessageInputEnabled(true);
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

    protected void refreshAll() {
        applyConversationTitleText(conversation != null ? conversation.getTitle() : null);

        timelineItemsDc.setItems(timelineItemFactory.buildTimelineItems(loadMessages()));
        scrollToBottom();

        refreshComposerVisibility();
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

        fireEvent(new TitleChangeEvent(this, title));
    }

    protected void warnIfAiUnavailable() {
        if (conversation == null || chatUnavailableWarned || chatService.isAvailable()) {
            return;
        }
        chatUnavailableWarned = true;
        notifications.create(messageBundle.getMessage("aiChatFragment.chatUnavailable"))
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

    protected void forceMessageInputFocus() {
        setMessageInputEnabled(true);
        focusMessageInput();
    }

    protected void refreshComposerVisibility() {
        boolean show = conversation != null && !readOnly && chatService.isAvailable();
        composerContainer.setVisible(show);
        editConversationTitleBtn.setVisible(show);
    }

    protected void appendTimelineItem(TimelineItem item) {
        timelineItemsDc.getMutableItems().add(item);
        scrollToBottom();
    }

    protected void refreshTimelineItem(TimelineItem item) {
        // replaceItem fires SET_ITEM on the container, which makes the
        // virtual list re-render this single row — needed when in-place
        // mutations on the item (e.g. statusUpdates of the thinking row)
        // wouldn't otherwise be observable to the data provider.
        timelineItemsDc.replaceItem(item);
    }

    /**
     * Scrolls the timeline to the latest row, forcing the move regardless of
     * where the user is currently looking. Use for discrete, user-initiated
     * events (opening a conversation, sending a message, the assistant's final
     * answer) where the bottom must be shown.
     */
    protected void scrollToBottom() {
        scrollToBottom(true);
    }

    /**
     * Scrolls the timeline to the latest row.
     *
     * @param force when {@code true}, always scrolls to the bottom and resets
     *              the "stick to bottom" intent. When {@code false}, scrolls
     *              only if the user is already at (or near) the bottom — used
     *              for streaming status updates so a user who scrolled up to
     *              read is not yanked back down on every minor update.
     */
    protected void scrollToBottom(boolean force) {
        int size = timelineItemsDc.getItems().size();
        if (size == 0) {
            return;
        }
        if (force) {
            // Coarse server-side positioning: makes the virtualizer render the
            // rows near the end so their real heights get measured. Skipped
            // for the conditional case — it would yank a user who scrolled up.
            timelineList.scrollToIndex(size - 1);
        }
        // The client-side sticking (pin to the bottom while the answer's markdown
        // lays out asynchronously; detach synchronously on a user scroll gesture)
        // lives in the ai-chat-timeline-scroll.js module (imported via @JsModule),
        // which registers window.jmixAiTools.stickToBottom. Called here with the
        // list element and the force flag. See that file and
        // docs/features/aitools/specs/chat-timeline-auto-scroll.md.
        timelineList.getElement().executeJs("window.jmixAiTools.stickToBottom(this, $0)", force);
    }

    protected AiChatMessage createTransientAssistantMessage(String content) {
        AiChatMessage message = dataManager.create(AiChatMessage.class);
        message.setConversation(conversation);
        message.setContent(content);
        message.setType(AiChatMessageType.ASSISTANT);
        message.setCreatedDate(timeSource.now().toOffsetDateTime());
        return message;
    }

    protected TimelineItemStatus createTimelineStatus(AiToolStatusUpdate statusUpdate) {
        TimelineItemStatus timelineStatus = dataManager.create(TimelineItemStatus.class);
        timelineStatus.setMessage(statusUpdate.getMessage());
        timelineStatus.setResultSnippet(statusUpdate.getResultSnippet());
        return timelineStatus;
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
                .withHeader(messageBundle.getMessage("aiChatFragment.editConversationTitleDialog.header"))
                .withLabelsPosition(Dialogs.InputDialogBuilder.LabelsPosition.TOP)
                .withParameters(
                        InputParameter.stringParameter("title")
                                .withLabel(messageBundle.getMessage("aiChatFragment.editConversationTitleDialog.titleField"))
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
            return ValidationErrors.of(messageBundle.formatMessage(
                    "aiChatFragment.editConversationTitleDialog.titleTooLong", AiConversation.TITLE_MAX_LENGTH));
        }
        return ValidationErrors.none();
    }

    /**
     * Fired when the title of the displayed conversation changes: a conversation is bound to the fragment,
     * or the bound conversation is renamed.
     */
    public static class TitleChangeEvent extends ComponentEvent<AiChatFragment> {

        @Nullable
        protected final String title;

        public TitleChangeEvent(AiChatFragment source, @Nullable String title) {
            super(source, false);

            this.title = title;
        }

        /**
         * Returns the new conversation title.
         *
         * @return the new title, or {@code null} when no conversation is bound or it has no title
         */
        @Nullable
        public String getTitle() {
            return title;
        }
    }
}
