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

package io.jmix.aitoolsflowui.fragment.chat.prompt;

import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Suggests questions the user may ask in the chat, e.g. prompts for the rows of a particular entity the user is
 * about to send. Implementations are Spring beans ordered with {@link Order @Order}. The
 * {@link AiSuggestedPromptManager} asks them in their order and takes the first non-empty result.
 * <p>
 * A provider answers for the state of the chat its context describes. A context may come without data, so a provider
 * that only knows about data returns an empty list then.
 * <p>
 * A provider may be called outside the UI thread, so an implementation must not access UI state. An implementation
 * may take noticeable time to answer.
 */
@Experimental
@NullMarked
public interface AiSuggestedPromptProvider {

    /**
     * Suggests questions for the given state of the chat.
     *
     * @param context state of the chat to suggest questions for
     * @return prompts in the current user's language, or an empty list to leave the choice to the next provider
     */
    List<String> getPrompts(AiSuggestedPromptContext context);
}
