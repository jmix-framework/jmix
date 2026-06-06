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

package io.jmix.aitoolsflowui.view.aiconversation;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelAfterOpenEvent;
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelCloseEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

/**
 * Custom master-detail screen for managing the {@link ChatMessage}s of one
 * {@link AiConversation}. The conversation id arrives as the {@code :id} route
 * segment. Messages are loaded by a standalone, paginated loader, so create /
 * edit / remove persist immediately (decoupled from the conversation editor).
 * <p>
 * Mirrors the Jmix UI sample {@code side-panel-layout-master-detail}: the
 * {@link ChatMessageEditFragment} editor is created fresh on every open and
 * dropped from the side panel on close.
 */
@Route(value = "aitols-ai-conversation-messages/:id", layout = DefaultMainViewParent.class)
@ViewController("aitols_AiConversationMessages")
@ViewDescriptor("ai-conversation-messages-view.xml")
public class AiConversationMessagesView extends StandardView {

    public static final String ROUTE_PARAM_ID = "id";

    @Autowired
    protected Fragments fragments;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    @ViewComponent
    protected SidePanelLayout sidePanelLayout;
    @ViewComponent
    protected VerticalLayout sidePanelContent;
    @ViewComponent
    protected DataGrid<ChatMessage> chatMessagesDataGrid;
    @ViewComponent
    protected H2 conversationTitle;

    @ViewComponent
    protected CollectionContainer<ChatMessage> chatMessagesDc;
    @ViewComponent
    protected CollectionLoader<ChatMessage> chatMessagesDl;

    @Nullable
    protected AiConversation conversation;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> rawId = event.getRouteParameters().get(ROUTE_PARAM_ID);
        if (rawId.isPresent()) {
            UUID id = urlParamSerializer.deserialize(UUID.class, rawId.get());
            conversation = dataManager.load(AiConversation.class).id(id).optional().orElse(null);
        }
        super.beforeEnter(event);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (conversation == null) {
            return;
        }
        conversationTitle.setText(metadataTools.getInstanceName(conversation));
        chatMessagesDl.setParameter("conversation", conversation);
        chatMessagesDl.load();
    }

    @Subscribe("chatMessagesDataGrid.create")
    public void onChatMessagesDataGridCreate(final ActionPerformedEvent event) {
        if (conversation == null) {
            return;
        }

        ChatMessageEditFragment fragment = createEditorFragment()
                .withNewItem(conversation.getId())
                .withSaveListener(e -> chatMessagesDl.load());
        sidePanelContent.add(fragment);
        sidePanelLayout.openSidePanel();
    }

    @Subscribe("chatMessagesDataGrid.edit")
    public void onChatMessagesDataGridEdit(final ActionPerformedEvent event) {
        ChatMessage selected = chatMessagesDataGrid.getSingleSelectedItem();
        if (selected == null) {
            return;
        }
        ChatMessageEditFragment fragment = createEditorFragment()
                .withEditedItem(selected)
                .withSaveListener(e -> chatMessagesDc.replaceItem(e.getItem()));
        sidePanelContent.add(fragment);
        sidePanelLayout.openSidePanel();
    }

    @Subscribe(id = "sidePanelLayout", subject = "addSidePanelAfterOpenListener")
    public void onSidePanelLayoutAfterOpen(final SidePanelAfterOpenEvent event) {
        if (sidePanelContent.getComponentCount() == 0) {
            return;
        }
        ((ChatMessageEditFragment) sidePanelContent.getComponentAt(0)).focusFirstField();
    }

    @Subscribe(id = "sidePanelLayout", subject = "addSidePanelCloseListener")
    public void onSidePanelLayoutClose(final SidePanelCloseEvent event) {
        sidePanelContent.removeAll();
    }

    protected ChatMessageEditFragment createEditorFragment() {
        return fragments.create(this, ChatMessageEditFragment.class)
                .withSidePanelLayout(sidePanelLayout);
    }
}
