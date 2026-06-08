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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitoolsflowui.service.AssistantResponseTaskCoordinator;
import io.jmix.aitoolsflowui.view.input.AiChatInputFragment;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitools.service.AiConversationService;
import io.jmix.aitools.tool.AiUiStatusUpdate;
import io.jmix.aitoolsflowui.model.TimelineItem;
import io.jmix.aitoolsflowui.model.TimelineItemType;
import io.jmix.aitoolsflowui.model.TimelineItemStatus;
import io.jmix.aitoolsflowui.service.TimelineItemFactory;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.UiComponentUtils;
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

import java.util.List;
import java.util.UUID;

/**
 * Encapsulates the "AI chat panel" UI — title row, message timeline, thinking
 * indicator and the {@link AiChatInputFragment}
 * composer — for an {@link AiConversation}.
 * Designed to be embedded into any host view (the standard detail view, a
 * side dialog, a chat home view, custom layouts).
 * <p>
 * <b>Ownership.</b> The host supplies the conversation to display via
 * {@link #setConversation(AiConversation)} or {@link #setConversationId(UUID)};
 * the fragment loads that conversation's {@link ChatMessage}s itself — it does
 * not rely on the {@code messages} collection being pre-fetched by the host —
 * and reads/writes directly through {@link DataManager}.
 * <p>
 * <b>Attachments / entity references.</b> Intentionally absent — the add-on's
 * entity model does not carry them. The composer is a plain
 * {@code TextArea} + send button, not the CRM composer fragment.
 * <p>
 * <b>Background task.</b> The LLM call runs through
 * {@link AssistantResponseTaskCoordinator}, which scopes the task to the
 * host {@link View} so cancellation on view detach is correct. The fragment
 * resolves its host view at submit time via {@link UiComponentUtils#findView}.
 */
@FragmentDescriptor("ai-chat-fragment.xml")
public class AiChatFragment extends Fragment<VerticalLayout> {

    private static final Logger log = LoggerFactory.getLogger(AiChatFragment.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected AiConversationService aiConversationService;
    @Autowired
    protected AssistantResponseTaskCoordinator assistantResponseTaskCoordinator;
    @Autowired
    protected TimelineItemFactory timelineItemFactory;

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

    protected AiConversation conversation;
    protected TimelineItem activeThinkingItem;
    protected boolean awaitingResponse;

    protected boolean readOnly;

    /**
     * Binds the fragment to a conversation. Re-renders the timeline,
     * refreshes the title and (re-)enables the composer. Safe to call
     * multiple times — each call rebuilds derived UI state.
     */
    public void setConversation(@Nullable AiConversation conversation) {
        this.conversation = conversation;
        this.activeThinkingItem = null;

        refreshAll();
    }

    /**
     * Binds the fragment to a conversation by id: loads the {@link AiConversation}
     * and delegates to {@link #setConversation(AiConversation)}. A {@code null} id
     * (or an id that no longer resolves) clears the fragment.
     */
    public void setConversationId(@Nullable UUID conversationId) {
        setConversation(conversationId != null ? loadConversation(conversationId) : null);
    }

    /**
     * Hides the composer and the title-edit button.
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        refreshComposerVisibility();
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
     * automatically while an assistant response is in flight and re-enables
     * it on completion; hosts can call this for additional read-only states.
     */
    public void setMessageInputEnabled(boolean enabled) {
        composerFragment.setInputEnabled(enabled);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        composerFragment.setSubmitHandler(this::sendMessage);

        refreshAll();
    }

    @Subscribe(id = "editConversationTitleBtn", subject = "clickListener")
    public void onEditConversationTitleBtnClick(final ClickEvent<JmixButton> event) {
        openTitleEditDialog();
    }

    /**
     * Whether an assistant response is currently being generated for the bound
     * conversation. Hosts can use this to warn the user before closing — the
     * background task is scoped to the host view and is cancelled (losing the
     * answer) when the view detaches.
     */
    public boolean isAwaitingResponse() {
        return awaitingResponse;
    }

    /**
     * Programmatic entry point to send a user message: persists it, appends it
     * to the timeline, shows the thinking indicator, disables the composer and
     * runs the assistant. Used by the composer's submit handler and by hosts
     * that start a conversation with an initial prompt (the chat-home flow).
     */
    public void sendMessage(String userMessage) {
        if (conversation == null) {
            log.warn("Cannot submit message — no conversation bound to the fragment");
            return;
        }
        if (userMessage == null || userMessage.isBlank()) {
            return;
        }

        ChatMessage savedUserMessage;
        try {
            savedUserMessage = aiConversationService.createUserMessage(conversation, userMessage.trim());
        } catch (Exception e) {
            log.error("Failed to persist user message", e);
            notifications.create(messageBundle.getMessage("errorProcessingMessage"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        composerFragment.clear();
        appendTimelineItem(timelineItemFactory.createUserItem(savedUserMessage));
        showThinkingIndicator();
        setMessageInputEnabled(false);
        awaitingResponse = true;

        processUserMessage(savedUserMessage);
    }

    protected void processUserMessage(ChatMessage savedUserMessage) {
        View<?> hostView = UiComponentUtils.findView(this);
        if (hostView == null) {
            log.warn("Fragment is not attached to a view — cannot run assistant response task");
            return;
        }
        assistantResponseTaskCoordinator.run(
                hostView,
                conversation,
                savedUserMessage,
                this::appendThinkingStatusUpdate,
                this::handleAssistantResponseDone,
                this::showAssistantProcessingError
        );
    }

    protected void handleAssistantResponseDone(@Nullable ChatMessage finalMessage) {
        activeThinkingItem = null;
        awaitingResponse = false;
        // Tools may persist side effects (including the new assistant message) in
        // their own transaction, so the in-memory state is stale here. Re-binding
        // by id reloads the conversation and its messages synchronously, after
        // which the fresh assistant row can be stamped.
        if (conversation != null) {
            setConversationId(conversation.getId());
        }
        if (finalMessage != null) {
            markFreshAssistantItem(finalMessage.getId());
        }
        forceMessageInputFocus();
    }

    /**
     * Locates the assistant item that just landed in the container after the
     * post-LLM reload and stamps it with the "fresh" flag, triggering a
     * single-row re-render via {@link CollectionContainer#replaceItem}.
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
        awaitingResponse = false;
        removeThinkingIndicator();
        appendTimelineItem(timelineItemFactory.createAssistantItem(createTransientAssistantMessage(
                messageBundle.getMessage("errorProcessingMessage"))));

        forceMessageInputFocus();
    }

    protected void showThinkingIndicator() {
        ChatMessage placeholder = createTransientAssistantMessage("");
        activeThinkingItem = timelineItemFactory.createThinkingItem(placeholder);
        appendTimelineItem(activeThinkingItem);
    }

    protected void removeThinkingIndicator() {
        if (activeThinkingItem == null) {
            return;
        }
        timelineItemsDc.getMutableItems().remove(activeThinkingItem);
        activeThinkingItem = null;
    }

    protected void appendThinkingStatusUpdate(AiUiStatusUpdate statusUpdate) {
        if (activeThinkingItem == null
                || statusUpdate == null
                || statusUpdate.message() == null
                || statusUpdate.message().isBlank()) {
            return;
        }

        List<TimelineItemStatus> statusUpdates = activeThinkingItem.getStatusUpdates();
        if (!statusUpdates.isEmpty()) {
            TimelineItemStatus last = statusUpdates.get(statusUpdates.size() - 1);
            // Fold only into the last entry if it is still in-flight (no
            // result yet). A completed entry with the same base message
            // belongs to a previous tool call and must not swallow a fresh
            // start phrase for the next call.
            if (last.getMessage().equals(statusUpdate.message()) && !last.isCompleted()) {
                if (statusUpdate.isCompleted()) {
                    statusUpdates.set(statusUpdates.size() - 1, createTimelineStatus(statusUpdate));
                    refreshTimelineItem(activeThinkingItem);
                    scrollToBottom(false);
                }
                return;
            }
        }

        statusUpdates.add(createTimelineStatus(statusUpdate));
        if (statusUpdates.size() > 6) {
            statusUpdates.remove(0);
        }
        refreshTimelineItem(activeThinkingItem);
        scrollToBottom(false);
    }

    protected void refreshAll() {
        conversationTitle.setText(conversation != null ? conversation.getTitle() : "");

        timelineItemsDc.setItems(timelineItemFactory.buildTimelineItems(loadMessages()));
        scrollToBottom();

        refreshComposerVisibility();
    }

    /**
     * Loads all messages of the bound conversation, oldest first. Empty when no
     * conversation is bound or it has not been persisted yet.
     */
    protected List<ChatMessage> loadMessages() {
        if (conversation == null || conversation.getId() == null) {
            return List.of();
        }
        return aiConversationService.loadMessages(conversation.getId());
    }

    protected void forceMessageInputFocus() {
        setMessageInputEnabled(true);
        focusMessageInput();
    }

    protected void refreshComposerVisibility() {
        boolean show = conversation != null && !readOnly;
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
        if (size <= 0) {
            return;
        }
        if (force) {
            // Coarse server-side positioning: makes the virtualizer render the
            // rows near the end so their real heights get measured. Skipped
            // for the conditional case — it would yank a user who scrolled up.
            timelineList.scrollToIndex(size - 1);
        }
        // Rows have variable height and, crucially, assistant answers render
        // through the <vaadin-markdown> web component which parses and lays
        // out its content asynchronously *after* the row's initial render,
        // in bursts. A one-shot re-pin fires before that growth lands; an
        // "until scrollHeight is stable" loop exits during a lull between
        // bursts (leaving the scroll "lower, but not at the bottom"). Instead
        // we react to the actual cause: a MutationObserver re-pins on every
        // DOM change (markdown rendering, row recycling) and we also re-pin
        // every frame for a bounded window, then disconnect.
        //
        // "Stick to bottom" intent lives in l.__stick, kept up to date by a
        // one-shot user-scroll listener: content growth alone fires no scroll
        // event (so it never clears the flag), and our own pins land at the
        // bottom (so they keep it set) — only a genuine user scroll-up clears
        // it. force=true resets the flag; force=false honours it and bails out
        // when the user is reading higher up. The per-element stop handle
        // cancels an in-flight pin when a new scroll request arrives (e.g.
        // back-to-back streaming updates) so loops don't stack.
        timelineList.getElement().executeJs("""
                        const l = this;
                        const force = $0;
                        const THRESHOLD = 50;
                        if (!l.__stickInit) {
                          l.__stickInit = true;
                          l.__stick = true;
                          l.addEventListener('scroll', () => {
                            l.__stick = l.scrollTop + l.clientHeight >= l.scrollHeight - THRESHOLD;
                          }, { passive: true });
                        }
                        if (force) { l.__stick = true; }
                        if (!l.__stick) { return; }
                        if (l.__scrollPinStop) { l.__scrollPinStop(); }
                        const toBottom = () => { if (l.__stick) { l.scrollTop = l.scrollHeight; } };
                        const mo = new MutationObserver(toBottom);
                        mo.observe(l, { childList: true, subtree: true,
                                        characterData: true, attributes: true });
                        const stop = () => { l.__scrollPinStop = null; mo.disconnect(); };
                        l.__scrollPinStop = stop;
                        let frames = 0;
                        const tick = () => {
                          if (l.__scrollPinStop !== stop) { return; }
                          if (!l.__stick) { stop(); return; }
                          toBottom();
                          if (++frames < 120) { requestAnimationFrame(tick); }
                          else { stop(); }
                        };
                        requestAnimationFrame(tick);
                        """,
                force);
    }

    protected ChatMessage createTransientAssistantMessage(String content) {
        ChatMessage message = dataManager.create(ChatMessage.class);
        message.setConversation(conversation);
        message.setContent(content);
        message.setType(ChatMessageType.ASSISTANT);
        message.setCreatedDate(timeSource.now().toOffsetDateTime());
        message.setCreatedBy(currentAuthentication.getUser().getUsername());
        return message;
    }

    protected TimelineItemStatus createTimelineStatus(AiUiStatusUpdate statusUpdate) {
        TimelineItemStatus timelineStatus = dataManager.create(TimelineItemStatus.class);
        timelineStatus.setMessage(statusUpdate.message());
        timelineStatus.setResultSnippet(statusUpdate.resultSnippet());
        return timelineStatus;
    }

    /**
     * Loads the conversation entity by id (without its {@code messages}
     * collection — messages are loaded separately via
     * {@link AiConversationService#loadMessages(UUID)}). Returns {@code null}
     * if the conversation no longer exists.
     */
    @Nullable
    protected AiConversation loadConversation(UUID conversationId) {
        return dataManager.load(AiConversation.class)
                .id(conversationId)
                .optional()
                .orElse(null);
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
                    if (conversation == null) {
                        return;
                    }
                    conversation.setTitle(updatedTitle.trim());
                    // Persist only the title and refresh just the header.
                    // Reloading conversation while awaiting LLM answer
                    // may break UI.
                    conversationTitle.setText(conversation.getTitle());
                })
                .open();
    }

}
