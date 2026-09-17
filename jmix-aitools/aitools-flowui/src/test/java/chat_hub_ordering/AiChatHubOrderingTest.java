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

package chat_hub_ordering;

import com.vaadin.flow.component.Component;
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
import test_support.TestAiChatMessageService;
import test_support.TestAiConversationService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The chat hub lists conversations most recently active first, where activity is the latest message date
 * and falls back to the creation date for conversations without messages.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatHubOrderingTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    TestAiConversationService conversationService;
    @Autowired
    TestAiChatMessageService messageService;

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
    }

    @Test
    void hubCards_conversationWithNewerMessage_comesBeforeNewerConversationWithoutMessages() {
        AiConversation old = conversationService.addConversation("old", day(1));
        AiConversation recent = conversationService.addConversation("new", day(3));
        messageService.addMessage(old, day(5));

        assertEquals(List.of(href(old), href(recent)), openHubCardHrefs());
    }

    @Test
    void hubCards_withoutMessages_orderedByCreationDateDescending() {
        AiConversation first = conversationService.addConversation("first", day(1));
        AiConversation third = conversationService.addConversation("third", day(3));
        AiConversation second = conversationService.addConversation("second", day(2));

        assertEquals(List.of(href(third), href(second), href(first)), openHubCardHrefs());
    }

    @Test
    void hubCards_latestOfSeveralMessages_definesActivity() {
        AiConversation chatty = conversationService.addConversation("chatty", day(1));
        AiConversation quiet = conversationService.addConversation("quiet", day(4));
        messageService.addMessage(chatty, day(2));
        messageService.addMessage(chatty, day(6));

        assertEquals(List.of(href(chatty), href(quiet)), openHubCardHrefs());
    }

    OffsetDateTime day(int day) {
        return OffsetDateTime.of(2026, 6, day, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    String href(AiConversation conversation) {
        return "aitls/chat/" + conversation.getId();
    }

    /**
     * Opens the hub and returns the distinct conversation links in card order. The recent strip and the
     * history panel render the same order, so distinct links give it once.
     */
    List<String> openHubCardHrefs() {
        navigationSupport.navigate(AiChatHubView.class);
        View<?> view = UiTestUtils.getCurrentView();
        return findComponents(view, AiConversationCard.class)
                .flatMap(card -> findComponents(card, Anchor.class))
                .map(Anchor::getHref)
                .distinct()
                .toList();
    }

    <T extends Component> Stream<T> findComponents(Component root, Class<T> componentType) {
        Stream<T> self = componentType.isInstance(root)
                ? Stream.of(componentType.cast(root))
                : Stream.empty();
        return Stream.concat(self, root.getChildren()
                .flatMap(child -> findComponents(child, componentType)));
    }
}
