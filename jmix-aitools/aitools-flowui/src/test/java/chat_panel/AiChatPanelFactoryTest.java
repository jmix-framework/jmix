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

package chat_panel;

import io.jmix.aitoolsflowui.fragment.chat.AiChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelFactory;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelProvider;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.flowui.fragment.FragmentOwner;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class AiChatPanelFactoryTest {

    @Test
    void firstProviderByOrder_suppliesBothPanels() {
        AiChatPanel chatPanel = mock(AiChatPanel.class);
        AiChatInputPanel inputPanel = mock(AiChatInputPanel.class);
        AiChatPanelFactory factory = new AiChatPanelFactory();
        ReflectionTestUtils.setField(factory, "providers", List.of(
                provider(1, chatPanel, inputPanel),
                provider(2, mock(AiChatPanel.class), mock(AiChatInputPanel.class))));

        FragmentOwner owner = mock(FragmentOwner.class);
        assertSame(chatPanel, factory.createChatPanel(owner));
        assertSame(inputPanel, factory.createInputPanel(owner));
    }

    AiChatPanelProvider provider(int order, AiChatPanel chatPanel, AiChatInputPanel inputPanel) {
        return new AiChatPanelProvider() {
            @Override
            public AiChatPanel createChatPanel(FragmentOwner owner) { return chatPanel; }

            @Override
            public AiChatInputPanel createInputPanel(FragmentOwner owner) { return inputPanel; }

            @Override
            public int getOrder() { return order; }
        };
    }
}
