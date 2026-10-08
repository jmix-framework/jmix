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

package io.jmix.aitoolsflowui.view.chat.support;

import io.jmix.aitoolsflowui.model.AiConversation;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Builds conversation titles that fit {@link AiConversation#TITLE_MAX_LENGTH}.
 */
@Component("aitls_ConversationTitleSupport")
public class ConversationTitleSupport {

    /**
     * Builds a title from the first user message: collapses whitespace and cuts an over-long text on a word
     * boundary.
     *
     * @param rawMessage the first user message text
     * @return the title
     */
    public String buildInitialTitle(String rawMessage) {
        return truncateToLimit(collapseWhitespace(rawMessage));
    }

    /**
     * Cleans a model-generated title: collapses whitespace, unwraps surrounding quotes and cuts an over-long
     * text.
     *
     * @param rawTitle the raw model output
     * @return the cleaned title, or {@code null} when the input is {@code null} or blank
     */
    @Nullable
    public String sanitizeGeneratedTitle(@Nullable String rawTitle) {
        if (rawTitle == null) {
            return null;
        }
        String cleaned = truncateToLimit(stripSurroundingQuotes(collapseWhitespace(rawTitle)));
        return cleaned.isBlank() ? null : cleaned;
    }

    protected String collapseWhitespace(String text) {
        return text.strip().replaceAll("\\s+", " ");
    }

    protected String truncateToLimit(String text) {
        if (text.length() <= AiConversation.TITLE_MAX_LENGTH) {
            return text;
        }
        String cut = text.substring(0, AiConversation.TITLE_MAX_LENGTH);
        int lastSpace = cut.lastIndexOf(' ');
        if (lastSpace > 0) {
            cut = cut.substring(0, lastSpace);
        }
        return stripTrailingPunctuation(cut.strip());
    }

    protected String stripTrailingPunctuation(String text) {
        int end = text.length();
        while (end > 0 && !Character.isLetterOrDigit(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }

    protected String stripSurroundingQuotes(String text) {
        String t = text.strip();
        if (t.length() >= 2 && isQuotePair(t.charAt(0), t.charAt(t.length() - 1))) {
            return t.substring(1, t.length() - 1).strip();
        }
        return t;
    }

    protected boolean isQuotePair(char opening, char closing) {
        return switch (opening) {
            case '"', '\'' -> closing == opening;
            case '“' -> closing == '”';
            case '«' -> closing == '»';
            default -> false;
        };
    }
}
