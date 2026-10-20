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

package chat_provider;

import com.vaadin.flow.component.Component;
import io.jmix.aitoolsflowui.fragment.chat.AiConversationChat;
import io.jmix.aitoolsflowui.fragment.chat.AiConversationChatFactory;
import io.jmix.aitoolsflowui.fragment.chat.AiConversationChatProvider;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.fragment.FragmentOwner;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiConversationChat;
import test_support.TestAiConversationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A provider ordered before the add-on's own one replaces the chat the chat view shows.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiConversationChatProviderOverrideTest.TestProviderConfiguration.class})
class AiConversationChatProviderOverrideTest {

    @Autowired
    AiConversationChatFactory chatFactory;
    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    RouteSupport routeSupport;
    @Autowired
    TestAiConversationService conversationService;

    @AfterEach
    void tearDown() {
        conversationService.clear();
    }

    @Test
    void create_higherPriorityProvider_returnsItsChat() {
        AiChatView view = navigateToConversation(conversationService.addConversation("Revenue by region"));

        assertInstanceOf(TestAiConversationChat.class, chatFactory.create(view));
    }

    @Test
    void aiChatView_higherPriorityProvider_showsItsChat() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");

        AiChatView view = navigateToConversation(conversation);

        Component chat = UiTestUtils.getComponent(view, AiChatView.CHAT_COMPONENT_ID);
        TestAiConversationChat testChat = assertInstanceOf(TestAiConversationChat.class, chat);
        assertNotNull(testChat.getConversation());
        assertEquals(conversation.getId(), testChat.getConversation().getId());
    }

    private AiChatView navigateToConversation(AiConversation conversation) {
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, conversation.getId()));
        return UiTestUtils.getCurrentView();
    }

    @TestConfiguration
    static class TestProviderConfiguration {

        @Bean
        TestChatProvider testChatProvider() {
            return new TestChatProvider();
        }
    }

    static class TestChatProvider implements AiConversationChatProvider {

        @Override
        public AiConversationChat create(FragmentOwner owner) {
            return new TestAiConversationChat();
        }

        @Override
        public int getOrder() {
            return JmixOrder.HIGHEST_PRECEDENCE;
        }
    }
}
