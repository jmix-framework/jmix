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

package io.jmix.aitools.service;

import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;

/**
 * Stateless, programmatic access to the AI assistant.
 */
public interface AiChatService {

    /**
     * Sends a message to the chat model and blocks until the full reply is produced.
     *
     * @param message user message
     * @return full reply text
     */
    @Nullable
    String send(String message);

    /**
     * Streams the chat model's reply as a flux of text chunks.
     *
     * @param message user message
     * @return flux emitting reply chunks in order
     */
    Flux<String> stream(String message);
}
