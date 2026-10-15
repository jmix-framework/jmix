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

package io.jmix.aitoolsflowui.generation.event;

import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.util.UUID;

/**
 * Published when the response was cancelled; nothing was persisted.
 */
@NullMarked
public class AiChatGenerationCancelledEvent extends AiChatGenerationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    public AiChatGenerationCancelledEvent(Object source, UUID conversationId, UUID generationId) {
        super(source, conversationId, generationId);
    }
}
