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

package conversation_title;

import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chat.AiChatFragment;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.aitoolsflowui.view.chat.support.AssistantResponseTaskCoordinator;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiConversationService;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The application-level {@code NONE} mode keeps the default title.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(
        classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = "jmix.aitools.ui.conversation-title-mode=NONE")
class AiChatFragmentTitleModeNoneTest {

    @Autowired ViewNavigationSupport navigationSupport;
    @Autowired RouteSupport routeSupport;
    @Autowired TestAiConversationService conversationService;
    @Autowired TestAiChatMessageService messageService;

    @MockitoBean AssistantResponseTaskCoordinator assistantResponseTaskCoordinator;

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
    }

    @Test
    void sendMessage_firstMessage_keepsDefaultTitle() {
        AiConversation conversation = conversationService.addConversation("New AI Conversation");
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, conversation.getId()));
        AiChatView view = UiTestUtils.getCurrentView();
        AiChatFragment fragment = UiTestUtils.getComponent(view, "chatFragment");

        fragment.sendMessage("Show me revenue by region");

        assertEquals("New AI Conversation", conversationService.loadConversation(conversation.getId()).getTitle());
        assertEquals("New AI Conversation", view.getPageTitle());
    }
}
