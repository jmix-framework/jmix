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
import io.jmix.aitoolsflowui.view.chat.support.ConversationTitleSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationTitleSupportTest {

    private final ConversationTitleSupport support = new ConversationTitleSupport();

    @Test
    void shortMessage_unchanged() {
        assertEquals("Show revenue by region", support.buildInitialTitle("Show revenue by region"));
    }

    @Test
    void whitespaceCollapsedAndTrimmed() {
        assertEquals("Show revenue by region", support.buildInitialTitle("  Show   revenue\nby   region  "));
    }

    @Test
    void tooLong_cutAtWordBoundary_noTrailingPunctuation() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= AiConversation.TITLE_MAX_LENGTH + 20) {
            sb.append("word").append(", ");
        }
        String result = support.buildInitialTitle(sb.toString());
        assertTrue(result.length() <= AiConversation.TITLE_MAX_LENGTH);
        assertTrue(result.endsWith("word"), "cut on a word boundary");
        assertFalse(result.endsWith(","), "trailing punctuation removed");
    }

    @Test
    void exactlyLimit_unchanged() {
        String exact = "a".repeat(AiConversation.TITLE_MAX_LENGTH);
        assertEquals(exact, support.buildInitialTitle(exact));
    }

    @Test
    void generatedTitle_surroundingQuotesStripped() {
        assertEquals("Revenue by region", support.sanitizeGeneratedTitle("\"Revenue by region\""));
    }

    @Test
    void generatedTitle_guillemetsStripped() {
        assertEquals("Revenue by region", support.sanitizeGeneratedTitle("«Revenue by region»"));
    }

    @Test
    void generatedTitle_mismatchedQuotes_kept() {
        assertEquals("\"Workers' rights", support.sanitizeGeneratedTitle("\"Workers' rights"));
    }

    @Test
    void generatedTitle_null_returnsNull() {
        assertNull(support.sanitizeGeneratedTitle(null));
    }

    @Test
    void generatedTitle_blank_returnsNull() {
        assertNull(support.sanitizeGeneratedTitle("   "));
    }
}
