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

import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.fragment.FragmentOwner;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the panels of the first {@link AiChatPanelProvider} by order.
 */
@Experimental
@NullMarked
@Component("aitls_AiChatPanelFactory")
public class AiChatPanelFactory {

    @Autowired
    protected List<AiChatPanelProvider> providers;

    /**
     * Creates a conversation panel.
     *
     * @param owner the fragment the panel is created for
     * @return a new panel of the first provider
     */
    public AiChatPanel createChatPanel(FragmentOwner owner) {
        return getProvider().createChatPanel(owner);
    }

    /**
     * Creates a message input panel.
     *
     * @param owner the fragment the panel is created for
     * @return a new panel of the first provider
     */
    public AiChatInputPanel createInputPanel(FragmentOwner owner) {
        return getProvider().createInputPanel(owner);
    }

    protected AiChatPanelProvider getProvider() {
        // Spring sorts an injected list by Ordered; never empty: the add-on registers its own provider.
        return providers.get(0);
    }
}
