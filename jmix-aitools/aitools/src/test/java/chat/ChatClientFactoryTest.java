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

package chat;

import io.jmix.aitools.ChatClientFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import repair.test_support.StubChatModel;
import repair.test_support.SpringAiJpqlRepairerTestConfiguration;
import test_support.AiToolsTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AiToolsTestConfiguration.class, SpringAiJpqlRepairerTestConfiguration.class})
class ChatClientFactoryTest {

    @Autowired
    ChatClientFactory chatClientFactory;

    @Autowired
    StubChatModel stubChatModel;

    @Test
    @DisplayName("Creates chat client and builder from configured prototype builder")
    void testCreatesChatClientAndBuilder() {
        stubChatModel.setContent("factory-response");

        ChatClient.Builder builder = chatClientFactory.createBuilder();
        ChatClient chatClient = chatClientFactory.createChatClient();

        assertNotNull(builder);
        assertNotNull(chatClient);
    }
}
