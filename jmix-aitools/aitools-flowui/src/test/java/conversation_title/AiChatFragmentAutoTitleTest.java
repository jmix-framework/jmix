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

import com.vaadin.flow.component.html.H3;
import io.jmix.aitoolsflowui.view.chat.AiConversationTitleMode;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiConversationAutoTitleService;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Automatic titling through {@link AiChatFragment#sendMessage(String)} in the default mode and with per-chat
 * overrides. The assistant response and the background title generation are mocked.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatFragmentAutoTitleTest {

    @Autowired ViewNavigationSupport navigationSupport;
    @Autowired RouteSupport routeSupport;
    @Autowired AiToolsFlowuiProperties properties;
    @Autowired TestAiConversationService conversationService;
    @Autowired TestAiChatMessageService messageService;

    @MockitoBean AssistantResponseTaskCoordinator assistantResponseTaskCoordinator;
    @MockitoBean AiConversationAutoTitleService autoTitleService;

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
    }

    @Test
    void conversationTitleMode_default_isFirstMessage() {
        assertEquals(AiConversationTitleMode.FIRST_MESSAGE, properties.getConversationTitleMode());
    }

    @Test
    void sendMessage_firstMessage_setsTrimmedTitle_andPageTitle() {
        AiConversation conversation = conversationService.addConversation("New AI Conversation");
        AiChatView view = navigate(conversation.getId());

        chatFragment(view).sendMessage("  Show me   revenue by region ");

        assertEquals("Show me revenue by region",
                conversationService.loadConversation(conversation.getId()).getTitle());
        assertEquals("Show me revenue by region", view.getPageTitle());
        H3 title = UiTestUtils.getComponent(view, "chatFragment.conversationTitle");
        assertEquals("Show me revenue by region", title.getText());
        verifyNoInteractions(autoTitleService);
    }

    @Test
    void sendMessage_notFirstMessage_keepsTitle() {
        AiConversation conversation = conversationService.addConversation("Earlier title");
        messageService.createMessage(conversation, AiChatMessageType.USER, "Earlier question");
        AiChatView view = navigate(conversation.getId());

        chatFragment(view).sendMessage("Show me revenue by region");

        assertEquals("Earlier title", conversationService.loadConversation(conversation.getId()).getTitle());
        assertEquals("Earlier title", view.getPageTitle());
    }

    @Test
    void sendMessage_titleModeOverrideNone_keepsPresetTitle() {
        AiConversation conversation = conversationService.addConversation("Preset title");
        AiChatView view = navigate(conversation.getId());
        AiChatFragment fragment = chatFragment(view);

        // The application-level mode is FIRST_MESSAGE and would overwrite; the per-chat NONE override wins.
        fragment.setTitleMode(AiConversationTitleMode.NONE);
        fragment.sendMessage("Show me revenue by region");

        assertEquals("Preset title", conversationService.loadConversation(conversation.getId()).getTitle());
        verifyNoInteractions(autoTitleService);
    }

    @Test
    void sendMessage_titleModeOverrideGenerated_setsFirstMessageTitle_andStartsGeneration() {
        AiConversation conversation = conversationService.addConversation("New AI Conversation");
        AiChatView view = navigate(conversation.getId());
        AiChatFragment fragment = chatFragment(view);

        fragment.setTitleMode(AiConversationTitleMode.GENERATED);
        fragment.sendMessage("Show me revenue by region");

        assertEquals("Show me revenue by region",
                conversationService.loadConversation(conversation.getId()).getTitle());
        verify(autoTitleService).generateAndApplyAsync(
                eq(conversation.getId()), eq("Show me revenue by region"), eq("Show me revenue by region"), any());
    }

    private AiChatView navigate(UUID id) {
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, id));
        return UiTestUtils.getCurrentView();
    }

    private AiChatFragment chatFragment(AiChatView view) {
        return UiTestUtils.getComponent(view, "chatFragment");
    }
}
