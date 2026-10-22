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

import io.jmix.aitoolsflowui.fragment.input.AiChatInput;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputFragment;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.fragment.FragmentOwner;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * The add-on's default {@link AiConversationChatProvider}: the timeline chat {@link AiChatFragment} and the
 * {@link AiChatInputFragment} message input. Used only when no other provider is registered.
 */
@Experimental
@Component("aitls_AiChatFragmentProvider")
public class AiChatFragmentProvider implements AiConversationChatProvider {

    @Autowired
    protected Fragments fragments;

    @NullMarked
    @Override
    public AiConversationChat create(FragmentOwner owner) {
        return fragments.create(owner, AiChatFragment.class);
    }

    @NullMarked
    @Override
    public AiChatInput createInput(FragmentOwner owner) {
        return fragments.create(owner, AiChatInputFragment.class);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
