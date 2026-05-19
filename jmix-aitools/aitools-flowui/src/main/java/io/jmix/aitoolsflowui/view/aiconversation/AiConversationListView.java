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

import com.vaadin.flow.router.Route;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "ai-conversations", layout = DefaultMainViewParent.class)
@ViewController("aitols_AiConversation.list")
@ViewDescriptor("ai-conversation-list-view.xml")
@LookupComponent("aiConversationsDataGrid")
@DialogMode(width = "90%", resizable = true)
public class AiConversationListView extends StandardListView<AiConversation> {

    @Autowired
    private Messages messages;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private DataManager dataManager;

    @Subscribe("aiConversationsDataGrid.createAction")
    public void onAiConversationsDataGridCreateAction(final ActionPerformedEvent event) {
        AiConversation aiConversation = createAiConversation();
        dataManager.save(aiConversation);

        viewNavigators.detailView(this, AiConversation.class)
                .editEntity(aiConversation)
                .navigate();
    }

    protected AiConversation createAiConversation() {
        AiConversation aiConversation = dataManager.create(AiConversation.class);
        aiConversation.setTitle(messages.getMessage("aiConversation.defaultTitle"));
        return aiConversation;
    }
}
