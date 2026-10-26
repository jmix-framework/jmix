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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.renderer.TimelineItemRenderer;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItem;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItemType;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.vaadin.TestUI;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiConversationService;
import test_support.view.ChatEmbedTestView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AiChatFragment} declared in XML draws the conversation with the add-on's own panel when no other provider
 * is registered, and hands the host's avatar to it.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatFragmentPanelTest {

    static final String CUSTOM_AVATAR_TEXT = "custom-avatar";

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    RouteSupport routeSupport;
    @Autowired
    TestAiConversationService conversationService;
    @Autowired
    TestAiChatMessageService messageService;

    @AfterEach
    void tearDown() {
        conversationService.clear();
        messageService.clear();
    }

    @Test
    void declaredInXml_getsTheProvidersPanel() {
        ChatEmbedTestView view = navigateToEmbedView();

        assertInstanceOf(TimelineChatPanel.class, panelOf(chatFragment(view)));
    }

    @Test
    void aiChatView_drawsWithTimelinePanel_andShowsTitleRow() {
        AiConversation conversation = conversationService.addConversation("Revenue by region");

        navigationSupport.navigate(AiChatView.class,
                routeSupport.createRouteParameters(AiChatView.ROUTE_PARAM_ID, conversation.getId()));
        AiChatView view = UiTestUtils.getCurrentView();

        assertInstanceOf(TimelineChatPanel.class, panelOf(UiTestUtils.getComponent(view, "chatFragment")));
        H3 title = UiTestUtils.getComponent(view, "chatFragment.conversationTitle");
        assertEquals("Revenue by region", title.getText());
    }

    @Test
    void avatarSupplier_reachesTimelinePanel() {
        AiConversation conversation = conversationService.addConversation("Chat");
        messageService.createMessage(conversation, AiChatMessageType.ASSISTANT, "Hello!");
        AiChatFragment fragment = chatFragment(navigateToEmbedView());
        List<Span> suppliedIcons = new ArrayList<>();
        SerializableSupplier<Component> avatarSupplier = () -> {
            Span icon = new Span(CUSTOM_AVATAR_TEXT);
            suppliedIcons.add(icon);
            return icon;
        };

        fragment.setAiAvatarIconSupplier(avatarSupplier);
        fragment.setConversation(conversation);

        TimelineChatPanel panel = (TimelineChatPanel) panelOf(fragment);
        assertSame(avatarSupplier, panel.getAiAvatarIconSupplier());
        // The assistant row drawn for the stored answer carries the supplied icon.
        Component assistantRow = renderOnlyItem(panel, TimelineItemType.ASSISTANT, suppliedIcons);
        assertTrue(descendants(assistantRow).anyMatch(component ->
                component instanceof Span span && CUSTOM_AVATAR_TEXT.equals(span.getText())));
    }

    ChatEmbedTestView navigateToEmbedView() {
        navigationSupport.navigate(ChatEmbedTestView.class);
        return UiTestUtils.getCurrentView();
    }

    AiChatFragment chatFragment(ChatEmbedTestView view) {
        return UiTestUtils.getComponent(view, "chatFragment");
    }

    Component panelOf(AiChatFragment fragment) {
        return UiComponentUtils.getComponent(fragment, "chatPanelContainer")
                .getChildren().findFirst().orElseThrow();
    }

    /**
     * Lets the panel's list draw its only item, as it does when the client asks for the visible rows, and returns the
     * drawn row, found from the icon it took from the supplier.
     */
    @SuppressWarnings("unchecked")
    Component renderOnlyItem(TimelineChatPanel panel, TimelineItemType expectedType, List<Span> suppliedIcons) {
        JmixVirtualList<TimelineItem> list =
                (JmixVirtualList<TimelineItem>) UiComponentUtils.getComponent(panel, "timelineList");
        DataProvider<TimelineItem, Object> dataProvider =
                (DataProvider<TimelineItem, Object>) (DataProvider<?, ?>) list.getDataProvider();
        TimelineItem item = dataProvider.fetch(new Query<>()).findFirst().orElseThrow();
        assertEquals(expectedType, item.getType());

        list.getDataCommunicator().setViewportRange(0, 1);
        ((TestUI) UI.getCurrent()).runExecutionsBeforeClientResponse();

        assertEquals(1, suppliedIcons.size());
        Component row = suppliedIcons.get(0);
        while (!(row instanceof TimelineItemRenderer)) {
            row = row.getParent().orElseThrow();
        }
        assertSame(list, row.getParent().orElseThrow());
        return row;
    }

    Stream<Component> descendants(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::descendants));
    }
}
