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

package io.jmix.aitoolsflowui.service;

/**
 * Outcome of a request to keep a message in the context of its conversation.
 *
 * @see AiChatMessageService#setKeepInContext(io.jmix.aitoolsflowui.model.AiChatMessage, boolean)
 */
public enum KeepInContextResult {

    /**
     * The message is in the requested state.
     */
    DONE,

    /**
     * The message was not kept because the conversation already holds the maximum number of kept messages.
     */
    LIMIT_REACHED
}
