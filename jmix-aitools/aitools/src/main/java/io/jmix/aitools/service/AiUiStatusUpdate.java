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

/**
 * Ephemeral status update emitted by an AI tool through {@link io.jmix.aitools.tool.AiToolStatusPublisher}
 * and delivered to the UI through the callback put into the tool context by
 * {@link AiConversationChatService}. Not persisted.
 */
public record AiUiStatusUpdate(String message) {
}
