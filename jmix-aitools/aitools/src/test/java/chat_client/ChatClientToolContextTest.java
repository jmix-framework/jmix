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

package chat_client;

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.dataload.tool.DomainModelDiscoveryTool;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.AiToolStatusPublisher;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import repair.test_support.StubChatModel;
import test_support.AiToolsTestConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsTestConfiguration.class)
class ChatClientToolContextTest {

    @Autowired
    ChatClientFactory chatClientFactory;

    @Autowired
    AiToolRegistry aiToolRegistry;

    @Autowired
    StubChatModel stubChatModel;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeEach
    void resetChatModel() {
        stubChatModel.reset();
    }

    @Test
    @DisplayName("A caller's status callback reaches the tool through the default tool context")
    void testCallerStatusCallbackReachesTheTool() {
        List<AiToolStatusUpdate> published = new ArrayList<>();

        stubChatModel.enqueueToolCall(DomainModelDiscoveryTool.AVAILABLE_ENTITIES_TOOL, "{}");
        stubChatModel.setContent("Here are the entities");

        ChatClient chatClient = chatClientFactory.createChatClientWithDefaultAdvisors().orElseThrow();

        systemAuthenticator.begin();
        try {
            chatClient.prompt()
                    .user("list the available entities")
                    .tools(aiToolRegistry.getAllCallbacks())
                    .toolContext(Map.of(AiToolStatusPublisher.STATUS_UPDATE_CALLBACK,
                            (Consumer<AiToolStatusUpdate>) published::add))
                    .call()
                    .content();
        } finally {
            systemAuthenticator.end();
        }

        assertFalse(published.isEmpty(), "The tool should have published its status to the caller's callback");
    }
}
