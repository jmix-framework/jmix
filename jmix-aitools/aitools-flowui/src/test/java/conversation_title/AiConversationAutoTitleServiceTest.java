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
import io.jmix.aitoolsflowui.service.AiConversationAutoTitleService;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiConversationService;
import test_support.TestAiConversationTitleGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The synchronous core of the title service: generation, the manual-rename guard and failure handling.
 */
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiConversationAutoTitleServiceTest {

    @Autowired AiConversationAutoTitleService service;
    @Autowired TestAiConversationService conversationService;
    @Autowired TestAiConversationTitleGenerator generator;

    @AfterEach
    void tearDown() {
        conversationService.clear();
        generator.reset();
    }

    @Test
    void generateAndApply_titleUnchanged_persistsGeneratedTitle() {
        AiConversation conversation = conversationService.addConversation("Show revenue");
        generator.setNextTitle("Revenue overview");

        String applied = service.generateAndApply(conversation.getId(), "Show revenue", "Show revenue");

        assertEquals("Revenue overview", applied);
        assertEquals("Revenue overview", conversationService.loadConversation(conversation.getId()).getTitle());
    }

    @Test
    void generateAndApply_titleChangedMeanwhile_skips() {
        AiConversation conversation = conversationService.addConversation("Renamed by user");
        generator.setNextTitle("Revenue overview");

        String applied = service.generateAndApply(conversation.getId(), "Show revenue", "Show revenue");

        assertNull(applied);
        assertEquals("Renamed by user", conversationService.loadConversation(conversation.getId()).getTitle());
    }

    @Test
    void generateAndApply_generatorEmpty_skips() {
        AiConversation conversation = conversationService.addConversation("Show revenue");

        String applied = service.generateAndApply(conversation.getId(), "Show revenue", "Show revenue");

        assertNull(applied);
        assertEquals("Show revenue", conversationService.loadConversation(conversation.getId()).getTitle());
    }

    @Test
    void generateAndApply_generatorThrows_returnsNull() {
        AiConversation conversation = conversationService.addConversation("Show revenue");
        generator.failWith(new RuntimeException("boom"));

        String applied = service.generateAndApply(conversation.getId(), "Show revenue", "Show revenue");

        assertNull(applied);
        assertEquals("Show revenue", conversationService.loadConversation(conversation.getId()).getTitle());
    }
}
