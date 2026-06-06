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

package io.jmix.aitoolsflowui.view.chathome;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitools.service.AiConversationService;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chathome.component.AiAssistantIcon;
import io.jmix.aitoolsflowui.view.chathome.component.AiConversationCard;
import io.jmix.aitoolsflowui.view.chathome.component.AiConversationHistoryGroup;
import io.jmix.aitoolsflowui.view.input.AiChatInputFragment;
import io.jmix.aitoolsflowui.view.chathome.component.HistoryBucket;
import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.gridlayout.GridLayout;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Self-contained chat home UI. Embeds the reusable chat input, shows the
 * current user's most recent chats next to it, and a full searchable,
 * date-bucketed history in a {@link SidePanelLayout}. Designed to be dropped
 * into any host view; the add-on also ships {@code AiChatHomeView}
 * as a ready-made host.
 * <p>
 * Starting a chat creates a conversation and navigates to {@link AiChatView}
 * (passing the conversation id as a route parameter), forwarding the prompt
 * via {@code withAfterNavigationHandler} →
 * {@link AiChatView#sendInitialPrompt(String)} (works in both standard routing
 * and tabbed mode).
 */
@FragmentDescriptor("ai-chat-home-fragment.xml")
public class AiChatHomeFragment extends Fragment<VerticalLayout> {

    private static final Logger log = LoggerFactory.getLogger(AiChatHomeFragment.class);

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected Div homeHeroIcon;
    @ViewComponent
    protected AiChatInputFragment composerFragment;
    @ViewComponent
    protected CollectionContainer<AiConversation> recentConversationsDc;
    @ViewComponent
    protected CollectionLoader<AiConversation> recentConversationsDl;
    @ViewComponent
    protected CollectionContainer<AiConversation> historyConversationsDc;
    @ViewComponent
    protected CollectionLoader<AiConversation> historyConversationsDl;
    @ViewComponent
    protected GridLayout<AiConversation> recentConversationsGridLayout;
    @ViewComponent
    protected Component recentConversationsHeader;
    @ViewComponent
    protected SidePanelLayout historySidePanel;
    @ViewComponent
    protected VerticalLayout historyListContainer;
    @ViewComponent
    protected Span historyPanelCount;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DatatypeFormatter datatypeFormatter;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected RouteSupport routeSupport;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected AiConversationService aiConversationService;
    @Autowired
    protected AiToolsFlowuiProperties properties;

    @Nullable
    protected Integer recentChatsCount;
    protected String historyFilter = "";

    /**
     * Overrides the number of recent chats shown next to the chat input.
     * When unset, the value comes from
     * {@code jmix.aitools.ui.chat-home-recent-chats-count} (default 6).
     */
    public void setRecentChatsCount(int recentChatsCount) {
        this.recentChatsCount = recentChatsCount;
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        AiAssistantIcon heroIcon = new AiAssistantIcon();
        heroIcon.addClassName("chat-home-hero-icon-glyph");
        homeHeroIcon.removeAll();
        homeHeroIcon.add(heroIcon);

        composerFragment.setSubmitHandler(this::startConversation);

        loadConversations();
        refreshRecentConversationsVisibility();
        renderHistoryList();
        composerFragment.focus();
    }

    protected void loadConversations() {
        String username = currentAuthentication.getUser().getUsername();

        recentConversationsDl.setParameter("currentUser", username);
        recentConversationsDl.setMaxResults(resolveRecentChatsCount());
        recentConversationsDl.load();

        historyConversationsDl.setParameter("currentUser", username);
        historyConversationsDl.load();
    }

    protected void startConversation(String prompt) {
        AiConversation conversation;
        try {
            conversation = aiConversationService.createNewConversation();
        } catch (Exception e) {
            log.error("Failed to create conversation from chat home", e);
            notifications.create(messageBundle.getMessage("errorProcessingMessage"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        composerFragment.clear();

        viewNavigators.view(UiComponentUtils.getView(this), AiChatView.class)
                .withRouteParameters(routeSupport.createRouteParameters(
                        AiChatView.ROUTE_PARAM_ID, conversation.getId()))
                .withAfterNavigationHandler(e -> e.getView().sendInitialPrompt(prompt))
                .navigate();
    }

    protected void refreshRecentConversationsVisibility() {
        boolean hasRecent = !recentConversationsDc.getItems().isEmpty();
        recentConversationsHeader.setVisible(hasRecent);
        recentConversationsGridLayout.setVisible(hasRecent);
    }

    @Supply(to = "recentConversationsGridLayout", subject = "renderer")
    protected ComponentRenderer<AiConversationCard, AiConversation> recentConversationsGridLayoutRenderer() {
        return new ComponentRenderer<>(this::createRecentCard);
    }

    @Subscribe("showAllHistoryBtn")
    public void onShowAllHistoryBtnClick(final ClickEvent<JmixButton> event) {
        renderHistoryList();
        historySidePanel.openSidePanel();
    }

    @Subscribe("historyCloseBtn")
    public void onHistoryCloseBtnClick(final ClickEvent<JmixButton> event) {
        historySidePanel.closeSidePanel();
    }

    @Subscribe("historyNewBtn")
    public void onHistoryNewBtnClick(final ClickEvent<JmixButton> event) {
        historySidePanel.closeSidePanel();
        composerFragment.focus();
    }

    @Subscribe("historySearchField")
    public void onHistorySearchFieldValueChange(
            final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
        historyFilter = Optional.ofNullable(event.getValue()).orElse("").trim().toLowerCase(Locale.ROOT);
        renderHistoryList();
    }

    protected AiConversationCard createRecentCard(AiConversation conversation) {
        return createCard(conversation, false);
    }

    protected AiConversationCard createHistoryCard(AiConversation conversation) {
        return createCard(conversation, true);
    }

    protected AiConversationCard createCard(AiConversation conversation, boolean deletable) {
        AiConversationCard card = new AiConversationCard();
        card.setConversation(
                metadataTools.getInstanceName(conversation),
                formatDateTime(conversation.getCreatedDate()),
                () -> openConversation(conversation),
                deletable ? () -> confirmDelete(conversation) : null,
                deletable ? messageBundle.getMessage("aiChatHomeFragment.deleteConversation") : null);
        return card;
    }

    protected void openConversation(AiConversation conversation) {
        viewNavigators.view(UiComponentUtils.getView(this), AiChatView.class)
                .withRouteParameters(routeSupport.createRouteParameters(
                        AiChatView.ROUTE_PARAM_ID, conversation.getId()))
                .navigate();
    }

    protected void confirmDelete(AiConversation conversation) {
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("aiChatHomeFragment.deleteConfirm.header"))
                .withText(messageBundle.getMessage("aiChatHomeFragment.deleteConfirm.text"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withVariant(ActionVariant.DANGER)
                                .withHandler(e -> deleteConversation(conversation)),
                        new DialogAction(DialogAction.Type.NO))
                .open();
    }

    protected void deleteConversation(AiConversation conversation) {
        try {
            dataManager.remove(conversation);
        } catch (Exception e) {
            log.error("Failed to delete conversation", e);
            notifications.create(messageBundle.getMessage("errorProcessingMessage"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }
        loadConversations();
        refreshRecentConversationsVisibility();
        renderHistoryList();
    }

    protected void renderHistoryList() {
        List<AiConversation> all = historyConversationsDc.getItems();
        historyPanelCount.setText(String.valueOf(all.size()));

        List<AiConversation> filtered = applyHistoryFilter(all);

        historyListContainer.removeAll();
        if (filtered.isEmpty()) {
            Span emptyState = uiComponents.create(Span.class);
            emptyState.setText(messageBundle.getMessage("aiChatHomeFragment.historyEmpty"));
            emptyState.addClassName("chat-home-history-empty");
            historyListContainer.add(emptyState);
            return;
        }

        Map<HistoryBucket, List<AiConversation>> grouped = groupByBucket(filtered);
        grouped.forEach((bucket, items) ->
                historyListContainer.add(createHistoryGroup(bucketLabel(bucket), items)));
    }

    protected List<AiConversation> applyHistoryFilter(List<AiConversation> conversations) {
        if (historyFilter.isEmpty()) {
            return conversations;
        }
        return conversations.stream()
                .filter(c -> {
                    String title = Optional.of(metadataTools.getInstanceName(c))
                            .orElse("").toLowerCase(Locale.ROOT);
                    if (title.contains(historyFilter)) {
                        return true;
                    }
                    return firstUserMessageSnippet(c).toLowerCase(Locale.ROOT).contains(historyFilter);
                })
                .toList();
    }

    protected Map<HistoryBucket, List<AiConversation>> groupByBucket(List<AiConversation> conversations) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        // Seed with an empty, ordered map so buckets always render
        // most-recent-first regardless of the data order.
        Map<HistoryBucket, List<AiConversation>> grouped = new LinkedHashMap<>();
        for (HistoryBucket bucket : HistoryBucket.values()) {
            grouped.put(bucket, new ArrayList<>());
        }
        for (AiConversation conversation : conversations) {
            HistoryBucket bucket = HistoryBucket.of(
                    conversation.getCreatedDate(), today, zone);
            grouped.get(bucket).add(conversation);
        }
        grouped.values().removeIf(List::isEmpty);
        return grouped;
    }

    protected String bucketLabel(HistoryBucket bucket) {
        return switch (bucket) {
            case TODAY -> messageBundle.getMessage("aiChatHomeFragment.historyBucketToday");
            case YESTERDAY -> messageBundle.getMessage("aiChatHomeFragment.historyBucketYesterday");
            case LAST_WEEK -> messageBundle.getMessage("aiChatHomeFragment.historyBucketLastWeek");
            case EARLIER -> messageBundle.getMessage("aiChatHomeFragment.historyBucketEarlier");
        };
    }

    protected Component createHistoryGroup(String bucketLabel, List<AiConversation> conversations) {
        AiConversationHistoryGroup group = new AiConversationHistoryGroup();
        group.setGroup(bucketLabel, conversations, this::createHistoryCard);
        return group;
    }

    protected String firstUserMessageSnippet(AiConversation conversation) {
        List<ChatMessage> messages = conversation.getMessages();
        if (messages == null) {
            return "";
        }
        return messages.stream()
                .filter(Objects::nonNull)
                .filter(m -> ChatMessageType.USER.equals(m.getType()))
                .map(ChatMessage::getContent)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    protected int resolveRecentChatsCount() {
        return recentChatsCount != null
                ? recentChatsCount
                : properties.getChatHomeRecentChatsCount();
    }

    @Nullable
    protected String formatDateTime(@Nullable OffsetDateTime dateTime) {
        return dateTime != null ? datatypeFormatter.formatOffsetDateTime(dateTime) : null;
    }
}
