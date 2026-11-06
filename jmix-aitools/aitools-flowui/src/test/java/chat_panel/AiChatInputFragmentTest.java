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
import com.vaadin.flow.component.Composite;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputFragment;
import io.jmix.aitoolsflowui.fragment.input.TextAreaChatInputPanel;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chathub.AiChatHubView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiConversationService;
import test_support.view.InputEmbedTestView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * {@link AiChatInputFragment} is a host that draws the first provider's input panel; the hub embeds it from XML.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatInputFragmentTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    TestAiConversationService conversationService;

    @AfterEach
    void tearDown() {
        conversationService.clear();
    }

    @Test
    void hub_showsInputFragment_withTextAreaPanel() {
        AiChatInputFragment input = hubInput(navigateToHub());

        assertInstanceOf(TextAreaChatInputPanel.class, panelOf(input));
    }

    @Test
    void hub_submit_startsConversation() {
        AiChatInputFragment input = hubInput(navigateToHub());
        Composite<?> panel = assertInstanceOf(Composite.class, panelOf(input));
        JmixTextArea messageField = (JmixTextArea) UiComponentUtils.getComponent(panel, "messageField");
        JmixButton sendButton = (JmixButton) UiComponentUtils.getComponent(panel, "sendButton");

        messageField.setValue("Revenue by region");
        sendButton.click();

        assertInstanceOf(AiChatView.class, UiTestUtils.getCurrentView());
    }

    @Test
    void declaredInXml_neverCalled_stillDrawsItsPanel() {
        navigationSupport.navigate(InputEmbedTestView.class);
        InputEmbedTestView view = UiTestUtils.getCurrentView();
        AiChatInputFragment input = UiTestUtils.getComponent(view, "inputFragment");

        assertInstanceOf(TextAreaChatInputPanel.class, panelOf(input));
    }

    @Test
    void restoreText_goesThroughThePanel() {
        AiChatInputFragment input = hubInput(navigateToHub());

        input.restoreText("Mine");

        JmixTextArea messageField = (JmixTextArea) UiComponentUtils.getComponent((Composite<?>) panelOf(input),
                "messageField");
        assertEquals("Mine", messageField.getValue());
    }

    AiChatHubView navigateToHub() {
        navigationSupport.navigate(AiChatHubView.class);
        return UiTestUtils.getCurrentView();
    }

    AiChatInputFragment hubInput(AiChatHubView hub) {
        return assertInstanceOf(AiChatInputFragment.class,
                UiTestUtils.getComponent(hub, "hubFragment.chatInput"));
    }

    Component panelOf(AiChatInputFragment input) {
        return UiComponentUtils.getComponent(input, "inputPanelContainer")
                .getChildren().findFirst().orElseThrow();
    }
}
