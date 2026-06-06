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
import io.jmix.core.EntitySet;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/**
 * Thin host view around {@link AiConversationFragment}.
 * <p>
 * Owns the {@code aiConversationDc} instance container (so Jmix navigation
 * and {@code @EditedEntityContainer} keep working), and forwards the loaded
 * conversation to the fragment on each load. The fragment owns all of the
 * actual chat UI — timeline, composer, thinking indicator, title edit.
 */
@Route(value = "aitols-ai-conversations/:id", layout = DefaultMainViewParent.class)
@ViewController("aitols_AiConversation.detail")
@ViewDescriptor("ai-conversation-detail-view.xml")
@EditedEntityContainer("aiConversationDc")
public class AiConversationDetailView extends StandardDetailView<AiConversation> {

    @ViewComponent
    private InstanceContainer<AiConversation> aiConversationDc;

    @ViewComponent
    private AiConversationFragment detailFragment;

    private boolean initialPromptSent = false;

    @Subscribe
    public void onInit(final InitEvent event) {
        setShowSaveNotification(false);

        detailFragment.setPersistDelegate(this::onDetailFragmentPersist);
        detailFragment.setReloadDelegate(this::onDetailFragmentReload);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        detailFragment.setConversation(aiConversationDc.getItemOrNull());
        detailFragment.focusMessageInput();
    }

    /**
     * Sends an initial user prompt into the bound conversation, used by the
     * chat starter's navigation handler. Idempotent: runs at most once per view
     * instance, so a re-fired navigation handler cannot double-submit.
     */
    public void sendInitialPrompt(String prompt) {
        if (initialPromptSent || prompt == null || prompt.isBlank()) {
            return;
        }
        initialPromptSent = true;
        detailFragment.sendMessage(prompt);
    }

    protected AiConversation onDetailFragmentPersist(AiConversation conversation) {
        EntitySet entitySet = getViewData().getDataContext().save();
        return entitySet.get(aiConversationDc.getItem());
    }

    protected AiConversation onDetailFragmentReload(AiConversation conversation) {
        getViewData().loadAll();
        return aiConversationDc.getItem();
    }
}
