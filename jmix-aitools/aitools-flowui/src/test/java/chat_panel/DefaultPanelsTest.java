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

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelFactory;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItem;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItemType;
import io.jmix.aitoolsflowui.fragment.input.AiChatInputPanel;
import io.jmix.aitoolsflowui.fragment.input.TextAreaChatInputPanel;
import io.jmix.aitoolsflowui.view.chathub.AiChatHubView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * With no other provider registered, the add-on's own panels draw the chat: the timeline and the text area input.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class DefaultPanelsTest {

    @Autowired
    AiChatPanelFactory chatPanelFactory;
    @Autowired
    ViewNavigationSupport navigationSupport;

    @Test
    void onlyDefaultProvider_panelsAreTimelineAndTextArea() {
        AiChatHubView hub = navigateToHub();

        assertInstanceOf(TimelineChatPanel.class, chatPanelFactory.createChatPanel(hub));
        assertInstanceOf(TextAreaChatInputPanel.class, chatPanelFactory.createInputPanel(hub));
    }

    @Test
    void textAreaPanel_submitHandsTrimmedText_blankIgnored() {
        AiChatHubView hub = navigateToHub();
        AiChatInputPanel panel = chatPanelFactory.createInputPanel(hub);
        hub.getContent().add(panel.asComponent());
        List<String> submitted = new ArrayList<>();
        panel.setSubmitHandler(submitted::add);
        Composite<?> panelComponent = (Composite<?>) panel.asComponent();
        JmixTextArea messageField = (JmixTextArea) UiComponentUtils.getComponent(panelComponent, "messageField");
        JmixButton sendButton = (JmixButton) UiComponentUtils.getComponent(panelComponent, "sendButton");

        messageField.setValue("  Hi  ");
        sendButton.click();
        messageField.setValue("   ");
        sendButton.click();

        assertEquals(List.of("Hi"), submitted);
    }

    @Test
    void timelinePanel_showResponseFailed_appendsErrorRow() {
        AiChatHubView hub = navigateToHub();
        AiChatPanel panel = chatPanelFactory.createChatPanel(hub);
        hub.getContent().add(panel.asComponent());

        panel.showMessages(List.of());
        panel.showResponseFailed("Oops");

        List<TimelineItem> items = timelineItems(panel);
        assertEquals(1, items.size());
        assertEquals(TimelineItemType.ASSISTANT, items.get(0).getType());
        assertEquals("Oops", items.get(0).getMessage().getContent());
    }

    AiChatHubView navigateToHub() {
        navigationSupport.navigate(AiChatHubView.class);
        return UiTestUtils.getCurrentView();
    }

    List<TimelineItem> timelineItems(AiChatPanel panel) {
        @SuppressWarnings("unchecked")
        JmixVirtualList<TimelineItem> list = (JmixVirtualList<TimelineItem>) UiComponentUtils.getComponent(
                (Composite<?>) panel.asComponent(), "timelineList");
        @SuppressWarnings("unchecked")
        DataProvider<TimelineItem, Object> dataProvider =
                (DataProvider<TimelineItem, Object>) (DataProvider<?, ?>) list.getDataProvider();
        return dataProvider.fetch(new Query<>()).toList();
    }
}
