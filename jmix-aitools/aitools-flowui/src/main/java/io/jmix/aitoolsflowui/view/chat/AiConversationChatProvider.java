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

import io.jmix.core.JmixOrder;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.fragment.FragmentOwner;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;

/**
 * Creates the {@link AiConversationChat} that hosts such as {@link AiChatView} show. Implementations are Spring beans;
 * {@link AiConversationChatFactory} uses the one with the lowest {@link #getOrder() order} value, that is the highest
 * precedence.
 * <p>
 * The add-on's own provider has {@link Ordered#LOWEST_PRECEDENCE}, so it is used only when nothing else is
 * registered. Other add-ons use {@link JmixOrder#LOWEST_PRECEDENCE}. An application provider must return an order
 * lower than the one of the provider it replaces, e.g. {@link JmixOrder#HIGHEST_PRECEDENCE}.
 */
@Experimental
@NullMarked
public interface AiConversationChatProvider extends Ordered {

    /**
     * Creates a new chat for the given owner.
     *
     * @param owner the view or fragment the chat is created for
     * @return a new chat, not bound to a conversation
     */
    AiConversationChat create(FragmentOwner owner);
}
