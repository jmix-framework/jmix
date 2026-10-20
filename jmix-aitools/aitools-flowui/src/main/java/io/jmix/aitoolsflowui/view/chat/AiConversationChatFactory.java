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

package io.jmix.aitoolsflowui.view.chat;

import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.fragment.FragmentOwner;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the {@link AiConversationChat} a host shows. The chat comes from the first
 * {@link AiConversationChatProvider} by order, so hosts need not know which providers are registered.
 */
@Experimental
@Component("aitls_AiConversationChatFactory")
public class AiConversationChatFactory {

    @Autowired
    protected List<AiConversationChatProvider> chatProviders;

    /**
     * Creates a new chat for the given owner.
     *
     * @param owner the view or fragment the chat is created for
     * @return a new chat, not bound to a conversation
     */
    @NullMarked
    public AiConversationChat create(FragmentOwner owner) {
        // Never empty: the add-on registers its own provider.
        return chatProviders.get(0).create(owner);
    }
}
