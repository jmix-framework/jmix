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

package chat_data_block;

import io.jmix.aitoolsflowui.model.AiChatDataBlock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatDataBlockTest {

    @Test
    void dataBlock_withoutPrompts_hasNoSuggestedPrompts() {
        AiChatDataBlock block = new AiChatDataBlock("Orders · 2 rows", "| a |", null);

        assertTrue(block.getSuggestedPrompts().isEmpty());
    }

    @Test
    void dataBlock_sourcePromptsChanged_keepsOwnCopy() {
        List<String> prompts = new ArrayList<>(List.of("Summarize"));

        AiChatDataBlock block = new AiChatDataBlock("Orders · 2 rows", "| a |", null, prompts);
        prompts.add("Compare");

        assertEquals(List.of("Summarize"), block.getSuggestedPrompts());
    }

    @Test
    void dataBlock_prompts_areUnmodifiable() {
        AiChatDataBlock block = new AiChatDataBlock("Orders · 2 rows", "| a |", null, List.of("Summarize"));

        assertThrows(UnsupportedOperationException.class, () -> block.getSuggestedPrompts().add("Compare"));
    }
}
