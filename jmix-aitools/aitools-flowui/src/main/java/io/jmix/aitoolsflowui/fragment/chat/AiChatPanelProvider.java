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

import io.jmix.aitoolsflowui.fragment.input.AiChatInputFragment;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.fragment.FragmentOwner;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;

/**
 * Supplies the panels {@link AiChatFragment} and {@link AiChatInputFragment} draw with. Providers are Spring beans;
 * {@link AiChatPanelFactory} uses the one with the lowest order value, so a provider of another add-on overrides
 * the add-on's own, and an application provider ordered lower overrides both. A provider supplies both panels, so
 * that the hub's input matches the conversation chat.
 */
@Experimental
@NullMarked
public interface AiChatPanelProvider extends Ordered {

    /**
     * Creates a new conversation panel.
     *
     * @param owner the fragment the panel is created for
     * @return a new panel
     */
    AiChatPanel createChatPanel(FragmentOwner owner);

    /**
     * Creates a new message input panel.
     *
     * @param owner the fragment the panel is created for
     * @return a new panel
     */
    AiChatInputPanel createInputPanel(FragmentOwner owner);
}
