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

package chat_hub;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import io.jmix.aitoolsflowui.fragment.chathub.component.AiConversationCard;
import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chathub.AiChatHubView;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiConversationService;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deleting a conversation from the chat hub stops its running response first, so the response does not try to
 * save its answer into a conversation that no longer exists.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatHubDeleteTest {

    @Autowired ViewNavigationSupport navigationSupport;
    @Autowired TestAiConversationService conversationService;

    @MockitoBean AiChatGenerationManager generationManager;

    @AfterEach
    void tearDown() {
        conversationService.clear();
    }

    @Test
    void deleteConfirmed_cancelsItsResponse_beforeRemovingConversation() {
        AiConversation conversation = conversationService.addConversation("Chat");
        AtomicBoolean storedAtCancel = new AtomicBoolean();
        when(generationManager.cancel(any())).thenAnswer(inv -> {
            storedAtCancel.set(conversationService.loadConversation(conversation.getId()) != null);
            return true;
        });
        navigationSupport.navigate(AiChatHubView.class);

        deleteButton().click();
        confirmButton().click();

        verify(generationManager).cancel(conversation);
        assertTrue(storedAtCancel.get());
        assertNull(conversationService.loadConversation(conversation.getId()));
    }

    @Test
    void deleteConfirmed_conversationAlreadyDeleted_refreshesWithoutError() {
        AiConversation conversation = conversationService.addConversation("Chat");
        when(generationManager.cancel(any())).thenThrow(new AccessDeniedException("entity", "aitls_AiConversation"));
        navigationSupport.navigate(AiChatHubView.class);
        Button delete = deleteButton();
        // Deleted meanwhile in another tab.
        conversationService.remove(conversation);

        delete.click();
        confirmButton().click();

        assertTrue(findComponents(UiTestUtils.getCurrentView(), AiConversationCard.class).findAny().isEmpty());
    }

    // Only the history panel's cards can be deleted.
    Button deleteButton() {
        return findComponents(UiTestUtils.getCurrentView(), AiConversationCard.class)
                .flatMap(card -> findComponents(card, Button.class))
                .filter(button -> button.getClassNames().stream().anyMatch(name -> name.endsWith("-delete")))
                .findFirst()
                .orElseThrow();
    }

    Button confirmButton() {
        DialogInfo dialog = Objects.requireNonNull(UiTestUtils.getLastOpenedDialog());
        return dialog.getButtons().stream()
                .filter(button -> button.getId().filter("yes"::equals).isPresent())
                .findFirst()
                .orElseThrow();
    }

    <T extends Component> Stream<T> findComponents(Component root, Class<T> componentType) {
        Stream<T> self = componentType.isInstance(root)
                ? Stream.of(componentType.cast(root))
                : Stream.empty();
        return Stream.concat(self, root.getChildren()
                .flatMap(child -> findComponents(child, componentType)));
    }
}
