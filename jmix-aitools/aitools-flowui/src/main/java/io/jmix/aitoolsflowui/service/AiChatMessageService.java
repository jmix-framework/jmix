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

import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface AiChatMessageService {

    AiChatMessage createMessage(AiConversation conversation, AiChatMessageType type, String message);

    @Nullable
    AiChatMessage loadLatestMessage(AiConversation conversation, @Nullable AiChatMessageType type);

    Collection<AiChatMessage> loadMessages(AiConversation conversation);
}
