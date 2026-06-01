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

package io.jmix.aitools.service.prompt;

import io.jmix.aitools.PromptProvider;

/**
 * Provides the system prompt template used by the generic {@code AiChatService}.
 * <p>
 * Mirrors {@code io.jmix.aitools.dataload.prompt.DataLoadChatSystemPromptProvider}
 * but for the umbrella chat that aggregates all {@code JmixAiTool}s.
 * <p>
 * Implementations return a {@link org.springframework.core.io.Resource} pointing at
 * a StringTemplate-style ({@code .st}) template. The final parameter binding is
 * performed by {@code AiChatServiceImpl} so additional parameters can be introduced
 * additively in the future without changing this contract.
 */
public interface AiChatSystemPromptProvider extends PromptProvider {
}
