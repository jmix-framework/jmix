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

package chat_panel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItem;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.core.Messages;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.vaadin.TestUI;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiConversationService;
import test_support.view.ChatEmbedTestView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The timeline panel draws a pin control on user and assistant messages, reports the user's request through the
 * handler, and redraws a single message when its mark changes.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class TimelineKeepInContextTest {

    static final String KEEP_BUTTON_CN = "timeline-keep-button";
    static final String KEPT_CN = "timeline-keep-button-kept";
    static final String KEEP_IN_CONTEXT_ENABLED_CN = "timeline-keep-in-context-enabled";

    record KeepRequest(AiChatMessage message, boolean keep) {
    }

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    TestAiConversationService conversationService;
    @Autowired
    TestAiChatMessageService messageService;
    @Autowired
    Messages messages;

    AiConversation conversation;
    List<KeepRequest> requests;

    @BeforeEach
    void setUp() {
        conversation = conversationService.addConversation("Chat");
        requests = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
    }

    @Test
    void userMessage_showsPinControl() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));

        assertEquals(1, renderedKeepButtons(panel).size());
    }

    @Test
    void assistantMessage_showsPinControl() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello")));

        assertEquals(1, renderedKeepButtons(panel).size());
    }

    @Test
    void toolMessage_hasNoPinControl() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(messageService.createMessage(conversation, AiChatMessageType.TOOL, "Result")));

        assertTrue(renderedKeepButtons(panel).isEmpty());
    }

    @Test
    void transientErrorRow_hasNoPinControl() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of());
        panel.showResponseFailed("Oops");

        assertTrue(renderedKeepButtons(panel).isEmpty());
    }

    @Test
    void thinkingRow_hasNoPinControl() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));
        panel.showResponseRunning();

        List<Button> buttons = renderedKeepButtons(panel);

        assertEquals(2, drawnRows(timelineList(panel)).size());
        assertEquals(1, buttons.size());
    }

    @Test
    void noHandler_pinControlIsNotOffered() {
        TimelineChatPanel panel = createPanel();
        panel.setKeepInContextHandler(null);
        panel.showMessages(List.of(userMessage("Hi")));

        assertFalse(timelineList(panel).hasClassName(KEEP_IN_CONTEXT_ENABLED_CN));

        renderedKeepButtons(panel).get(0).click();
        assertTrue(requests.isEmpty());
    }

    @Test
    void handlerSetAfterRowsDrawn_offersPinControlWithoutRedraw() {
        TimelineChatPanel panel = createPanel();
        panel.setKeepInContextHandler(null);
        panel.showMessages(List.of(userMessage("Hi")));
        Button buttonBefore = renderedKeepButtons(panel).get(0);

        panel.setKeepInContextHandler((message, keep) -> requests.add(new KeepRequest(message, keep)));

        assertTrue(timelineList(panel).hasClassName(KEEP_IN_CONTEXT_ENABLED_CN));
        Button buttonAfter = flushedKeepButtons(panel).get(0);
        assertSame(buttonBefore, buttonAfter);

        buttonAfter.click();
        assertEquals(1, requests.size());
    }

    @Test
    void click_onMessageNotKept_asksToKeep() {
        TimelineChatPanel panel = createPanel();
        AiChatMessage message = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello");
        panel.showMessages(List.of(message));

        renderedKeepButtons(panel).get(0).click();

        assertEquals(1, requests.size());
        assertSame(message, requests.get(0).message());
        assertTrue(requests.get(0).keep());
    }

    @Test
    void click_onKeptMessage_asksToRelease() {
        TimelineChatPanel panel = createPanel();
        AiChatMessage message = userMessage("Hi", true);
        panel.showMessages(List.of(message));

        renderedKeepButtons(panel).get(0).click();

        assertEquals(1, requests.size());
        assertSame(message, requests.get(0).message());
        assertFalse(requests.get(0).keep());
    }

    @Test
    void messageNotKept_controlIsNotHighlighted_andOffersToPin() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));

        Button button = renderedKeepButtons(panel).get(0);

        assertFalse(button.hasClassName(KEPT_CN));
        assertEquals(messages.getMessage("assistantMemory.pinDescription"),
                button.getElement().getAttribute("aria-label"));
    }

    @Test
    void keptMessage_controlIsHighlighted_andOffersToUnpin() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(
                messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello", true)));

        Button button = renderedKeepButtons(panel).get(0);

        assertTrue(button.hasClassName(KEPT_CN));
        assertEquals(messages.getMessage("assistantMemory.unpinDescription"),
                button.getElement().getAttribute("aria-label"));
    }

    @Test
    void refreshKeepInContext_redrawsOnlyThatRow_withoutReplacingItems() {
        TimelineChatPanel panel = createPanel();
        AiChatMessage first = userMessage("Hi");
        AiChatMessage second = messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello");
        panel.showMessages(List.of(first, second));
        List<Button> buttonsBefore = renderedKeepButtons(panel);
        assertEquals(2, buttonsBefore.size());
        assertTrue(buttonsBefore.stream().noneMatch(button -> button.hasClassName(KEPT_CN)));
        List<TimelineItem> itemsBefore = new ArrayList<>(timelineItems(panel));

        second.setKeepInContext(true);
        panel.refreshKeepInContext(second);

        List<Button> buttonsAfter = flushedKeepButtons(panel);
        assertEquals(2, buttonsAfter.size());
        // Only the refreshed row is drawn again: the other row keeps its control.
        assertSame(buttonsBefore.get(0), buttonsAfter.get(0));
        assertNotSame(buttonsBefore.get(1), buttonsAfter.get(1));
        assertFalse(buttonsAfter.get(0).hasClassName(KEPT_CN));
        assertTrue(buttonsAfter.get(1).hasClassName(KEPT_CN));

        List<TimelineItem> itemsAfter = timelineItems(panel);
        assertEquals(itemsBefore.size(), itemsAfter.size());
        for (int i = 0; i < itemsBefore.size(); i++) {
            assertSame(itemsBefore.get(i), itemsAfter.get(i));
        }
    }

    @Test
    void refreshKeepInContext_releasedMessage_dropsTheHighlight() {
        TimelineChatPanel panel = createPanel();
        AiChatMessage message = userMessage("Hi", true);
        panel.showMessages(List.of(message));
        assertTrue(renderedKeepButtons(panel).get(0).hasClassName(KEPT_CN));

        message.setKeepInContext(false);
        panel.refreshKeepInContext(message);

        assertFalse(renderedKeepButtons(panel).get(0).hasClassName(KEPT_CN));
    }

    @Test
    void refreshKeepInContext_unknownMessage_doesNothing() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));
        AiChatMessage unknown = userMessage("Other");

        assertDoesNotThrow(() -> panel.refreshKeepInContext(unknown));
        assertEquals(1, timelineItems(panel).size());
    }

    @Test
    void scrollToMessage_unknownMessage_doesNotFail() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));
        AiChatMessage unknown = userMessage("Other");

        assertDoesNotThrow(() -> panel.scrollToMessage(unknown));
    }

    @Test
    void scrollToMessage_shownMessage_doesNotFail() {
        TimelineChatPanel panel = createPanel();
        AiChatMessage message = userMessage("Hi");
        panel.showMessages(List.of(message, messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "A")));

        assertDoesNotThrow(() -> panel.scrollToMessage(message));
    }

    @Test
    void handlerCleared_pinControlIsNotOfferedAndClickIsIgnored() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));
        Button button = renderedKeepButtons(panel).get(0);

        panel.setKeepInContextHandler(null);

        assertFalse(timelineList(panel).hasClassName(KEEP_IN_CONTEXT_ENABLED_CN));
        button.click();
        assertTrue(requests.isEmpty());
    }

    @Test
    void handlerSetAgain_offersPinControlAgain() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));
        panel.setKeepInContextHandler(null);

        panel.setKeepInContextHandler((message, keep) -> requests.add(new KeepRequest(message, keep)));

        assertTrue(timelineList(panel).hasClassName(KEEP_IN_CONTEXT_ENABLED_CN));
        renderedKeepButtons(panel).get(0).click();
        assertEquals(1, requests.size());
    }

    @Test
    void inputHidden_doesNotAffectPinControl() {
        TimelineChatPanel panel = createPanel();
        panel.showMessages(List.of(userMessage("Hi")));

        panel.setInputVisible(false);

        assertTrue(timelineList(panel).hasClassName(KEEP_IN_CONTEXT_ENABLED_CN));
    }

    /**
     * Takes the panel that a chat fragment declared in a view draws, which is initialized with the view, and gives it
     * a handler that records the user's requests. The panel is driven directly: the fragment's own conversation logic
     * is not involved.
     */
    TimelineChatPanel createPanel() {
        navigationSupport.navigate(ChatEmbedTestView.class);
        ChatEmbedTestView view = UiTestUtils.getCurrentView();
        AiChatFragment fragment = UiTestUtils.getComponent(view, "chatFragment");
        TimelineChatPanel panel = (TimelineChatPanel) UiComponentUtils.getComponent(fragment, "chatPanelContainer")
                .getChildren().findFirst().orElseThrow();
        panel.setKeepInContextHandler((message, keep) -> requests.add(new KeepRequest(message, keep)));
        return panel;
    }

    /**
     * A message of another author, so that drawing its row does not need the test principal's instance name.
     */
    AiChatMessage userMessage(String text) {
        return userMessage(text, false);
    }

    AiChatMessage userMessage(String text, boolean keepInContext) {
        AiChatMessage message = messageService.createMessage(
                conversation, AiChatMessageType.USER, text, keepInContext);
        message.setCreatedBy("alice");
        return message;
    }

    /**
     * Lets the list draw its rows, as it does when the client asks for the visible ones, and returns the visible pin
     * controls of the drawn rows.
     */
    List<Button> renderedKeepButtons(TimelineChatPanel panel) {
        JmixVirtualList<TimelineItem> list = timelineList(panel);
        list.getDataCommunicator().setViewportRange(0, timelineItems(panel).size());
        return flushedKeepButtons(panel);
    }

    /**
     * Returns the visible pin controls of the rows drawn after pending changes are flushed, without asking the list
     * to draw its rows again.
     */
    List<Button> flushedKeepButtons(TimelineChatPanel panel) {
        JmixVirtualList<TimelineItem> list = timelineList(panel);
        ((TestUI) UI.getCurrent()).runExecutionsBeforeClientResponse();

        return drawnRows(list).stream()
                .flatMap(this::descendants)
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.hasClassName(KEEP_BUTTON_CN) && button.isVisible())
                .toList();
    }

    /**
     * The rows of a component renderer are virtual children of the list, so they are not among its children: each
     * one is wrapped in an element of its own, which holds the row's content.
     */
    List<Component> drawnRows(JmixVirtualList<TimelineItem> list) {
        List<Component> rows = new ArrayList<>();
        list.getElement().getNode().getFeatureIfInitialized(VirtualChildrenList.class)
                .ifPresent(virtualChildren -> virtualChildren.iterator().forEachRemaining(
                        node -> Element.get(node).getChildren()
                                .forEach(content -> content.getComponent().ifPresent(rows::add))));
        return rows;
    }

    @SuppressWarnings("unchecked")
    List<TimelineItem> timelineItems(TimelineChatPanel panel) {
        DataProvider<TimelineItem, Object> dataProvider =
                (DataProvider<TimelineItem, Object>) (DataProvider<?, ?>) timelineList(panel).getDataProvider();
        return dataProvider.fetch(new Query<>()).toList();
    }

    @SuppressWarnings("unchecked")
    JmixVirtualList<TimelineItem> timelineList(TimelineChatPanel panel) {
        return (JmixVirtualList<TimelineItem>) UiComponentUtils.getComponent(panel, "timelineList");
    }

    Stream<Component> descendants(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::descendants));
    }
}
