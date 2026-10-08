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

package chat_navigation;

import com.vaadin.flow.component.html.H3;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chat.AiChatFragment;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.Messages;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiConversationService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The chat view titles itself after the conversation it shows, which is what identifies it as a browser
 * page and as a tab in Tabbed Mode.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatViewTitleTest {

    private static final String VIEW_TITLE_KEY = "io.jmix.aitoolsflowui.view.chat/aiChatView.title";

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    RouteSupport routeSupport;
    @Autowired
    Messages messages;
    @Autowired
    TestAiConversationService conversationService;

    @AfterEach
    void tearDown() {
        conversationService.clear();
    }

    @Test
    void pageTitle_conversationBound_isConversationTitle() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");

        AiChatView view = navigateToConversation(conversation.getId());

        assertEquals("Revenue by region", view.getPageTitle());
    }

    @Test
    void pageTitle_conversationRenamed_followsNewTitle() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");
        AiChatView view = navigateToConversation(conversation.getId());

        conversation.setTitle("Revenue by publisher");
        chatFragment(view).setConversation(conversation);

        assertEquals("Revenue by publisher", view.getPageTitle());
    }

    @Test
    void conversationTitle_shownByTheViewItself_isHiddenInTheFragment() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");

        AiChatView view = navigateToConversation(conversation.getId());

        // The view title already names the conversation, so the fragment must not repeat it.
        assertFalse(conversationTitle(view).isVisible());
        assertEquals("Revenue by region", view.getPageTitle());
    }

    @Test
    void conversationTitle_hostWithoutItsOwnTitle_keepsShowingIt() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");
        AiChatView view = navigateToConversation(conversation.getId());

        chatFragment(view).setTitleVisible(true);

        assertTrue(conversationTitle(view).isVisible());
        assertEquals("Revenue by region", conversationTitle(view).getText());
    }

    @Test
    void pageTitle_conversationNotFound_fallsBackToViewTitle() {
        AiChatView view = navigateToConversation(UUID.randomUUID());

        assertEquals(messages.getMessage(VIEW_TITLE_KEY), view.getPageTitle());
    }

    private AiChatView navigateToConversation(UUID conversationId) {
        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, conversationId));

        return UiTestUtils.getCurrentView();
    }

    private AiChatFragment chatFragment(AiChatView view) {
        return UiTestUtils.getComponent(view, "chatFragment");
    }

    private H3 conversationTitle(AiChatView view) {
        return UiTestUtils.getComponent(view, "chatFragment.conversationTitle");
    }
}
