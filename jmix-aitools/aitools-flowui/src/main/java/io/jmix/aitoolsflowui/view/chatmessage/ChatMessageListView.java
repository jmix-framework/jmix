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

package io.jmix.aitoolsflowui.view.chatmessage;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Route(value = "aitls/chat-messages", layout = DefaultMainViewParent.class)
@ViewController("aitls_ChatMessage.list")
@ViewDescriptor("chat-message-list-view.xml")
@LookupComponent("chatMessagesDataGrid")
@DialogMode(width = "60em", height = "40em", resizable = true)
public class ChatMessageListView extends StandardListView<ChatMessage> {

    public static final String QUERY_PARAM_CONVERSATION_ID = "conversationId";

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected H2 conversationTitle;
    @ViewComponent
    protected CollectionLoader<ChatMessage> chatMessagesDl;

    @Nullable
    protected AiConversation conversation;

    @Subscribe
    public void onQueryParametersChange(final QueryParametersChangeEvent event) {
        List<String> values = event.getQueryParameters().getParameters().get(QUERY_PARAM_CONVERSATION_ID);
        if (values != null && !values.isEmpty()) {
            UUID id = urlParamSerializer.deserialize(UUID.class, values.get(0));
            conversation = dataManager.load(AiConversation.class).id(id).optional().orElse(null);
            if (conversation != null) {
                chatMessagesDl.setParameter("conversationId", id);
            }
        }
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (conversation != null) {
            conversationTitle.setText(
                    messageBundle.formatMessage("chatMessageListView.aiConversationTitle",
                            metadataTools.getInstanceName(conversation)));
        } else {
            conversationTitle.setVisible(false);
        }
    }
}
