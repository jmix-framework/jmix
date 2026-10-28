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

import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineChatPanel;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.aitoolsflowui.fragment.input.TextAreaChatInputPanel;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.fragment.FragmentOwner;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * The add-on's own panels: {@link TimelineChatPanel} and {@link TextAreaChatInputPanel}. Ordered last, so an
 * {@link AiChatPanelProvider} with a lower order value takes over.
 */
@Experimental
@NullMarked
@Component("aitls_DefaultAiChatPanelProvider")
public class DefaultAiChatPanelProvider implements AiChatPanelProvider {

    @Autowired
    protected Fragments fragments;

    @Override
    public AiChatPanel createChatPanel(FragmentOwner owner) {
        return fragments.create(owner, TimelineChatPanel.class);
    }

    @Override
    public AiChatInputPanel createInputPanel(FragmentOwner owner) {
        return fragments.create(owner, TextAreaChatInputPanel.class);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
