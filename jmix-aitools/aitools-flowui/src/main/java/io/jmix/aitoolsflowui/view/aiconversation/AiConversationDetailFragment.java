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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitools.service.AiConversationService;
import io.jmix.aitools.service.AiUiStatusUpdate;
import io.jmix.aitoolsflowui.model.TimelineItem;
import io.jmix.aitoolsflowui.model.TimelineItemType;
import io.jmix.aitoolsflowui.model.TimelineItemStatus;
import io.jmix.aitoolsflowui.service.TimelineItemFactory;
import io.jmix.aitoolsflowui.view.aiconversation.support.AssistantResponseTaskCoordinator;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
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
 * indicator and {@link MessageInput} composer — for an {@link AiConversation}.
 * Designed to be embedded into different host views (the detail view, a side
 * dialog, a starter view, custom layouts).
 * <p>
 * <b>Ownership.</b> The fragment is UI-only: the host view owns the data
 * container that holds the {@link AiConversation} and feeds the fragment via
 * {@link #setConversation(AiConversation)}. The fragment never reloads the
 * container itself; instead it asks the host to reload after a message is
 * persisted, via the {@code onReload} callback.
 * <p>
 * <b>Attachments / entity references.</b> Intentionally absent — the add-on's
 * entity model does not carry them. The composer is a stock Vaadin
 * {@link MessageInput}, not the CRM composer fragment.
 * <p>
 * <b>Background task.</b> The LLM call runs through
 * {@link AssistantResponseTaskCoordinator}, which scopes the task to the
 * host {@link View} so cancellation on view detach is correct. The fragment
 * resolves its host view at submit time via {@link UiComponentUtils#findView}.
 */
@FragmentDescriptor("ai-conversation-detail-fragment.xml")
public class AiConversationDetailFragment extends Fragment<VerticalLayout> {

    private static final Logger log = LoggerFactory.getLogger(AiConversationDetailFragment.class);

    @Autowired
    private DataManager dataManager;
    @Autowired
    private FetchPlans fetchPlans;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Notifications notifications;
    @Autowired
    private AiConversationService aiConversationService;
    @Autowired
    private AssistantResponseTaskCoordinator assistantResponseTaskCoordinator;
    @Autowired
    protected TimelineItemFactory timelineItemFactory;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private H3 conversationTitle;
    @ViewComponent
    private JmixButton editConversationTitleBtn;
    @ViewComponent
    private VerticalLayout composerContainer;
    @ViewComponent
    private JmixVirtualList<TimelineItem> timelineList;

    @ViewComponent
    private CollectionContainer<TimelineItem> timelineItemsDc;

    private MessageInput messageInput;

    private AiConversation conversation;
    private TimelineItem activeThinkingItem;

    private PersistDelegate persistDelegate = this::defaultPersist;
    private ReloadDelegate reloadDelegate = this::defaultReload;

    private boolean readOnly;

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
     * Overrides how the fragment persists the conversation entity (currently
     * used after a title edit). Passing {@code null} restores the default,
     * which calls {@link DataManager#save(Object)} directly. A host view
     * typically supplies a delegate that goes through its own
     * {@code DataContext} so other UI bound to the same container stays
     * consistent.
     */
    public void setPersistDelegate(@Nullable PersistDelegate persistDelegate) {
        this.persistDelegate = persistDelegate != null ? persistDelegate : this::defaultPersist;
    }

    /**
     * Overrides how the fragment reloads the conversation entity (after a
     * title edit and after the LLM assistant has finished writing). Passing
     * {@code null} restores the default, which reads through
     * {@link DataManager} with a fetch plan that includes the
     * {@code messages} collection. A host view typically supplies a delegate
     * that calls {@code getViewData().loadAll()} so its container reflects
     * the freshly loaded instance.
     */
    public void setReloadDelegate(@Nullable ReloadDelegate reloadDelegate) {
        this.reloadDelegate = reloadDelegate != null ? reloadDelegate : this::defaultReload;
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
        if (messageInput.isEnabled()) {
            messageInput.focus();
        }
    }

    /**
     * Enables or disables the message composer. The fragment disables it
     * automatically while an assistant response is in flight and re-enables
     * it on completion; hosts can call this for additional read-only states.
     */
    public void setMessageInputEnabled(boolean enabled) {
        messageInput.setEnabled(enabled);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        messageInput = createMessageInput();
        composerContainer.add(messageInput);

        refreshAll();
    }

    @Subscribe(id = "editConversationTitleBtn", subject = "clickListener")
    public void onEditConversationTitleBtnClick(final ClickEvent<JmixButton> event) {
        openTitleEditDialog();
    }

    protected void onMessageSubmit(MessageInput.SubmitEvent event) {
        if (conversation == null) {
            log.warn("Cannot submit message — no conversation bound to the fragment");
            return;
        }

        String userMessage = event.getValue();
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

        appendTimelineItem(timelineItemFactory.createUserItem(savedUserMessage));
        showThinkingIndicator();
        setMessageInputEnabled(false);

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
        // Tools may persist side effects in their own transaction, so the
        // in-memory conversation is stale here. The reload is synchronous —
        // whether it goes through DataManager (default) or a host-supplied
        // DataLoader delegate, by the time it returns we have a fresh entity
        // (with the new assistant message inside) and can stamp it fresh.
        AiConversation reloaded = reloadDelegate.reload(conversation);
        setConversation(reloaded);
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
        removeThinkingIndicator();
        appendTimelineItem(timelineItemFactory.createAssistantItem(createTransientAssistantMessage(
                messageBundle.getMessage("errorProcessingMessage"))));

        forceMessageInputFocus();
    }

    // -- Thinking indicator ---------------------------------------------------

    protected void showThinkingIndicator() {
        ChatMessage placeholder = createTransientAssistantMessage("");
        activeThinkingItem = timelineItemFactory.createThinkingItem(placeholder);
        appendTimelineItem(activeThinkingItem);
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
                    scrollToBottom();
                }
                return;
            }
        }

        statusUpdates.add(createTimelineStatus(statusUpdate));
        if (statusUpdates.size() > 6) {
            statusUpdates.remove(0);
        }
        refreshTimelineItem(activeThinkingItem);
        scrollToBottom();
    }

    protected void removeThinkingIndicator() {
        if (activeThinkingItem == null) {
            return;
        }
        timelineItemsDc.getMutableItems().remove(activeThinkingItem);
        activeThinkingItem = null;
    }

    protected void refreshAll() {
        conversationTitle.setText(conversation != null ? conversation.getTitle() : "");

        timelineItemsDc.setItems(timelineItemFactory.buildTimelineItems(conversation));
        scrollToBottom();

        refreshComposerVisibility();
    }

    protected void forceMessageInputFocus() {
        setMessageInputEnabled(true);
        focusMessageInput();
    }

    private void refreshComposerVisibility() {
        boolean show = conversation != null && !readOnly;
        composerContainer.setVisible(show);
        editConversationTitleBtn.setVisible(show);
    }

    private void appendTimelineItem(TimelineItem item) {
        timelineItemsDc.getMutableItems().add(item);
        scrollToBottom();
    }

    private void refreshTimelineItem(TimelineItem item) {
        // replaceItem fires SET_ITEM on the container, which makes the
        // virtual list re-render this single row — needed when in-place
        // mutations on the item (e.g. statusUpdates of the thinking row)
        // wouldn't otherwise be observable to the data provider.
        timelineItemsDc.replaceItem(item);
    }

    private void scrollToBottom() {
        int size = timelineItemsDc.getItems().size();
        if (size > 0) {
            timelineList.scrollToIndex(size - 1);
        }
    }

    protected MessageInput createMessageInput() {
        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();
        messageInput.addSubmitListener(this::onMessageSubmit);
        messageInput.addClassName("ai-conversation-message-input");
        return messageInput;
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
     * Default {@link PersistDelegate}: hands the conversation to
     * {@link DataManager#save(Object)} and returns the saved instance.
     * Bypasses any view-level {@code DataContext}, so a host that has other
     * UI bound to the same container should provide its own delegate.
     */
    protected AiConversation defaultPersist(AiConversation conversation) {
        return dataManager.save(conversation);
    }

    /**
     * Default {@link ReloadDelegate}: re-reads the conversation by id with a
     * fetch plan that brings in the {@code messages} collection (the timeline
     * cannot render without it). The host can override with a delegate that
     * goes through its own {@code DataLoader} so the host's container stays
     * in sync.
     */
    protected AiConversation defaultReload(AiConversation conversation) {
        FetchPlan fetchPlan = fetchPlans.builder(AiConversation.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("messages", FetchPlan.BASE)
                .build();
        return dataManager.load(AiConversation.class)
                .id(conversation.getId())
                .fetchPlan(fetchPlan)
                .one();
    }

    /**
     * Persists the conversation. Receives the current in-memory instance
     * (with any pending edits applied by the fragment, e.g. a new title) and
     * returns the saved/merged instance the fragment should treat as its
     * live reference going forward.
     */
    @FunctionalInterface
    public interface PersistDelegate {
        AiConversation persist(AiConversation conversation);
    }

    /**
     * Reloads the conversation. Receives the current live instance, returns
     * a freshly-loaded one (typically with the {@code messages} collection
     * eagerly fetched so the timeline can be rebuilt).
     */
    @FunctionalInterface
    public interface ReloadDelegate {
        AiConversation reload(AiConversation conversation);
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
                    AiConversation saved = persistDelegate.persist(conversation);
                    AiConversation reloaded = reloadDelegate.reload(saved);
                    setConversation(reloaded);
                })
                .open();
    }
}
