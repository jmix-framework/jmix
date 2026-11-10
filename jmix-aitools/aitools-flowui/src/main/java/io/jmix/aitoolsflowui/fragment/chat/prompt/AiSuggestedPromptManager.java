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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chooses the questions the chat offers the user. An application can replace this bean to change how they are
 * chosen.
 */
@Experimental
@NullMarked
@Component("aitls_AiSuggestedPromptManager")
public class AiSuggestedPromptManager {

    private static final Logger log = LoggerFactory.getLogger(AiSuggestedPromptManager.class);

    @Autowired
    protected ObjectProvider<AiSuggestedPromptProvider> providers;

    /**
     * Returns the questions to offer in the given state of the chat: the first non-empty result of the
     * {@link AiSuggestedPromptProvider} beans by order. A provider that fails is skipped.
     *
     * @param context state of the chat
     * @return questions to offer, or an empty list if no provider suggests any
     */
    public List<String> getPrompts(AiSuggestedPromptContext context) {
        return providers.orderedStream()
                .map(provider -> getProviderPrompts(provider, context))
                .filter(prompts -> !prompts.isEmpty())
                .findFirst()
                .orElse(List.of());
    }

    protected List<String> getProviderPrompts(AiSuggestedPromptProvider provider, AiSuggestedPromptContext context) {
        try {
            return provider.getPrompts(context);
        } catch (RuntimeException e) {
            // Suggested questions are optional and must never break the chat.
            log.warn("Prompt provider {} failed; asking the next one", provider.getClass().getName(), e);
            return List.of();
        }
    }
}
