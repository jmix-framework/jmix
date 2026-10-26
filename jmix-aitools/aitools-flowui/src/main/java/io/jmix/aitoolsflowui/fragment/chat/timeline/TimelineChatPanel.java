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

package io.jmix.aitoolsflowui.fragment.chat.timeline;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.HasAiAvatarIcon;
import io.jmix.aitoolsflowui.fragment.chat.renderer.TimelineItemRenderer;
import io.jmix.aitoolsflowui.fragment.chat.support.TimelineItemFactory;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputFragment;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The add-on's {@link AiChatPanel}: the conversation as a timeline of message rows with a thinking row for the
 * running response, and an {@link AiChatInputFragment} below.
 */
@FragmentDescriptor("timeline-chat-panel.xml")
@JsModule("./aitools/ai-chat-timeline-scroll.js")
public class TimelineChatPanel extends Fragment<VerticalLayout> implements AiChatPanel, HasAiAvatarIcon {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected TimelineItemFactory timelineItemFactory;
    @Autowired
    protected Fragments fragments;

    @ViewComponent
    protected VerticalLayout chatInputContainer;
    @ViewComponent
    protected JmixVirtualList<TimelineItem> timelineList;

    @ViewComponent
    protected CollectionContainer<TimelineItem> timelineItemsDc;

    @ViewComponent
    protected AiChatInputFragment chatInput;

    @Nullable
    protected TimelineItem activeThinkingItem;

    @Nullable
    protected SerializableSupplier<Component> aiAvatarIconSupplier;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        timelineList.setRenderer(new ComponentRenderer<>(this::createTimelineRow, TimelineItemRenderer::setItem));
    }

    @Override
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

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public void setSubmitHandler(Consumer<String> handler) {
        chatInput.setSubmitHandler(handler);
    }

    @Override
    public void setStopHandler(Runnable handler) {
        // The timeline has no Stop control.
    }

    @Override
    public void setRetryHandler(Runnable handler) {
        // The timeline has no Retry control.
    }

    @Override
    public void focusInput() {
        chatInput.focus();
    }

    @Override
    public void restoreInputText(String text) {
        chatInput.restoreText(text);
    }

    @Override
    public void setInputEnabled(boolean enabled) {
        chatInput.setInputEnabled(enabled);
    }

    @Override
    public void setInputVisible(boolean visible) {
        chatInputContainer.setVisible(visible);
    }

    @Override
    public void showMessages(Collection<AiChatMessage> messages) {
        timelineItemsDc.setItems(timelineItemFactory.buildTimelineItems(messages));
        scrollToBottom();
    }

    @Override
    public void appendUserMessage(AiChatMessage message) {
        appendTimelineItem(timelineItemFactory.createUserItem(message));
    }

    @Override
    public void showResponseRunning() {
        AiChatMessage placeholder = createTransientAssistantMessage("");
        activeThinkingItem = timelineItemFactory.createThinkingItem(placeholder);
        appendTimelineItem(activeThinkingItem);
    }

    @Override
    public void showStatuses(List<AiToolStatusUpdate> statuses) {
        if (activeThinkingItem == null) {
            return;
        }

        List<TimelineItemStatus> timelineStatuses = statuses.stream()
                .map(this::createTimelineStatus)
                .collect(Collectors.toCollection(ArrayList::new));

        activeThinkingItem.setStatusUpdates(timelineStatuses);
        refreshTimelineItem(activeThinkingItem);
        scrollToBottom(false);
    }

    @Override
    public void hideResponseRunning() {
        if (activeThinkingItem != null) {
            timelineItemsDc.getMutableItems().remove(activeThinkingItem);
            activeThinkingItem = null;
        }
    }

    @Override
    public void showResponseFailed(String errorText) {
        appendTimelineItem(timelineItemFactory.createAssistantItem(createTransientAssistantMessage(errorText)));
    }

    @Override
    public void onResponseCompleted(UUID assistantMessageId) {
        markFreshAssistantItem(assistantMessageId);
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
        // list element and the force flag.
        timelineList.getElement().executeJs("window.jmixAiTools.stickToBottom(this, $0)", force);
    }

    protected AiChatMessage createTransientAssistantMessage(String content) {
        AiChatMessage message = dataManager.create(AiChatMessage.class);
        message.setContent(content);
        message.setType(AiChatMessageType.ASSISTANT);
        message.setCreatedDate(timeSource.now().toOffsetDateTime());
        return message;
    }

    /**
     * Creates a row of the timeline, owned by this panel, the way a {@code fragmentRenderer} declared in a descriptor
     * does, and gives it the avatar of this panel.
     *
     * @return the new row
     */
    protected TimelineItemRenderer createTimelineRow() {
        TimelineItemRenderer row = fragments.create(this, TimelineItemRenderer.class);
        row.setAiAvatarIconSupplier(aiAvatarIconSupplier);
        return row;
    }

    protected TimelineItemStatus createTimelineStatus(AiToolStatusUpdate statusUpdate) {
        TimelineItemStatus timelineStatus = dataManager.create(TimelineItemStatus.class);
        timelineStatus.setMessage(statusUpdate.getMessage());
        timelineStatus.setResultSnippet(statusUpdate.getResultSnippet());
        return timelineStatus;
    }
}
