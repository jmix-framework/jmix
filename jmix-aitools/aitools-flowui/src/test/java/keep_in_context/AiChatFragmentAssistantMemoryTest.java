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

package keep_in_context;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelProvider;
import io.jmix.aitoolsflowui.fragment.chat.action.RenameConversationAction;
import io.jmix.aitoolsflowui.fragment.chat.action.ShowAssistantMemoryAction;
import io.jmix.aitoolsflowui.fragment.chat.memory.AssistantMemoryFragment;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.fragment.FragmentOwner;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.node.ObjectNode;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatInputPanel;
import test_support.TestAiChatMessageService;
import test_support.TestAiConversationService;
import test_support.TestKeepInContextAiChatPanel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * The chat fragment keeps messages in the assistant memory on the panel's request and from its own memory side
 * panel through one path, counts them in the title-row menu and lists them, oldest first, in the side panel.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiChatFragmentAssistantMemoryTest.TestPanelConfiguration.class})
class AiChatFragmentAssistantMemoryTest {

    static final String CARD_CN = "assistant-memory-card";
    static final String CARD_TEXT_CN = "assistant-memory-card-text";
    static final String CARD_REMOVE_CN = "assistant-memory-card-remove";

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    RouteSupport routeSupport;
    @Autowired
    TestAiConversationService conversationService;
    @Autowired
    TestAiChatMessageService messageService;
    @Autowired
    Messages messages;

    // Deep stubs keep the builders of notifications and dialogs from failing on a null result.
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    Notifications notifications;
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    Dialogs dialogs;

    AiConversation conversation;

    @BeforeEach
    void setUp() {
        conversation = conversationService.addConversation("Chat");
    }

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
        TestPanelProvider.lastChatPanel = null;
    }

    @Test
    void panelKeepRequest_done_keepsMessage_refreshesItsMark_andTheMemory() {
        AiChatMessage message = userMessage("Remember me", false);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);
        TestKeepInContextAiChatPanel panel = lastChatPanel();

        panel.requestKeepInContext(message, true);

        assertTrue(message.getKeepInContext());
        assertEquals(List.of(message), panel.refreshedMessages);
        assertEquals(menuItemText(1), memoryAction(fragment).getText());
        assertEquals(List.of("Remember me"), cardTexts(fragment));
    }

    @Test
    void panelReleaseRequest_done_releasesMessage() {
        AiChatMessage message = userMessage("Remember me", true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);
        TestKeepInContextAiChatPanel panel = lastChatPanel();

        panel.requestKeepInContext(message, false);

        assertFalse(message.getKeepInContext());
        assertEquals(List.of(message), panel.refreshedMessages);
        assertEquals(menuItemText(0), memoryAction(fragment).getText());
        assertTrue(cardTexts(fragment).isEmpty());
    }

    @Test
    void panelKeepRequest_limitReached_showsNotification_andChangesNothing() {
        messageService.setKeepInContextLimit(1);
        userMessage("Kept", true);
        AiChatMessage other = userMessage("Other", false);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);
        TestKeepInContextAiChatPanel panel = lastChatPanel();

        panel.requestKeepInContext(other, true);

        verify(notifications).create(messages.formatMessage("", "assistantMemory.limitReached", 1));
        assertFalse(Boolean.TRUE.equals(other.getKeepInContext()));
        assertTrue(panel.refreshedMessages.isEmpty());
        assertEquals(menuItemText(1), memoryAction(fragment).getText());
        assertEquals(List.of("Kept"), cardTexts(fragment));
    }

    @Test
    void panelKeepRequest_messageGone_resyncsInsteadOfFailing() {
        AiChatMessage message = userMessage("Remember me", false);
        AiChatFragment fragment = chatFragment(navigate());
        TestKeepInContextAiChatPanel panel = lastChatPanel();
        int showMessagesCalls = panel.showMessagesCalls;
        messageService.setKeepInContextFailure(new IllegalArgumentException("Message not found"));

        panel.requestKeepInContext(message, true);

        assertEquals(showMessagesCalls + 1, panel.showMessagesCalls);
        assertTrue(panel.refreshedMessages.isEmpty());
        assertFalse(Boolean.TRUE.equals(message.getKeepInContext()));
        assertEquals(menuItemText(0), memoryAction(fragment).getText());
    }

    @Test
    void panelKeepRequest_accessDenied_resyncsInsteadOfFailing() {
        AiChatMessage message = userMessage("Remember me", false);
        AiChatFragment fragment = chatFragment(navigate());
        TestKeepInContextAiChatPanel panel = lastChatPanel();
        int showMessagesCalls = panel.showMessagesCalls;
        messageService.setKeepInContextFailure(new AccessDeniedException("entity", "aitls_AiConversation"));

        panel.requestKeepInContext(message, true);

        assertEquals(showMessagesCalls + 1, panel.showMessagesCalls);
        assertTrue(panel.refreshedMessages.isEmpty());
        assertEquals(menuItemText(0), memoryAction(fragment).getText());
    }

    @Test
    void menuItem_countsKeptMessages() {
        userMessage("First", true);
        userMessage("Second", true);
        userMessage("Third", false);

        AiChatFragment fragment = chatFragment(navigate());

        assertEquals(menuItemText(2), memoryAction(fragment).getText());
    }

    @Test
    void menuItem_nothingKept_isOfferedWithZero() {
        AiChatFragment fragment = chatFragment(navigate());

        ActionItem item = memoryItem(fragment);
        assertEquals(menuItemText(0), item.getAction().getText());
        assertTrue(item.isVisible());
        assertTrue(item.isEnabled());
    }

    @Test
    void renameItem_opensTitleDialog() {
        AiChatFragment fragment = chatFragment(navigate());
        ActionItem item = (ActionItem) Objects.requireNonNull(menu(fragment).getItem(RenameConversationAction.ID));

        item.getAction().actionPerform(menu(fragment));

        verify(dialogs).createInputDialog(any());
    }

    @Test
    void memoryItem_opensSidePanel_withKeptMessagesOldestFirst() {
        userMessage("First", true);
        userMessage("Not kept", false);
        messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Second", true);
        AiChatFragment fragment = chatFragment(navigate());

        openMemory(fragment);

        assertTrue(sidePanel(fragment).isSidePanelOpened());
        assertEquals(List.of("First", "Second"), cardTexts(fragment));
        assertFalse(emptyState(fragment).isVisible());
    }

    @Test
    void memory_nothingKept_showsEmptyState() {
        AiChatFragment fragment = chatFragment(navigate());

        openMemory(fragment);

        assertTrue(cardTexts(fragment).isEmpty());
        assertTrue(emptyState(fragment).isVisible());
    }

    @Test
    void memoryCard_longText_isCutAtTheLimit() {
        userMessage("a".repeat(400), true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);

        assertEquals(List.of("a".repeat(300) + "…"), cardTexts(fragment));
    }

    @Test
    void memoryCard_assistantMessage_isWholeMarkdown() {
        String table = IntStream.rangeClosed(1, 10)
                .mapToObj(row -> "| row " + row + " |")
                .collect(Collectors.joining("\n", "| login |\n|---|\n", ""));
        messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, table, true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);

        Component text = descendants(memoryFragment(fragment))
                .filter(component -> component instanceof Markdown)
                .findFirst()
                .orElseThrow();
        assertEquals(table, ((Markdown) text).getContent());
    }

    @Test
    void memoryCard_cutInsideSurrogatePair_keepsNoHalfOfIt() {
        // The emoji occupies indexes 299 and 300, so a cut at 300 would split it.
        userMessage("a".repeat(299) + "😀" + "b".repeat(50), true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);

        assertEquals(List.of("a".repeat(299) + "…"), cardTexts(fragment));
    }

    @Test
    void memoryCardClick_keepsSidePanelOpen_andScrollsToMessage() {
        AiChatMessage message = userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        TestKeepInContextAiChatPanel panel = lastChatPanel();
        openMemory(fragment);

        clickCard(cards(fragment).get(0), false);

        assertTrue(sidePanel(fragment).isSidePanelOpened());
        assertEquals(List.of(message.getId()), panel.scrolledToMessages.stream().map(AiChatMessage::getId).toList());
    }

    @Test
    void memoryCardRemoveClick_doesNotScrollToMessage() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        TestKeepInContextAiChatPanel panel = lastChatPanel();
        openMemory(fragment);

        clickCard(cards(fragment).get(0), true);

        assertTrue(panel.scrolledToMessages.isEmpty());
    }

    @Test
    void memoryRemove_releasesMessage_throughTheSamePath() {
        AiChatMessage message = userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        TestKeepInContextAiChatPanel panel = lastChatPanel();
        openMemory(fragment);

        removeButtons(fragment).get(0).click();

        assertTrue(messageService.loadKeptInContextMessages(conversation).isEmpty());
        assertEquals(List.of(message.getId()), panel.refreshedMessages.stream().map(AiChatMessage::getId).toList());
        assertEquals(menuItemText(0), memoryAction(fragment).getText());
        assertTrue(cardTexts(fragment).isEmpty());
        assertTrue(sidePanel(fragment).isSidePanelOpened());
    }

    @Test
    void setConversation_closesOpenMemory() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);
        assertTrue(sidePanel(fragment).isSidePanelOpened());

        fragment.setConversation(conversationService.addConversation("Another"));

        assertFalse(sidePanel(fragment).isSidePanelOpened());
    }

    @Test
    void sameConversationBoundAgain_keepsMemoryOpen() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);

        // As after each finished response: the same conversation is bound again by id.
        fragment.setConversationId(conversation.getId());

        assertTrue(sidePanel(fragment).isSidePanelOpened());
    }

    @Test
    void memoryOpenedAgain_isCreatedAnew() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);
        AssistantMemoryFragment first = memoryFragment(fragment);

        sidePanel(fragment).closeSidePanel();
        openMemory(fragment);

        assertNotSame(first, memoryFragment(fragment));
        assertEquals(List.of("Kept"), cardTexts(fragment));
    }

    @Test
    void clearConversation_closesOpenMemory() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        openMemory(fragment);
        assertTrue(sidePanel(fragment).isSidePanelOpened());

        fragment.setConversation(null);

        assertFalse(sidePanel(fragment).isSidePanelOpened());
    }

    @Test
    void readOnly_clearsPanelHandler_hidesRemoveAndRename_keepsMemoryAvailable() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        TestKeepInContextAiChatPanel panel = lastChatPanel();
        assertNotNull(panel.keepInContextHandler);

        fragment.setReadOnly(true);
        openMemory(fragment);

        assertNull(panel.keepInContextHandler);
        assertEquals(List.of("Kept"), cardTexts(fragment));
        assertTrue(removeButtons(fragment).isEmpty());
        assertTrue(memoryItem(fragment).isVisible());
        assertFalse(Objects.requireNonNull(menu(fragment).getItem(RenameConversationAction.ID)).isVisible());
        assertTrue(menu(fragment).isVisible());
    }

    @Test
    void readOnlyCleared_setsPanelHandler_andShowsRemoveAgain() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());
        TestKeepInContextAiChatPanel panel = lastChatPanel();
        fragment.setReadOnly(true);

        fragment.setReadOnly(false);
        openMemory(fragment);

        assertNotNull(panel.keepInContextHandler);
        assertEquals(1, removeButtons(fragment).size());
    }

    @Test
    void noConversation_hidesTheMenu_andCountsNothing() {
        userMessage("Kept", true);
        AiChatFragment fragment = chatFragment(navigate());

        fragment.setConversation(null);

        assertFalse(menu(fragment).isVisible());
        assertEquals(menuItemText(0), memoryAction(fragment).getText());
    }

    AiChatMessage userMessage(String text, boolean keepInContext) {
        AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.USER, text,
                keepInContext);
        // Another author, so that the card does not need the test principal's instance name.
        message.setCreatedBy("alice");
        return message;
    }

    String menuItemText(int keptCount) {
        return messages.formatMessage("", "assistantMemory.menuItem", keptCount);
    }

    AiChatView navigate() {
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, conversation.getId()));
        return UiTestUtils.getCurrentView();
    }

    AiChatFragment chatFragment(AiChatView view) {
        return UiTestUtils.getComponent(view, "chatFragment");
    }

    TestKeepInContextAiChatPanel lastChatPanel() {
        return Objects.requireNonNull(TestPanelProvider.lastChatPanel);
    }

    DropdownButton menu(AiChatFragment fragment) {
        return (DropdownButton) UiComponentUtils.getComponent(fragment, "chatActionsBtn");
    }

    ActionItem memoryItem(AiChatFragment fragment) {
        return (ActionItem) Objects.requireNonNull(menu(fragment).getItem(ShowAssistantMemoryAction.ID));
    }

    Action memoryAction(AiChatFragment fragment) {
        return memoryItem(fragment).getAction();
    }

    void openMemory(AiChatFragment fragment) {
        memoryAction(fragment).actionPerform(menu(fragment));
    }

    SidePanelLayout sidePanel(AiChatFragment fragment) {
        return (SidePanelLayout) UiComponentUtils.getComponent(fragment, "sidePanel");
    }

    /**
     * The memory fragment shown in the side panel; it is created when the memory is opened.
     */
    AssistantMemoryFragment memoryFragment(AiChatFragment fragment) {
        return (AssistantMemoryFragment) UiComponentUtils.getComponent(fragment, "sidePanelContent")
                .getChildren()
                .findFirst()
                .orElseThrow();
    }

    Component emptyState(AiChatFragment fragment) {
        return UiComponentUtils.getComponent(memoryFragment(fragment), "memoryEmpty");
    }

    List<Card> cards(AiChatFragment fragment) {
        return descendants(memoryFragment(fragment))
                .filter(component -> component instanceof Card card && card.hasClassName(CARD_CN))
                .map(Card.class::cast)
                .toList();
    }

    /**
     * Delivers a browser click on the card. The card filters out clicks on its remove button in the browser; the
     * filter result travels with the event, so the test states it.
     */
    void clickCard(Card card, boolean onRemoveButton) {
        ObjectNode eventData = JacksonUtils.createObjectNode();
        eventData.put("!event.target.closest('." + CARD_REMOVE_CN + "')", !onRemoveButton);
        card.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(card.getElement(), "click", eventData));
    }

    /**
     * The text of each card: plain text of a user message, the Markdown source of an assistant one.
     */
    List<String> cardTexts(AiChatFragment fragment) {
        return descendants(memoryFragment(fragment))
                .filter(component -> component.hasClassName(CARD_TEXT_CN))
                .map(component -> component instanceof Markdown markdown ? markdown.getContent()
                        : ((Span) component).getText())
                .toList();
    }

    List<Button> removeButtons(AiChatFragment fragment) {
        return descendants(memoryFragment(fragment))
                .filter(component -> component instanceof Button button && button.hasClassName(CARD_REMOVE_CN))
                .map(Button.class::cast)
                .toList();
    }

    Stream<Component> descendants(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::descendants));
    }

    @TestConfiguration
    static class TestPanelConfiguration {

        @Bean
        TestPanelProvider testKeepInContextPanelProvider() {
            return new TestPanelProvider();
        }
    }

    static class TestPanelProvider implements AiChatPanelProvider {

        @Nullable
        static TestKeepInContextAiChatPanel lastChatPanel;

        @Override
        public AiChatPanel createChatPanel(FragmentOwner owner) {
            lastChatPanel = new TestKeepInContextAiChatPanel();
            return lastChatPanel;
        }

        @Override
        public AiChatInputPanel createInputPanel(FragmentOwner owner) {
            return new TestAiChatInputPanel();
        }

        @Override
        public int getOrder() {
            return JmixOrder.HIGHEST_PRECEDENCE;
        }
    }
}
