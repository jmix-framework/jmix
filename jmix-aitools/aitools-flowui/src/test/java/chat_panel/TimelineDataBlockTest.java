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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.component.AiChatDataBlockChip;
import io.jmix.aitoolsflowui.fragment.chat.component.AiChatSuggestions;
import io.jmix.aitoolsflowui.fragment.chat.component.timeline.TimelineChatPanel;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputFragment;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.view.ChatEmbedTestView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The timeline panel shows the pending data block above its input and the suggested prompts below it, and reports
 * the user's actions through the handlers.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class TimelineDataBlockTest {

    static final String CAPTION_CN = "jmix-ai-chat-data-block-caption";
    static final String WARNING_CN = "jmix-ai-chat-data-block-warning";
    static final String REMOVE_CN = "jmix-ai-chat-data-block-remove";
    static final String PROMPT_CN = "jmix-ai-chat-suggestion";

    @Autowired
    ViewNavigationSupport navigationSupport;

    @Test
    void showDataBlock_chipIsRightAboveTheInput() {
        TimelineChatPanel panel = createPanel();

        panel.showDataBlock("Orders · 12 rows", null);

        List<Component> children = inputContainerChildren(panel);
        assertEquals(2, children.size());
        assertInstanceOf(AiChatDataBlockChip.class, children.get(0));
        assertInstanceOf(AiChatInputFragment.class, children.get(1));
    }

    @Test
    void setSuggestedPrompts_buttonsAreRightBelowTheInput() {
        TimelineChatPanel panel = createPanel();

        panel.setSuggestedPrompts(List.of("Summarize", "Compare"));

        List<Component> children = inputContainerChildren(panel);
        assertEquals(2, children.size());
        assertInstanceOf(AiChatInputFragment.class, children.get(0));
        assertInstanceOf(AiChatSuggestions.class, children.get(1));
        assertEquals(List.of("Summarize", "Compare"), promptTexts(panel));
    }

    @Test
    void chipAndPrompts_inputIsBetweenThem() {
        TimelineChatPanel panel = createPanel();

        panel.setSuggestedPrompts(List.of("Summarize"));
        panel.showDataBlock("Orders", null);

        List<Component> children = inputContainerChildren(panel);
        assertEquals(3, children.size());
        assertInstanceOf(AiChatDataBlockChip.class, children.get(0));
        assertInstanceOf(AiChatInputFragment.class, children.get(1));
        assertInstanceOf(AiChatSuggestions.class, children.get(2));
    }

    @Test
    void showDataBlock_withCaption_showsIt() {
        TimelineChatPanel panel = createPanel();

        panel.showDataBlock("Orders · 12 rows", null);

        assertEquals("Orders · 12 rows", findOne(panel, CAPTION_CN, Span.class).getText());
    }

    @Test
    void showDataBlock_withWarning_showsIt() {
        TimelineChatPanel panel = createPanel();

        panel.showDataBlock("Orders", "Memory is full");

        Span warning = findOne(panel, WARNING_CN, Span.class);
        assertTrue(warning.isVisible());
        assertEquals("Memory is full", warning.getText());
    }

    @Test
    void showDataBlock_again_replacesCaptionAndWarning_keepsOneChip() {
        TimelineChatPanel panel = createPanel();
        panel.showDataBlock("Orders", "Memory is full");

        panel.showDataBlock("Customers", null);

        assertEquals(2, inputContainerChildren(panel).size());
        assertEquals("Customers", findOne(panel, CAPTION_CN, Span.class).getText());
        assertFalse(findOne(panel, WARNING_CN, Span.class).isVisible());
    }

    @Test
    void setSuggestedPrompts_empty_removesThem() {
        TimelineChatPanel panel = createPanel();
        panel.setSuggestedPrompts(List.of("Summarize"));

        panel.setSuggestedPrompts(List.of());

        List<Component> children = inputContainerChildren(panel);
        assertEquals(1, children.size());
        assertInstanceOf(AiChatInputFragment.class, children.get(0));
    }

    @Test
    void setSuggestedPrompts_afterRemoved_showsThemAgain() {
        TimelineChatPanel panel = createPanel();
        panel.setSuggestedPrompts(List.of("Summarize"));
        panel.setSuggestedPrompts(List.of());

        panel.setSuggestedPrompts(List.of("Compare"));

        assertEquals(2, inputContainerChildren(panel).size());
        assertEquals(List.of("Compare"), promptTexts(panel));
    }

    @Test
    void setSuggestedPrompts_sameAgain_keepsTheButtons() {
        TimelineChatPanel panel = createPanel();
        panel.setSuggestedPrompts(List.of("Summarize", "Compare"));
        List<Button> shown = find(panel, PROMPT_CN, Button.class);

        panel.setSuggestedPrompts(List.of("Summarize", "Compare"));

        List<Button> afterwards = find(panel, PROMPT_CN, Button.class);
        assertSame(shown.get(0), afterwards.get(0));
        assertSame(shown.get(1), afterwards.get(1));
    }

    @Test
    void removeClick_callsRemoveHandler() {
        TimelineChatPanel panel = createPanel();
        List<String> calls = new ArrayList<>();
        panel.setDataBlockRemoveHandler(() -> calls.add("removed"));
        panel.showDataBlock("Orders", null);

        findOne(panel, REMOVE_CN, Button.class).click();

        assertEquals(List.of("removed"), calls);
    }

    @Test
    void promptClick_callsPromptHandlerWithPrompt() {
        TimelineChatPanel panel = createPanel();
        List<String> picked = new ArrayList<>();
        panel.setSuggestedPromptHandler(picked::add);
        panel.setSuggestedPrompts(List.of("Summarize", "Compare"));

        find(panel, PROMPT_CN, Button.class).get(1).click();

        assertEquals(List.of("Compare"), picked);
    }

    @Test
    void handlerSetAfterShow_isUsed() {
        TimelineChatPanel panel = createPanel();
        panel.showDataBlock("Orders", null);
        panel.setSuggestedPrompts(List.of("Summarize"));
        List<String> calls = new ArrayList<>();

        panel.setDataBlockRemoveHandler(() -> calls.add("removed"));
        panel.setSuggestedPromptHandler(prompt -> calls.add(prompt));
        findOne(panel, REMOVE_CN, Button.class).click();
        find(panel, PROMPT_CN, Button.class).get(0).click();

        assertEquals(List.of("removed", "Summarize"), calls);
    }

    @Test
    void noHandlers_clicksAreIgnored() {
        TimelineChatPanel panel = createPanel();
        panel.showDataBlock("Orders", null);
        panel.setSuggestedPrompts(List.of("Summarize"));

        assertDoesNotThrow(() -> findOne(panel, REMOVE_CN, Button.class).click());
        assertDoesNotThrow(() -> find(panel, PROMPT_CN, Button.class).get(0).click());
    }

    @Test
    void handlersCleared_clicksAreIgnored() {
        TimelineChatPanel panel = createPanel();
        List<String> calls = new ArrayList<>();
        panel.setDataBlockRemoveHandler(() -> calls.add("removed"));
        panel.setSuggestedPromptHandler(calls::add);
        panel.showDataBlock("Orders", null);
        panel.setSuggestedPrompts(List.of("Summarize"));

        panel.setDataBlockRemoveHandler(null);
        panel.setSuggestedPromptHandler(null);
        findOne(panel, REMOVE_CN, Button.class).click();
        find(panel, PROMPT_CN, Button.class).get(0).click();

        assertTrue(calls.isEmpty());
    }

    @Test
    void hideDataBlock_removesChip_keepsPrompts() {
        TimelineChatPanel panel = createPanel();
        panel.showDataBlock("Orders", null);
        panel.setSuggestedPrompts(List.of("Summarize"));

        panel.hideDataBlock();

        List<Component> children = inputContainerChildren(panel);
        assertEquals(2, children.size());
        assertInstanceOf(AiChatInputFragment.class, children.get(0));
        assertInstanceOf(AiChatSuggestions.class, children.get(1));
    }

    @Test
    void hideDataBlock_withoutBlock_doesNothing() {
        TimelineChatPanel panel = createPanel();

        assertDoesNotThrow(panel::hideDataBlock);
        assertEquals(1, inputContainerChildren(panel).size());
    }

    @Test
    void showAfterHide_showsChipAgain() {
        TimelineChatPanel panel = createPanel();
        panel.showDataBlock("Orders", null);
        panel.hideDataBlock();

        panel.showDataBlock("Customers", null);

        assertEquals(2, inputContainerChildren(panel).size());
        assertEquals("Customers", findOne(panel, CAPTION_CN, Span.class).getText());
    }

    @Test
    void inputHidden_dataBlockGoesWithIt() {
        TimelineChatPanel panel = createPanel();
        panel.showDataBlock("Orders", null);

        panel.setInputVisible(false);

        assertFalse(inputContainer(panel).isVisible());
        assertInstanceOf(AiChatDataBlockChip.class, inputContainerChildren(panel).get(0));
    }

    /**
     * Takes the panel that a chat fragment declared in a view draws, which is initialized with the view.
     */
    TimelineChatPanel createPanel() {
        navigationSupport.navigate(ChatEmbedTestView.class);
        ChatEmbedTestView view = UiTestUtils.getCurrentView();
        AiChatFragment fragment = UiTestUtils.getComponent(view, "chatFragment");
        return (TimelineChatPanel) UiComponentUtils.getComponent(fragment, "chatPanelContainer")
                .getChildren().findFirst().orElseThrow();
    }

    VerticalLayout inputContainer(TimelineChatPanel panel) {
        return (VerticalLayout) UiComponentUtils.getComponent(panel, "chatInputContainer");
    }

    List<Component> inputContainerChildren(TimelineChatPanel panel) {
        return inputContainer(panel).getChildren().toList();
    }

    List<String> promptTexts(TimelineChatPanel panel) {
        return find(panel, PROMPT_CN, Button.class).stream()
                .map(Button::getText)
                .toList();
    }

    <T extends Component> T findOne(Component root, String className, Class<T> type) {
        List<T> found = find(root, className, type);
        assertEquals(1, found.size(), "components with class " + className);
        return found.get(0);
    }

    <T extends Component> List<T> find(Component root, String className, Class<T> type) {
        return descendants(root)
                .filter(type::isInstance)
                .filter(component -> component.getElement().getClassList().contains(className))
                .map(type::cast)
                .toList();
    }

    Stream<Component> descendants(Component component) {
        return component.getChildren()
                .flatMap(child -> Stream.concat(Stream.of(child), descendants(child)));
    }
}
