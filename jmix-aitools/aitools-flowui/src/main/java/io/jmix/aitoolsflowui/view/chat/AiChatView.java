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

package io.jmix.aitoolsflowui.view.chat;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitoolsflowui.view.chathome.AiChatHomeView;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

/**
 * Standalone (non-detail) host view around {@link AiChatFragment}.
 * <p>
 * Opens either by URL — the {@code :id} route segment is the id of an
 * {@link AiConversation} that gets loaded and bound to the fragment — or
 * programmatically via {@link #setConversation(AiConversation)} (used by
 * {@code DialogWindows} and after-navigation handlers).
 * <p>
 * When the URL carries an id that does not resolve to a conversation, the
 * fragment is hidden and an in-view "chat not found" placeholder is shown.
 * When opened with no id and no conversation set, an empty-state placeholder
 * invites the user to start a chat (navigating to {@code AiChatHomeView}).
 * <p>
 * Unlike {@code AiConversationDetailView} this is a plain {@link StandardView}
 * with no own {@code DataContext}; the fragment keeps its default
 * {@code DataManager}-based persist/reload.
 */
@Route(value = "ai-chat/:id?", layout = DefaultMainViewParent.class)
@ViewController("AiChatView")
@ViewDescriptor("ai-chat-view.xml")
public class AiChatView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(AiChatView.class);

    public static final String ROUTE_PARAM_ID = "id";

    @Autowired
    private DataManager dataManager;
    @Autowired
    private FetchPlans fetchPlans;
    @Autowired
    private UrlParamSerializer urlParamSerializer;
    @Autowired
    private RouteSupport routeSupport;
    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private AiChatFragment chatFragment;
    @ViewComponent
    private VerticalLayout notFoundLayout;
    @ViewComponent
    private VerticalLayout emptyLayout;

    @Nullable
    private AiConversation conversation;
    private boolean conversationNotFound;
    private boolean contentInitialized;
    private boolean initialPromptSent;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadConversationFromRouteParameters(event);
        super.beforeEnter(event);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        contentInitialized = true;
        applyConversation();
        if (conversation != null) {
            chatFragment.focusMessageInput();
        }
    }

    /**
     * Binds a conversation to the hosted fragment programmatically. If the
     * view is already shown the conversation is applied immediately, otherwise
     * it is applied on {@code ReadyEvent}. Clears any previous "not found"
     * state and keeps the browser URL in sync (unless the view is opened in a
     * dialog).
     */
    public void setConversation(@Nullable AiConversation conversation) {
        this.conversation = conversation;
        this.conversationNotFound = false;
        if (contentInitialized) {
            applyConversation();
        }
    }

    /**
     * Sends an initial user prompt into the bound conversation, used by the
     * chat home's navigation handler. Idempotent: runs at most once per view
     * instance, so a re-fired navigation handler cannot double-submit. No-op
     * when no conversation is bound (e.g. the id did not resolve).
     */
    public void sendInitialPrompt(String prompt) {
        if (initialPromptSent || prompt == null || prompt.isBlank() || conversation == null) {
            return;
        }
        initialPromptSent = true;
        chatFragment.sendMessage(prompt);
    }

    /**
     * Reads the optional {@code :id} route segment and loads the corresponding
     * conversation. Idempotent: re-entering with the id of the already-loaded
     * conversation does not reload. A non-empty id that does not resolve puts
     * the view into the "not found" state.
     */
    protected void loadConversationFromRouteParameters(BeforeEnterEvent event) {
        Optional<String> rawId = event.getRouteParameters().get(ROUTE_PARAM_ID);
        if (rawId.isEmpty()) {
            return;
        }
        UUID id = urlParamSerializer.deserialize(UUID.class, rawId.get());
        if (conversation != null && id.equals(conversation.getId())) {
            return;
        }
        Optional<AiConversation> loaded = dataManager.load(AiConversation.class)
                .id(id)
                .fetchPlan(buildFetchPlan())
                .optional();
        if (loaded.isPresent()) {
            this.conversation = loaded.get();
            this.conversationNotFound = false;
        } else {
            this.conversation = null;
            this.conversationNotFound = true;
            log.warn("AiConversation with id {} not found", id);
        }
    }

    protected FetchPlan buildFetchPlan() {
        return fetchPlans.builder(AiConversation.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("messages", FetchPlan.BASE)
                .build();
    }

    @Subscribe(id = "newChatBtn", subject = "clickListener")
    public void onNewChatBtnClick(final ClickEvent<JmixButton> event) {
        navigateToChatHome();
    }

    @Subscribe(id = "notFoundNewChatBtn", subject = "clickListener")
    public void onNotFoundNewChatBtnClick(final ClickEvent<JmixButton> event) {
        navigateToChatHome();
    }

    protected void navigateToChatHome() {
        viewNavigators.view(this, AiChatHomeView.class).navigate();
    }

    /**
     * Reconciles the view with one of three states:
     * <ul>
     *   <li>a conversation is bound → the fragment is shown and bound;</li>
     *   <li>a requested id did not resolve → the "not found" placeholder;</li>
     *   <li>nothing is bound (opened without id) → the empty-state placeholder
     *       inviting the user to start a chat.</li>
     * </ul>
     * Binds the conversation and syncs the URL only in the first case.
     */
    protected void applyConversation() {
        boolean hasConversation = conversation != null;
        chatFragment.setVisible(hasConversation);
        notFoundLayout.setVisible(conversationNotFound);
        emptyLayout.setVisible(!hasConversation && !conversationNotFound);
        if (hasConversation) {
            chatFragment.setConversation(conversation);
            syncUrl();
        }
    }

    /**
     * Updates the browser URL to {@code ai-chat/<id>} via
     * {@code history.replaceState} so it stays consistent with the bound
     * conversation. Skipped when the view is opened in a dialog (changing the
     * page URL under a modal would be misleading) or has no UI yet.
     */
    protected void syncUrl() {
        if (conversation == null || conversation.getId() == null) {
            return;
        }
        if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            return;
        }
        getUI().ifPresent(ui -> routeSupport.replaceUrl(
                ui,
                AiChatView.class,
                routeSupport.createRouteParameters(ROUTE_PARAM_ID, conversation.getId())));
    }
}
