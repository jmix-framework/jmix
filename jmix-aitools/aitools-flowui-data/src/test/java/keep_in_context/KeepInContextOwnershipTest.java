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

package keep_in_context;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowuidata.converter.AiChatMessageConverter;
import io.jmix.aitoolsflowuidata.converter.AiConversationConverter;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntityType;
import io.jmix.aitoolsflowuidata.entity.AiConversationEntity;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsFlowuiDataTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsFlowuiDataTestConfiguration.class)
@TestPropertySource(properties = {
        "eclipselink.ddl-generation=create-tables",
        "jmix.core.work-dir=build/test-home/work"})
class KeepInContextOwnershipTest {

    @Autowired
    AiChatMessageService messageService;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    AiConversationConverter conversationConverter;
    @Autowired
    AiChatMessageConverter messageConverter;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    void createMessage_foreignConversation_accessDenied() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation foreign = foreignConversation();

            assertThrows(AccessDeniedException.class,
                    () -> messageService.createMessage(foreign, AiChatMessageType.USER, "Hi", true));
        });
    }

    @Test
    void setKeepInContext_foreignMessage_accessDeniedAndNotKept() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation foreign = foreignConversation();
            AiChatMessage message = foreignMessage(foreign);

            assertThrows(AccessDeniedException.class, () -> messageService.setKeepInContext(message, true));

            AiChatMessageEntity stored = dataManager.load(AiChatMessageEntity.class).id(message.getId()).one();
            assertFalse(stored.getKeepInContext());
        });
    }

    @Test
    void loadKeptInContextMessages_foreignConversation_empty() {
        systemAuthenticator.runWithSystem(() -> {
            AiConversation foreign = foreignConversation();
            keptForeignMessage(foreign);

            assertTrue(messageService.loadKeptInContextMessages(foreign).isEmpty());
        });
    }

    @Test
    void loadKeptInContextMessages_missingConversation_empty() {
        systemAuthenticator.runWithSystem(() ->
                assertTrue(messageService.loadKeptInContextMessages(missingConversation()).isEmpty()));
    }

    @Test
    void isKeepInContextLimitReached_foreignConversation_true() {
        systemAuthenticator.runWithSystem(() ->
                assertTrue(messageService.isKeepInContextLimitReached(foreignConversation())));
    }

    @Test
    void isKeepInContextLimitReached_missingConversation_true() {
        systemAuthenticator.runWithSystem(() ->
                assertTrue(messageService.isKeepInContextLimitReached(missingConversation())));
    }

    private AiConversation foreignConversation() {
        AiConversationEntity conversation = dataManager.create(AiConversationEntity.class);
        conversation.setTitle("Foreign");
        conversation.setUsername("someone-else");
        return conversationConverter.convertToModel(dataManager.save(conversation));
    }

    private AiChatMessage foreignMessage(AiConversation conversation) {
        AiChatMessageEntity message = dataManager.create(AiChatMessageEntity.class);
        message.setConversation(dataManager.load(AiConversationEntity.class).id(conversation.getId()).one());
        message.setType(AiChatMessageEntityType.USER);
        message.setContent("Hi");
        return messageConverter.convertToModel(dataManager.save(message), conversation);
    }

    private void keptForeignMessage(AiConversation conversation) {
        AiChatMessageEntity message = dataManager.load(AiChatMessageEntity.class)
                .id(foreignMessage(conversation).getId())
                .one();
        message.setKeepInContext(true);
        dataManager.save(message);
    }

    private AiConversation missingConversation() {
        AiConversationEntity conversation = dataManager.create(AiConversationEntity.class);
        conversation.setTitle("Missing");
        return conversationConverter.convertToModel(conversation);
    }
}
