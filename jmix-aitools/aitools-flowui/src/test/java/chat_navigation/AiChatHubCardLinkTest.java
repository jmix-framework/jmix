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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chathub.AiChatHubView;
import io.jmix.aitoolsflowui.view.chathub.component.AiConversationCard;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiConversationService;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Conversation cards on the chat hub are real links to the conversation route, which is what makes
 * "open in new tab", "copy link address" and keyboard activation work.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatHubCardLinkTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    TestAiConversationService conversationService;

    @AfterEach
    void tearDown() {
        conversationService.clear();
    }

    @Test
    void conversationCard_linksToConversationRoute() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");

        List<AiConversationCard> cards = openHubCards();
        assertFalse(cards.isEmpty());

        for (AiConversationCard card : cards) {
            List<Anchor> links = findComponents(card, Anchor.class).toList();

            assertEquals(1, links.size());
            assertEquals("aitls/chat/" + conversation.getId(), links.get(0).getHref());
        }
    }

    @Test
    void conversationCard_deleteButtonIsOutsideTheLink() {
        conversationService.addConversation("Revenue by region");

        AiConversationCard card = openHubCards().get(0);
        Anchor link = findComponents(card, Anchor.class).findFirst().orElseThrow();

        assertTrue(findComponents(link, Button.class).findAny().isEmpty());
    }

    private List<AiConversationCard> openHubCards() {
        navigationSupport.navigate(AiChatHubView.class);
        View<?> view = UiTestUtils.getCurrentView();

        return findComponents(view, AiConversationCard.class).toList();
    }

    private <T extends Component> Stream<T> findComponents(Component root, Class<T> componentType) {
        Stream<T> self = componentType.isInstance(root)
                ? Stream.of(componentType.cast(root))
                : Stream.empty();

        return Stream.concat(self, root.getChildren()
                .flatMap(child -> findComponents(child, componentType)));
    }
}
