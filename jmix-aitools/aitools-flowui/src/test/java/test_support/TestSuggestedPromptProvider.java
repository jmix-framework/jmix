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

package test_support;

import io.jmix.aitoolsflowui.fragment.chat.prompt.AiSuggestedPromptContext;
import io.jmix.aitoolsflowui.fragment.chat.prompt.AiSuggestedPromptProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * Prompt provider whose answer a test sets. It records the contexts it is asked with.
 */
public class TestSuggestedPromptProvider implements AiSuggestedPromptProvider, Ordered {

    public final List<AiSuggestedPromptContext> askedContexts = new ArrayList<>();
    public List<String> prompts = List.of();
    @Nullable
    public RuntimeException failure;

    private final int order;

    public TestSuggestedPromptProvider(int order) {
        this.order = order;
    }

    public void reset() {
        prompts = List.of();
        failure = null;
        askedContexts.clear();
    }

    @Override
    public List<String> getPrompts(AiSuggestedPromptContext context) {
        askedContexts.add(context);
        if (failure != null) {
            throw failure;
        }
        return prompts;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
