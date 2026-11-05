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

package io.jmix.aitoolsflowui.fragment.chat;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.core.annotation.Internal;
import org.jspecify.annotations.NullMarked;

/**
 * INTERNAL.
 * <p>
 * Fired by an {@link AiChatFragment} when it creates a conversation itself and binds it: on the first message of a
 * new conversation ({@link AiChatFragment#isNewConversation()}). A host that tracks the shown conversation, e.g. in the
 * URL, takes it from this event. Fired before the message is sent, so the conversation exists even if sending
 * fails.
 */
@Internal
@NullMarked
public class AiConversationCreatedEvent extends ComponentEvent<AiChatFragment> {

    protected final AiConversation conversation;

    public AiConversationCreatedEvent(AiChatFragment source, AiConversation conversation) {
        super(source, false);

        this.conversation = conversation;
    }

    /**
     * Returns the conversation the chat created and now shows.
     *
     * @return the created conversation
     */
    public AiConversation getConversation() {
        return conversation;
    }
}
