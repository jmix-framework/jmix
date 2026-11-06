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

package suggested_prompts;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.prompt.AiSuggestedPromptContext;
import io.jmix.aitoolsflowui.prompt.AiSuggestedPromptManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiConversationService;
import test_support.TestSuggestedPromptProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The prompt manager offers the first non-empty answer of the providers by order and skips a failing provider.
 */
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiSuggestedPromptManagerTest.TestPromptConfiguration.class})
class AiSuggestedPromptManagerTest {

    @Autowired
    AiSuggestedPromptManager promptManager;
    @Autowired
    TestAiConversationService conversationService;
    @Autowired
    Metadata metadata;
    @Autowired
    @Qualifier("firstPromptProvider")
    TestSuggestedPromptProvider firstPromptProvider;
    @Autowired
    @Qualifier("secondPromptProvider")
    TestSuggestedPromptProvider secondPromptProvider;

    @AfterEach
    void tearDown() {
        conversationService.clear();
        firstPromptProvider.reset();
        secondPromptProvider.reset();
    }

    @Test
    void getPrompts_firstNonEmptyProviderByOrder_wins() {
        secondPromptProvider.prompts = List.of("Second");
        AiSuggestedPromptContext context = dataContext();

        assertEquals(List.of("Second"), promptManager.getPrompts(context));
        assertSame(context, firstPromptProvider.askedContexts.get(0));

        firstPromptProvider.prompts = List.of("First");

        assertEquals(List.of("First"), promptManager.getPrompts(context));
        assertEquals(1, secondPromptProvider.askedContexts.size());
    }

    @Test
    void getPrompts_providerFails_nextProviderAnswers() {
        firstPromptProvider.failure = new IllegalStateException("provider down");
        secondPromptProvider.prompts = List.of("Second");

        List<String> prompts = promptManager.getPrompts(dataContext());

        assertEquals(List.of("Second"), prompts);
        assertEquals(1, firstPromptProvider.askedContexts.size());
    }

    @Test
    void getPrompts_noData_offersWhatProvidersSay() {
        secondPromptProvider.prompts = List.of("Second");
        AiConversation conversation = conversationService.addConversation("New chat");

        List<String> prompts = promptManager.getPrompts(new AiSuggestedPromptContext(conversation, null, null));

        assertEquals(List.of("Second"), prompts);
        assertNull(firstPromptProvider.askedContexts.get(0).getDataMetaClass());
    }

    @Test
    void getPrompts_noProviderAnswers_offersNone_evenForData() {
        List<String> prompts = promptManager.getPrompts(dataContext());

        assertTrue(prompts.isEmpty());
        assertEquals(1, secondPromptProvider.askedContexts.size());
    }

    AiSuggestedPromptContext dataContext() {
        MetaClass metaClass = metadata.getClass(AiChatMessage.class);
        return new AiSuggestedPromptContext(null, metaClass, null);
    }

    @TestConfiguration
    static class TestPromptConfiguration {

        // Declared before the first one, so that only the order puts it second.
        @Bean
        TestSuggestedPromptProvider secondPromptProvider() {
            return new TestSuggestedPromptProvider(2);
        }

        @Bean
        TestSuggestedPromptProvider firstPromptProvider() {
            return new TestSuggestedPromptProvider(1);
        }
    }
}
