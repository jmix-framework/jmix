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

package io.jmix.aitoolsflowui.view.chathub;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.icon.AiIconProvider;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.UserAiChatService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chathub.component.AiConversationCard;
import io.jmix.aitoolsflowui.view.chathub.component.AiConversationHistoryGroup;
import io.jmix.aitoolsflowui.view.input.AiChatInputFragment;
import io.jmix.aitoolsflowui.view.chathub.component.HistoryBucket;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
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
import java.util.Optional;

/**
 * Self-contained chat hub UI. Embeds the reusable chat input, shows the
 * current user's most recent chats next to it, and a full searchable,
 * date-bucketed history in a {@link SidePanelLayout}. Designed to be dropped
 * into any host view.
 */
@Experimental
@FragmentDescriptor("ai-chat-hub-fragment.xml")
public class AiChatHubFragment extends Fragment<VerticalLayout> {

    private static final Logger log = LoggerFactory.getLogger(AiChatHubFragment.class);

    protected static final String HERO_ICON_CN = "chat-hub-hero-icon-glyph";

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected Div hubHeroIcon;
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
    protected MetadataTools metadataTools;
    @Autowired
    protected DatatypeFormatter datatypeFormatter;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected RouteSupport routeSupport;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected AiConversationService conversationService;
    @Autowired
    protected UserAiChatService chatService;
    @Autowired
    protected AiToolsFlowuiProperties properties;
    @Autowired
    protected AiIconProvider iconProvider;

    @Nullable
    protected Integer recentChatsCount;
    protected String historyFilter = "";

    @Nullable
    protected SerializableSupplier<Component> markIconSupplier;

    /**
     * Sets a supplier of the brand mark icon shown on the hub hero and conversation cards, letting
     * the host override the default add-on icon. The supplier must return a fresh component on every call.
     *
     * @param markIconSupplier supplier of the mark icon, or {@code null} to use the default
     */
    public void setMarkIconSupplier(@Nullable SerializableSupplier<Component> markIconSupplier) {
        this.markIconSupplier = markIconSupplier;
    }

    /**
     * Overrides the number of recent chats shown next to the chat input.
     * When unset, the value comes from
     * {@code jmix.aitools.ui.chat-hub-recent-chats-count} (default 6).
     *
     * @param recentChatsCount number of recent chats to show
     */
    public void setRecentChatsCount(int recentChatsCount) {
        this.recentChatsCount = recentChatsCount;
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        Component heroIcon = resolveMarkIcon();
        heroIcon.addClassNames("ai-assistant-mark", HERO_ICON_CN);
        hubHeroIcon.removeAll();
        hubHeroIcon.add(heroIcon);

        composerFragment.setSubmitHandler(this::startConversation);

        loadConversations();
        refreshRecentConversationsVisibility();
        renderHistoryList();
        refreshComposerAvailability();
        composerFragment.focus();
    }

    @Install(to = "recentConversationsDl", target = Target.DATA_LOADER)
    public List<AiConversation> recentConversationsLoadDelegate(LoadContext<AiConversation> loadContext) {
        return conversationService.loadConversations().stream()
                .limit(resolveRecentChatsCount())
                .toList();
    }

    @Install(to = "historyConversationsDl", target = Target.DATA_LOADER)
    public List<AiConversation> historyConversationsLoadDelegate(LoadContext<AiConversation> loadContext) {
        return conversationService.loadConversations();
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
    public void onHistorySearchFieldValueChange(final TypedValueChangeEvent<TypedTextField<String>, String> event) {
        historyFilter = Optional.ofNullable(event.getValue()).orElse("").trim().toLowerCase(Locale.ROOT);
        renderHistoryList();
    }

    protected void startConversation(String prompt) {
        AiConversation conversation;
        try {
            conversation = conversationService.create();
        } catch (Exception e) {
            log.error("Failed to create conversation from chat hub", e);
            notifications.create(messageBundle.getMessage("aiChatHubFragment.errorProcessingMessage"))
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

    /**
     * Disables the composer and warns the user when the chat model is not configured —
     * starting a new chat would fail.
     */
    protected void refreshComposerAvailability() {
        if (chatService.isAvailable()) {
            return;
        }
        composerFragment.setInputEnabled(false);
        notifications.create(messageBundle.getMessage("aiChatHubFragment.chatUnavailable"))
                .withType(Notifications.Type.WARNING)
                .show();
    }

    protected void loadConversations() {
        recentConversationsDl.load();
        historyConversationsDl.load();
    }

    protected AiConversationCard createRecentCard(AiConversation conversation) {
        return createCard(conversation, false);
    }

    protected AiConversationCard createHistoryCard(AiConversation conversation) {
        return createCard(conversation, true);
    }

    protected AiConversationCard createCard(AiConversation conversation, boolean deletable) {
        AiConversationCard card = new AiConversationCard();
        card.setIcon(resolveMarkIcon());
        card.setTitle(metadataTools.getInstanceName(conversation));
        card.setCreatedDate(formatDateTime(conversation.getCreatedDate()));
        card.setOpenHandler(() -> openConversation(conversation));
        if (deletable) {
            card.setDeleteHandler(() -> confirmDelete(conversation));
            card.setDeleteAriaLabel(messageBundle.getMessage("aiChatHubFragment.deleteConversation"));
        }
        card.build();
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
                .withHeader(messageBundle.getMessage("aiChatHubFragment.deleteConfirm.header"))
                .withText(messageBundle.getMessage("aiChatHubFragment.deleteConfirm.text"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withVariant(ActionVariant.DANGER)
                                .withHandler(e -> deleteConversation(conversation)),
                        new DialogAction(DialogAction.Type.NO))
                .open();
    }

    protected void deleteConversation(AiConversation conversation) {
        try {
            conversationService.remove(conversation);
        } catch (Exception e) {
            log.error("Failed to delete conversation", e);
            notifications.create(messageBundle.getMessage("aiChatHubFragment.errorProcessingMessage"))
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
            emptyState.setText(messageBundle.getMessage("aiChatHubFragment.historyEmpty"));
            emptyState.addClassName("chat-hub-history-empty");
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
                .filter(c -> metadataTools.getInstanceName(c).toLowerCase(Locale.ROOT).contains(historyFilter))
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
            case TODAY -> messageBundle.getMessage("aiChatHubFragment.historyBucketToday");
            case YESTERDAY -> messageBundle.getMessage("aiChatHubFragment.historyBucketYesterday");
            case LAST_WEEK -> messageBundle.getMessage("aiChatHubFragment.historyBucketLastWeek");
            case EARLIER -> messageBundle.getMessage("aiChatHubFragment.historyBucketEarlier");
        };
    }

    protected Component createHistoryGroup(String bucketLabel, List<AiConversation> conversations) {
        AiConversationHistoryGroup group = new AiConversationHistoryGroup();
        group.setGroup(bucketLabel, conversations, this::createHistoryCard);
        return group;
    }

    protected int resolveRecentChatsCount() {
        return recentChatsCount != null
                ? recentChatsCount
                : properties.getChatHubRecentChatsCount();
    }

    protected Component resolveMarkIcon() {
        return markIconSupplier != null ? markIconSupplier.get() : iconProvider.createMarkIcon();
    }

    @Nullable
    protected String formatDateTime(@Nullable OffsetDateTime dateTime) {
        return dateTime != null ? datatypeFormatter.formatOffsetDateTime(dateTime) : null;
    }
}
